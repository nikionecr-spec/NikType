package com.niktype.keyboard.keyboard

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.text.InputType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.niktype.engine.geometry.FaLayoutStyle
import com.niktype.engine.geometry.KeyRect
import com.niktype.engine.geometry.KeyboardLayout
import com.niktype.engine.geometry.LanguageMode
import com.niktype.engine.geometry.LayoutParams
import com.niktype.engine.geometry.LayoutBuilder
import com.niktype.engine.geometry.LayoutMode
import com.niktype.engine.geometry.KeyType
import com.niktype.engine.touch.FingerProfileKind
import com.niktype.engine.touch.FingerProfiles
import com.niktype.engine.touch.KeyCalibration
import com.niktype.engine.touch.TouchPoint
import com.niktype.engine.touch.TouchResolver
import com.niktype.keyboard.MainActivity
import com.niktype.keyboard.data.KeyboardSettings
import com.niktype.keyboard.data.CalibrationFileStore
import com.niktype.keyboard.voice.VoiceResultBus
import com.niktype.keyboard.R
import com.niktype.prediction.PredictionEngine
import com.niktype.prediction.text.PersianText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

enum class ShiftState { OFF, ON, LOCK }

/** وضعیت پاپ‌آپ کلیدهای جایگزین / علائم. */
data class PopupState(
    val keyId: String,
    val options: List<String>,
    val outputs: List<String>,
    var selectedIndex: Int,
    val panelX: Float,
    val panelY: Float,
    val panelW: Float,
    val panelH: Float,
)

private enum class SessionMode { PRESS, POPUP, GESTURE, BACKSPACE_WORD }

private class PointerSession(
    val pointerId: Long,
    var key: KeyRect?,
    val startX: Float,
    val startY: Float,
    val startTime: Long,
) {
    var lastX = startX
    var lastY = startY
    var mode = SessionMode.PRESS
    var moved = false
    val path = ArrayList<TouchPoint>(32)
    var longPressJob: Job? = null
    var repeatJob: Job? = null
    var popupIndex = -1
}

/**
 * کنترلر مرکزی کیبورد: پل بین رابط Compose، موتورهای تشخیص/پیش‌بینی
 * و InputConnection سیستم. همه پردازش‌های سنگین خارج از UI Thread.
 */
class KeyboardController(
    private val service: InputMethodService,
    container: com.niktype.keyboard.AppContainer,
    scope: kotlinx.coroutines.CoroutineScope,
) {

    private val appContext: Context = container.appContext
    private val prediction: PredictionEngine = container.prediction
    private val calibrationStore: CalibrationFileStore = container.calibrationStore
    private val mainScope = scope
    private val layoutBuilder = LayoutBuilder()

    // -------------------------------------------------- state (Compose) --
    var settings by mutableStateOf(KeyboardSettings())
        private set

    var language by mutableStateOf(LanguageMode.FA)
        private set
    var layoutMode by mutableStateOf(LayoutMode.LETTERS)
        private set
    var shiftState by mutableStateOf(ShiftState.OFF)
        private set

    var suggestions by mutableStateOf<List<String>>(emptyList())
        private set
    var emojiSuggestions by mutableStateOf<List<String>>(emptyList())
        private set
    var showZwnjChip by mutableStateOf(false)
        private set
    var pressedKey by mutableStateOf<String?>(null)
        private set
    var popup by mutableStateOf<PopupState?>(null)
        private set
    var gestureWord by mutableStateOf<String?>(null)
        private set

    var layout by mutableStateOf<KeyboardLayout?>(null)
        private set
    var layoutHeightPx by mutableStateOf(0f)
        private set

    var isPasswordField by mutableStateOf(false)
        private set

    // -------------------------------------------------- engine internals --
    private val calibration = KeyCalibration()
    private val resolver = TouchResolver(FingerProfiles.NORMAL_PROFILE, calibration)

    private val sessions = HashMap<Long, PointerSession>()
    private var currentWord = ""
    private var prevWord: String? = null
    private val charAlts = ArrayList<List<Char>>()
    private var lastCorrection: Pair<String, String>? = null
    private var lastCorrectionTime = 0L
    private val commitTimes = ArrayDeque<Long>(8)

    private var suggestJob: Job? = null
    private var persistJob: Job? = null
    private var voiceJob: Job? = null

    private val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    // -------------------------------------------------- setup / settings --

    fun applySettings(s: KeyboardSettings) {
        settings = s
        resolver.profile = FingerProfiles.profile(s.fingerProfile)
        rebuildLayout()
    }

    fun geometryParams(density: Float): LayoutParams {
        val gapPx = (settings.gapDp / settings.keyWidthScale.coerceIn(0.8f, 1.3f)) * density
        val keyH = settings.keyHeightDp * settings.heightScale * density
        return LayoutParams(
            keyHeightPx = keyH,
            gapPx = gapPx,
            radiusPx = settings.cornerRadiusDp * density,
            sidePaddingPx = 5f * density,
        )
    }

    fun totalHeightPx(density: Float): Float {
        val p = geometryParams(density)
        return 4f * p.keyHeightPx + 3f * p.gapPx + 8f * density
    }

    fun onInputViewCreated() {
        // کالیبراسیون ذخیره‌شده را بازیابی کن
        runCatching { calibration.restore(calibrationStore.load()) }
    }

    fun buildLayout(widthPx: Float, heightPx: Float) {
        layoutHeightPx = heightPx
        val density = appContext.resources.displayMetrics.density
        val params = geometryParams(density)
        layout = layoutBuilder.build(language, layoutMode, params, widthPx, heightPx, settings.persianDigits, settings.faLayoutStyle)
    }

    private fun rebuildLayout() {
        val l = layout ?: return
        buildLayout(l.width, layoutHeightPx)
    }

    // -------------------------------------------------- lifecycle hooks --

    fun onInputViewStarted() {
        refreshShiftFromContext()
    }

    fun onInputViewFinished() {
        flushPersistence()
    }

    fun resetTransientState() {
        sessions.clear()
        pressedKey = null
        popup = null
        gestureWord = null
    }

    fun shutdown() {
        suggestJob?.cancel()
        voiceJob?.cancel()
        flushPersistence()
    }

    fun onEditorInfo(info: EditorInfo) {
        val cls = info.inputType and InputType.TYPE_CLASS_MASK
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        isPasswordField = (cls == InputType.TYPE_CLASS_TEXT && (
            variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            )) ||
            (cls == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        // در فیلدهای محرمانه: هیچ یادگیری، هیچ پیشنهادی
        if (isPasswordField) {
            suggestions = emptyList()
            emojiSuggestions = emptyList()
            showZwnjChip = false
        }
    }

    // -------------------------------------------------- haptic & sound --

    private fun haptic() {
        if (!settings.vibration || settings.hapticStrength <= 0) return
        val amp = (settings.hapticStrength * 2.55f).toInt().coerceIn(1, 255)
        runCatching { vibrator?.vibrate(VibrationEffect.createOneShot(18, amp)) }
    }

    private fun click() {
        if (settings.soundEnabled) {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.45f)
        }
    }

    // -------------------------------------------------- input connection --

    private fun ic(): InputConnection? = service.currentInputConnection

    fun commitText(text: String) {
        ic()?.commitText(text, 1)
        noteCommitTime()
    }

    private fun noteCommitTime() {
        val now = System.currentTimeMillis()
        commitTimes.addLast(now)
        while (commitTimes.size > 6) commitTimes.removeFirst()
    }

    private fun speedCps(): Float {
        if (commitTimes.size < 2) return 0f
        val span = (commitTimes.last() - commitTimes.first()) / 1000f
        return if (span <= 0.05f) 0f else (commitTimes.size - 1) / span
    }

    private fun textBefore(n: Int): String? =
        ic()?.getTextBeforeCursor(n, 0)?.toString()

    private fun deleteBack(n: Int) {
        if (n > 0) ic()?.deleteSurroundingText(n, 0)
    }

    private fun deleteWord() {
        val before = textBefore(80) ?: return
        var n = 0
        var i = before.length - 1
        while (i >= 0 && before[i].isWhitespace()) { n++; i-- }
        while (i >= 0 && !before[i].isWhitespace()) { n++; i-- }
        if (n > 0) {
            deleteBack(n)
            currentWord = ""
            showZwnjChip = false
        }
    }

    // -------------------------------------------------- editor actions --

    private fun performEnter() {
        val conn = ic() ?: return
        val info = service.currentInputEditorInfo
        val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            conn.performEditorAction(action)
        } else {
            conn.commitText("\n", 1)
        }
        onWordBoundary()
        maybeAutoCapitalize()
    }

    private fun refreshShiftFromContext() {
        if (language != LanguageMode.EN || !settings.autoCaps) return
        val before = textBefore(1)
        if (before.isNullOrEmpty() || before.last() in ".!?\n") {
            shiftState = ShiftState.ON
        }
    }

    private fun maybeAutoCapitalize() {
        if (language != LanguageMode.EN || !settings.autoCaps) return
        val before = textBefore(1)
        if (before.isNullOrEmpty() || before.last() in ".!?\n") {
            shiftState = ShiftState.ON
        }
    }

    // -------------------------------------------------- word pipeline --

    private fun isLearnable(word: String): Boolean {
        if (word.length < 2 || word.any { it.isDigit() }) return false
        if (word.contains('/') || word.contains('@') || word.contains("www")) return false
        return true
    }

    private fun preserveCase(raw: String, corrected: String): String {
        if (language != LanguageMode.EN) return corrected
        if (raw.isNotEmpty() && raw[0].isUpperCase() && corrected.isNotEmpty()) {
            return corrected.replaceFirstChar { it.uppercaseChar() }
        }
        return corrected
    }

    private fun expandShortcut(word: String) {
        val shortcuts = appContext.let { ShortcutsCache.get(appContext) }
        val match = shortcuts.firstOrNull { it.key == word } ?: return
        deleteBack(word.length)
        commitText(match.value)
    }

    private fun onWordBoundary() {
        val raw = currentWord
        charAlts.clear()
        if (raw.length >= 2 && !isPasswordField) {
            var committed = raw
            if (settings.autocorrect) {
                val correction = prediction.correct(raw, language, charAlts)
                if (correction != null && correction.confidence >= 0.55f) {
                    deleteBack(raw.length)
                    val out = preserveCase(raw, correction.corrected)
                    commitText(out)
                    lastCorrection = raw to out
                    lastCorrectionTime = System.currentTimeMillis()
                    committed = out
                }
            }
            if (isLearnable(committed)) {
                prediction.learn(committed, prevWord, language)
                schedulePersist()
            }
            emojiSuggestions = prediction.emojiFor(committed)
            suggestions = if (settings.nextWord) prediction.nextWords(committed, language) else emptyList()
            prevWord = committed
        } else {
            suggestions = emptyList()
            emojiSuggestions = emptyList()
        }
        currentWord = ""
        showZwnjChip = false
    }

    private fun requestSuggestions() {
        if (isPasswordField) return
        suggestJob?.cancel()
        val snapshot = currentWord
        showZwnjChip = language == LanguageMode.FA &&
            (PersianText.needsZwnjAfterPrefix(snapshot) || PersianText.needsZwnjBeforeSuffix(snapshot))
        if (snapshot.isEmpty()) {
            suggestions = emptyList()
            return
        }
        suggestJob = mainScope.launch(Dispatchers.Default) {
            delay(40)
            val res = prediction.suggestions(snapshot, language, prevWord)
            withContext(Dispatchers.Main) {
                if (currentWord == snapshot) suggestions = res
            }
        }
    }

    private fun schedulePersist() {
        if (persistJob?.isActive == true) return
        persistJob = mainScope.launch(Dispatchers.IO) {
            delay(20_000)
            prediction.persist()
            calibration.clearDirty()
            calibrationStore.save(calibration.toLines())
        }
    }

    private fun flushPersistence() {
        if (calibration.isDirty()) calibrationStore.save(calibration.toLines())
        mainScope.launch(Dispatchers.IO) { prediction.persist() }
    }

    // -------------------------------------------------- pointer events --

    fun onDown(pointerId: Long, x: Float, y: Float, timeMs: Long) {
        val l = layout ?: return
        if (sessions.containsKey(pointerId)) return

        val key = resolver.insideKey(x, y, l) ?: resolver.nearestKey(x, y, l)
        pressedKey = key?.key?.id
        haptic()
        click()

        val session = PointerSession(pointerId, key, x, y, timeMs)
        session.path.add(TouchPoint(x, y, timeMs, pointerId))
        sessions[pointerId] = session

        if (key == null) return

        // پاپ‌آپ برای کلیدهای دارای جایگزین و علائم نقطه
        val hasPopup = (key.key.type == KeyType.CHAR && key.key.altLabels.isNotEmpty()) ||
            key.key.id == "period"
        // رفتار نگه‌داشتن فاصله: نیم‌فاصله در فارسی / چسباندن در انگلیسی
        val spaceSpecial = key.key.type == KeyType.SPACE
        if (hasPopup || spaceSpecial) {
            session.longPressJob = mainScope.launch {
                delay(resolver.profile.longPressMs)
                if (sessions[pointerId] === session && !session.moved) {
                    if (spaceSpecial) {
                        session.mode = SessionMode.POPUP
                        onSpaceLongPress()
                    } else {
                        showKeyPopup(session, key)
                    }
                }
            }
        }
        // تکرار خودکار حذف
        if (key.key.type == KeyType.BACKSPACE) {
            session.repeatJob = mainScope.launch {
                delay(420)
                var interval = 60L
                var count = 0
                while (sessions[pointerId] === session && session.mode == SessionMode.PRESS) {
                    performBackspace(false)
                    count++
                    if (count % 8 == 0 && interval > 28) interval -= 10
                    delay(interval)
                }
            }
        }
    }

    fun onMove(pointerId: Long, x: Float, y: Float, timeMs: Long) {
        val session = sessions[pointerId] ?: return
        val l = layout ?: return
        session.lastX = x
        session.lastY = y

        val slop = 14f * appContext.resources.displayMetrics.density
        val dist = abs(x - session.startX) + abs(y - session.startY)
        if (dist > slop && session.mode == SessionMode.PRESS) {
            session.moved = true
            if (session.key?.key?.type == KeyType.BACKSPACE) {
                // کشیدن به چپ روی backspace = حذف کل واژه
                if (x < session.startX - slop * 2.2f) {
                    session.mode = SessionMode.BACKSPACE_WORD
                    session.repeatJob?.cancel()
                    haptic()
                }
                return
            }
            if (session.key?.key?.type == KeyType.CHAR &&
                layoutMode == LayoutMode.LETTERS && settings.swipeEnabled
            ) {
                session.path.add(TouchPoint(x, y, timeMs, pointerId))
                if (session.path.size > 5 && looksLikeGesture(session, l)) {
                    session.mode = SessionMode.GESTURE
                    session.longPressJob?.cancel()
                }
                return
            }
        }
        if (session.mode == SessionMode.GESTURE) {
            session.path.add(TouchPoint(x, y, timeMs, pointerId))
            return
        }
        if (session.mode == SessionMode.POPUP) {
            val p = popup
            if (p != null && y < p.panelY + p.panelH) {
                val idx = (((x - p.panelX) / (p.panelW / p.options.size)).toInt()).coerceIn(0, p.options.size - 1)
                if (idx != p.selectedIndex) {
                    p.selectedIndex = idx
                    popup = popup // trigger recompose
                    haptic()
                }
            }
            return
        }
        // هایلایت کلید زیر انگشت (انتخاب با کشیدن)
        if (session.mode == SessionMode.PRESS) {
            val under = resolver.insideKey(x, y, l)
            pressedKey = under?.key?.id ?: pressedKey
        }
    }

    fun onUp(pointerId: Long, x: Float, y: Float, timeMs: Long) {
        val session = sessions.remove(pointerId) ?: return
        session.longPressJob?.cancel()
        session.repeatJob?.cancel()
        pressedKey = null

        when (session.mode) {
            SessionMode.GESTURE -> handleGesture(session)
            SessionMode.BACKSPACE_WORD -> deleteWord()
            SessionMode.POPUP -> handlePopupRelease(session)
            SessionMode.PRESS -> {
                val l = layout
                if (l != null) {
                    val finalKey = resolver.insideKey(x, y, l) ?: session.key
                    if (finalKey != null) executeKey(finalKey, x, y)
                }
            }
        }
        popup = null
    }

    private fun looksLikeGesture(session: PointerSession, l: KeyboardLayout): Boolean {
        val pts = session.path
        if (pts.size < 6) return false
        var total = 0f
        for (i in 1 until pts.size) {
            val dx = pts[i].x - pts[i - 1].x
            val dy = pts[i].y - pts[i - 1].y
            total += kotlin.math.sqrt(dx * dx + dy * dy)
        }
        val keyW = l.keys.firstOrNull { it.key.type == KeyType.CHAR }?.width ?: 40f
        return total > keyW * 3.2f
    }

    private fun handleGesture(session: PointerSession) {
        val l = layout ?: return
        if (session.path.size < 6) return
        val centers = HashMap<Char, Pair<Float, Float>>()
        for ((c, kr) in l.charToKey) centers[c] = kr.centerX to kr.centerY
        val job = mainScope.launch(Dispatchers.Default) {
            val res = prediction.swipe(session.path, centers, language, 3)
            withContext(Dispatchers.Main) {
                if (res.isNotEmpty()) {
                    val best = res[0]
                    commitText(best.word)
                    currentWord = best.word
                    prevWord = best.word
                    gestureWord = best.word
                    suggestions = res.drop(1).map { it.word }
                    emojiSuggestions = prediction.emojiFor(best.word)
                    if (isLearnable(best.word)) {
                        prediction.learn(best.word, prevWord, language)
                        schedulePersist()
                    }
                    haptic()
                }
                mainScope.launch { delay(1200); gestureWord = null }
            }
        }
        // جلوگیری از هشدار unused: نتیجه همیشه مصرف می‌شود
        if (job.isCancelled) return
    }

    private fun handlePopupRelease(session: PointerSession) {
        val p = popup ?: return
        val idx = p.selectedIndex
        if (idx >= 0 && idx < p.outputs.size) {
            val out = p.outputs[idx]
            commitText(out)
            if (p.keyId != "period") {
                currentWord += out
                requestSuggestions()
            }
        }
    }

    private fun showKeyPopup(session: PointerSession, key: KeyRect) {
        val density = appContext.resources.displayMetrics.density
        val options: List<String>
        val outputs: List<String>
        if (key.key.id == "period") {
            options = if (language == LanguageMode.FA) listOf("؟", "!", "؛", ":", "…", "،")
            else listOf("?", "!", ";", ":", "…", ",")
            outputs = options
        } else {
            options = key.key.altLabels
            outputs = key.key.altLabels
        }
        if (options.isEmpty()) return
        val cellW = 52f * density
        val panelW = cellW * options.size
        val panelH = 52f * density
        var px = key.centerX - panelW / 2f
        px = px.coerceIn(0f, (layout?.width ?: panelW) - panelW)
        val py = (key.y - panelH - 6f * density).coerceAtLeast(0f)
        popup = PopupState(key.key.id, options, outputs, -1, px, py, panelW, panelH)
        session.mode = SessionMode.POPUP
        haptic()
    }

    private fun onSpaceLongPress() {
        if (language == LanguageMode.FA) {
            commitText(PersianText.ZWNJ)
            currentWord += PersianText.ZWNJ
            showZwnjChip = false
        } else {
            pasteClipboard()
        }
    }

    fun pasteClipboard() {
        val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val text = cm?.primaryClip?.getItemAt(0)?.text?.toString()
        if (!text.isNullOrEmpty()) {
            commitText(text)
            currentWord += text.takeLastWhile { !it.isWhitespace() }
            requestSuggestions()
        }
    }

    // -------------------------------------------------- key actions --

    private fun executeKey(key: KeyRect, touchX: Float, touchY: Float) {
        when (key.key.type) {
            KeyType.CHAR -> commitChar(key, touchX, touchY)
            KeyType.SPACE -> {
                commitText(" ")
                onWordBoundary()
                maybeAutoCapitalize()
            }
            KeyType.BACKSPACE -> performBackspace(true)
            KeyType.ENTER -> performEnter()
            KeyType.SYMBOLS -> toggleSymbols()
            KeyType.SHIFT -> {
                shiftState = when (shiftState) {
                    ShiftState.OFF -> ShiftState.ON
                    ShiftState.ON -> ShiftState.LOCK
                    ShiftState.LOCK -> ShiftState.OFF
                }
                haptic()
            }
            KeyType.LANGUAGE -> toggleLanguage()
        }
        // رفتارهای خاص با شناسه کلید
        when (key.key.id) {
            "comma" -> {
                // نگه‌داشتن کاما به‌صورت long-press مدیریت می‌شود؛ ضربه = کاراکتر
            }
        }
    }

    private fun commitChar(key: KeyRect, touchX: Float, touchY: Float) {
        var out = key.key.output ?: return
        if (language == LanguageMode.EN && shiftState != ShiftState.OFF &&
            out.length == 1 && out[0].isLetter()
        ) {
            out = out.uppercase()
            if (shiftState == ShiftState.ON) shiftState = ShiftState.OFF
        }

        // کاندیدهای لمسی این فشار برای رفع ابهام اصلاح خودکار
        val l = layout
        if (l != null && out.length == 1 && key.key.type == KeyType.CHAR) {
            val cands = resolver.resolve(touchX, touchY, l, speedCps(), 3)
            val alts = cands.mapNotNull { c ->
                val o = c.rect.key.output
                if (o != null && o.length == 1 && o[0] != out[0] &&
                    c.rect.key.type == KeyType.CHAR
                ) o[0] else null
            }
            charAlts.add(alts)

            // یادگیری کالیبراسیون از لمس مطمئن
            val top = cands.firstOrNull()
            if (top != null && top.rect.key.id == key.key.id && top.probability > 0.85f) {
                val dx = touchX - key.centerX
                val dy = touchY - key.centerY
                if (key.contains(touchX, touchY)) {
                    calibration.learn(key.key.id, dx, dy, key.width, key.height)
                }
            }
        }

        commitText(out)
        currentWord += out
        if (out == "." || out == "،" || out == "," || out == "؟" || out == "!") {
            onWordBoundary()
        } else {
            requestSuggestions()
        }
    }

    private fun performBackspace(hapticFeedback: Boolean) {
        if (hapticFeedback) haptic()
        // بازگردانی اصلاح خودکار با نخستین backspace
        val correction = lastCorrection
        if (correction != null && System.currentTimeMillis() - lastCorrectionTime < 4000) {
            val before = textBefore(correction.second.length + 1)
            if (before != null && before.endsWith(correction.second)) {
                deleteBack(correction.second.length)
                commitText(correction.first)
                currentWord = correction.first
                lastCorrection = null
                requestSuggestions()
                return
            }
        }
        lastCorrection = null
        val before = textBefore(4)
        if (before != null && before.endsWith(PersianText.ZWNJ)) {
            deleteBack(1)
        } else {
            deleteBack(1)
        }
        if (currentWord.isNotEmpty()) currentWord = currentWord.dropLast(1)
        showZwnjChip = language == LanguageMode.FA &&
            (PersianText.needsZwnjAfterPrefix(currentWord) || PersianText.needsZwnjBeforeSuffix(currentWord))
        requestSuggestions()
    }

    // -------------------------------------------------- actions from UI --

    fun toggleLanguage() {
        language = if (language == LanguageMode.FA) LanguageMode.EN else LanguageMode.FA
        if (language == LanguageMode.EN) refreshShiftFromContext() else shiftState = ShiftState.OFF
        currentWord = ""
        charAlts.clear()
        rebuildLayout()
        haptic()
        requestSuggestions()
    }

    fun toggleSymbols() {
        layoutMode = if (layoutMode == LayoutMode.LETTERS) LayoutMode.SYMBOLS else LayoutMode.LETTERS
        rebuildLayout()
        haptic()
    }

    fun onSuggestionTap(word: String) {
        val cur = currentWord
        if (cur.isNotEmpty()) deleteBack(cur.length)
        commitText(word)
        commitText(" ")
        if (isLearnable(word)) {
            prediction.learn(word, prevWord, language)
            schedulePersist()
        }
        prevWord = word
        currentWord = ""
        charAlts.clear()
        emojiSuggestions = prediction.emojiFor(word)
        suggestions = if (settings.nextWord) prediction.nextWords(word, language) else emptyList()
        showZwnjChip = false
        haptic()
    }

    fun onEmojiTap(emoji: String) {
        commitText(emoji)
    }

    fun onZwnjChipTap() {
        commitText(PersianText.ZWNJ)
        currentWord += PersianText.ZWNJ
        showZwnjChip = false
        haptic()
    }

    fun openAppSettings() {
        val intent = Intent(appContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    fun startVoiceInput() {
        if (isPasswordField) {
            toast(appContext.getString(R.string.voice_failed))
            return
        }
        val intent = Intent(appContext, com.niktype.keyboard.voice.VoiceInputActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("lang", if (language == LanguageMode.FA) "fa-IR" else "en-US")
        appContext.startActivity(intent)
        voiceJob?.cancel()
        voiceJob = mainScope.launch {
            val text = withContext(Dispatchers.Default) {
                kotlinx.coroutines.withTimeoutOrNull(120_000) { VoiceResultBus.receive() }
            }
            if (!text.isNullOrEmpty()) {
                commitText(text)
                currentWord += text.takeLastWhile { !it.isWhitespace() }
                requestSuggestions()
            }
        }
    }

    private fun toast(msg: String) {
        android.widget.Toast.makeText(appContext, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    private object ShortcutsCache {
        @Volatile
        private var cached: List<com.niktype.keyboard.data.ShortcutsStore.Shortcut>? = null
        private var loadedAt = 0L

        fun get(ctx: Context): List<com.niktype.keyboard.data.ShortcutsStore.Shortcut> {
            val now = System.currentTimeMillis()
            val c = cached
            if (c != null && now - loadedAt < 15_000) return c
            val fresh = (ctx.applicationContext as com.niktype.keyboard.NikApp).container.shortcutsStore.load()
            cached = fresh
            loadedAt = now
            return fresh
        }
    }
}
