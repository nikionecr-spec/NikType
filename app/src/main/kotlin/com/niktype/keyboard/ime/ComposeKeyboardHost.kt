package com.niktype.keyboard.ime

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * میزبان Compose داخل سرویس IME.
 */
class ComposeKeyboardHost(
    context: Context,
    owner: ServiceLifecycleOwner,
) : AbstractComposeView(context) {

    var content: @Composable () -> Unit = { }

    @Composable
    override fun Content() {
        content()
    }

    init {
        setViewTreeLifecycleOwner(owner)
        setViewTreeSavedStateRegistryOwner(owner)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
}
