package com.niktype.engine.geometry

/**
 * سازنده چیدمان‌های صفحه‌کلید فارسی و انگلیسی.
 * هر چیدمان بر اساس پارامترهای هندسی (ارتفاع کلید، فاصله، حاشیه) و
 * عرض واقعی نمایشگر ساخته می‌شود تا در هر ابعادی درست جا شود.
 */
class LayoutBuilder {

    fun build(
        language: LanguageMode,
        mode: LayoutMode,
        params: LayoutParams,
        totalWidth: Float,
        totalHeight: Float,
        persianDigits: Boolean = true,
        faStyle: FaLayoutStyle = FaLayoutStyle.STANDARD,
    ): KeyboardLayout {
        val rows: List<List<KeySpec>> = when (mode) {
            LayoutMode.LETTERS -> letterRows(language, faStyle)
            LayoutMode.SYMBOLS -> symbolRows(language, persianDigits)
        }
        val bottom = bottomRow(language, mode, faStyle)

        val gap = params.gapPx
        val kh = params.keyHeightPx
        val allRows = rows + listOf(bottom)
        val rowCount = allRows.size
        val contentH = rowCount * kh + (rowCount - 1) * gap
        val startY = ((totalHeight - contentH) / 2f).coerceAtLeast(0f)

        val keyRects = ArrayList<KeyRect>()
        val rowRects = ArrayList<List<KeyRect>>()
        for ((ri, row) in allRows.withIndex()) {
            val y = startY + ri * (kh + gap)
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val available = totalWidth - 2f * params.sidePaddingPx - (row.size - 1) * gap
            val unit = (available / totalWeight).coerceAtLeast(0f)
            var x = params.sidePaddingPx
            val rects = ArrayList<KeyRect>(row.size)
            for (key in row) {
                val w = unit * key.weight
                rects.add(KeyRect(key, x, y, w, kh))
                keyRects.add(rects.last())
                x += w + gap
            }
            rowRects.add(rects)
        }

        val layoutId = "${language.name}_${mode.name}_${if (language == LanguageMode.FA) faStyle.name else "QWERTY"}"
        return KeyboardLayout(
            id = layoutId,
            language = language,
            mode = mode,
            width = totalWidth,
            height = totalHeight,
            rows = rowRects,
            keys = keyRects,
        )
    }

    // ------------------------------------------------------------------
    // ردیف‌های حروف
    // ------------------------------------------------------------------

    private fun letterRows(language: LanguageMode, faStyle: FaLayoutStyle): List<List<KeySpec>> = when (language) {
        LanguageMode.FA -> when (faStyle) {
            FaLayoutStyle.STANDARD -> faStandardRows()
            FaLayoutStyle.ALPHABETIC -> faAlphabeticRows()
        }
        LanguageMode.EN -> englishRows()
    }

    private fun faStandardRows(): List<List<KeySpec>> = listOf(
        listOf(
            faKey("ض"), faKey("ص"), faKey("ث"), faKey("ق"), faKey("ف"),
            faKey("غ"), faKey("ع"), faKey("ه", alts = listOf("ة", "هٔ")), faKey("خ"),
            faKey("ح"), faKey("ج"), faKey("چ"),
        ),
        listOf(
            faKey("ش"), faKey("س"), faKey("ی", alts = listOf("ي", "ئ")), faKey("ب"),
            faKey("ل"), faKey("ا", alts = listOf("آ", "أ", "إ")), faKey("ت"), faKey("ن"),
            faKey("م"), faKey("ک", alts = listOf("ك")), faKey("گ"),
        ),
        listOf(
            faKey("ظ"), faKey("ط"), faKey("ز"), faKey("ر"), faKey("ذ"),
            faKey("د"), faKey("پ"), faKey("و", alts = listOf("ؤ")), faKey("ژ"),
            faKey("؟", output = "؟"),
            backspaceKey(1.5f),
        ),
    )

    private fun faAlphabeticRows(): List<List<KeySpec>> = listOf(
        listOf(
            faKey("ا", alts = listOf("آ", "أ", "إ")), faKey("ب"), faKey("پ"), faKey("ت"),
            faKey("ث"), faKey("ج"), faKey("چ"), faKey("ح"), faKey("خ"), faKey("د"),
            faKey("ذ"),
        ),
        listOf(
            faKey("ر"), faKey("ز"), faKey("ژ"), faKey("س"), faKey("ش"), faKey("ص"),
            faKey("ض"), faKey("ط"), faKey("ظ"), faKey("ع"), faKey("غ"),
        ),
        listOf(
            faKey("ف"), faKey("ق"), faKey("ک", alts = listOf("ك")), faKey("گ"),
            faKey("ل"), faKey("م"), faKey("ن"), faKey("و", alts = listOf("ؤ")),
            faKey("ه", alts = listOf("ة", "هٔ")), faKey("ی", alts = listOf("ي", "ئ")),
        ),
    )

    private fun englishRows(): List<List<KeySpec>> = listOf(
        listOf(
            enKey("q"), enKey("w"), enKey("e", alts = listOf("é", "è", "ê", "ë")),
            enKey("r"), enKey("t"), enKey("y"), enKey("u", alts = listOf("û", "ü", "ù", "ú")),
            enKey("i", alts = listOf("î", "ï", "í", "ì")), enKey("o", alts = listOf("ô", "ö", "ò", "ó")),
            enKey("p"),
        ),
        listOf(
            enKey("a", alts = listOf("à", "â", "á", "ã")), enKey("s"), enKey("d"),
            enKey("f"), enKey("g"), enKey("h"), enKey("j"), enKey("k"),
            enKey("l"),
        ),
        listOf(
            shiftKey(1.6f),
            enKey("z"), enKey("x"), enKey("c", alts = listOf("ç")), enKey("v"),
            enKey("b"), enKey("n", alts = listOf("ñ")), enKey("m"),
            backspaceKey(1.6f),
        ),
    )

    // ------------------------------------------------------------------
    // لایه علائم و اعداد
    // ------------------------------------------------------------------

    private fun symbolRows(language: LanguageMode, persianDigits: Boolean): List<List<KeySpec>> {
        val digits = if (language == LanguageMode.FA && persianDigits) {
            listOf('۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹', '۰')
        } else {
            listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
        }
        val digitRow = digits.map { d ->
            KeySpec(id = "digit_$d", label = d.toString(), output = d.toString(), weight = 1f)
        } + backspaceKey(1.5f)

        val secondRow = if (language == LanguageMode.FA) {
            listOf("@", "#", "٪", "$", "_", "&", "-", "+", "(", ")")
        } else {
            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
        }.map { KeySpec(id = "sym_$it", label = it, output = it, weight = 1f) }

        val thirdRow = if (language == LanguageMode.FA) {
            listOf(
                KeySpec("sym_!", "!", "!"), KeySpec("sym_quote", "\"", "\""),
                KeySpec("sym_apos", "'", "'"), KeySpec("sym_colon", ":", ":"),
                KeySpec("sym_semi", ";", ";"), KeySpec("sym_qm_fa", "؟", "؟"),
                KeySpec("sym_tilde", "~", "~"), KeySpec("sym_star", "*", "*"),
                KeySpec("sym_eq", "=", "="),
                KeySpec(ZWNJ_ID, "نیم‌", "\u200C"),
            )
        } else {
            listOf(
                KeySpec("sym_!", "!", "!"), KeySpec("sym_quote", "\"", "\""),
                KeySpec("sym_apos", "'", "'"), KeySpec("sym_colon", ":", ":"),
                KeySpec("sym_semi", ";", ";"), KeySpec("sym_qm", "?", "?"),
                KeySpec("sym_tilde", "~", "~"), KeySpec("sym_star", "*", "*"),
                KeySpec("sym_eq", "=", "="), KeySpec("sym_lt", "<", "<"),
            )
        }

        return listOf(digitRow, secondRow, thirdRow)
    }

    // ------------------------------------------------------------------
    // ردیف پایین
    // ------------------------------------------------------------------

    private fun bottomRow(language: LanguageMode, mode: LayoutMode, faStyle: FaLayoutStyle): List<KeySpec> {
        val backspaceInBottom = language == LanguageMode.FA && faStyle == FaLayoutStyle.ALPHABETIC
        val spaceWeight = if (backspaceInBottom) 3.4f else 4.2f
        val comma = if (language == LanguageMode.FA) "،" else ","
        val keys = ArrayList<KeySpec>()
        keys.add(
            KeySpec(
                id = if (mode == LayoutMode.LETTERS) "symbols" else "letters",
                label = if (mode == LayoutMode.LETTERS) {
                    if (language == LanguageMode.FA) "۱۲۳" else "123"
                } else {
                    if (language == LanguageMode.FA) "حروف" else "ABC"
                },
                type = KeyType.SYMBOLS,
                weight = 1.3f,
            )
        )
        keys.add(KeySpec(id = "comma", label = comma, output = comma, weight = 1.1f))
        keys.add(KeySpec(id = "space", label = "", type = KeyType.SPACE, output = " ", weight = spaceWeight))
        keys.add(KeySpec(id = "period", label = ".", output = ".", weight = 1.1f))
        if (backspaceInBottom) keys.add(backspaceKey(1.5f))
        keys.add(KeySpec(id = "enter", label = "", type = KeyType.ENTER, weight = 1.6f))
        return keys
    }

    // ------------------------------------------------------------------
    // سازنده‌های کمکی
    // ------------------------------------------------------------------

    private fun faKey(char: String, alts: List<String> = emptyList()) =
        KeySpec(id = char, label = char, output = char, altLabels = alts, weight = 1f)

    private fun enKey(char: String, alts: List<String> = emptyList()) =
        KeySpec(id = char, label = char, output = char, altLabels = alts, weight = 1f)

    private fun backspaceKey(weight: Float) =
        KeySpec(id = "backspace", label = "", type = KeyType.BACKSPACE, weight = weight)

    private fun shiftKey(weight: Float) =
        KeySpec(id = "shift", label = "", type = KeyType.SHIFT, weight = weight)

    companion object {
        const val ZWNJ_ID = "zwnj"
        const val ZWNJ_CHAR = "\u200C"
    }
}
