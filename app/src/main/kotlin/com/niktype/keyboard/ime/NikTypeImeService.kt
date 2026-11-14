package com.niktype.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.niktype.keyboard.NikApp
import com.niktype.keyboard.keyboard.KeyboardController
import com.niktype.keyboard.keyboard.NikKeyboardRoot

/**
 * سرویس اصلی کیبورد NikType — ثبت‌شده در سیستم اندروید به‌عنوان InputMethod.
 */
class NikTypeImeService : InputMethodService() {

    private val lifecycleOwner = ServiceLifecycleOwner()
    private var controller: KeyboardController? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.onCreate()
    }

    override fun onDestroy() {
        controller?.shutdown()
        controller = null
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        val container = (application as NikApp).container
        val ctrl = controller ?: KeyboardController(
            service = this,
            container = container,
            scope = lifecycleOwner.lifecycleScope,
        ).also { controller = it }

        val host = ComposeKeyboardHost(this, lifecycleOwner)
        host.content = { NikKeyboardRoot(ctrl) }
        ctrl.onInputViewCreated()
        return host
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        attribute?.let { controller?.onEditorInfo(it) }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        controller?.onInputViewStarted()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        controller?.onInputViewFinished()
        super.onFinishInputView(finishingInput)
    }

    override fun onWindowHidden() {
        controller?.resetTransientState()
        super.onWindowHidden()
    }

    override fun onUpdateExtractingVisibility(ei: EditorInfo?) {
        // حالت استخراج تمام‌صفحه را غیرفعال می‌کنیم تا همیشه کیبورد خودمان دیده شود
        isExtractViewShown = false
    }
}
