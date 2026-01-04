package com.bottlr.app.util

import android.content.res.Resources
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

private const val DRAWER_WIDTH_DP = 280f
private const val ANIMATION_DURATION_MS = 300L

class NavDrawerHelper(
    private val navWindow: View,
    private val navScrim: View? = null,
    private val resources: Resources
) {
    private var isOpen = false

    fun toggle() {
        if (isOpen) close() else open()
    }

    fun open() {
        navScrim?.let {
            it.visibility = View.VISIBLE
            it.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_MS)
                .start()
        }

        navWindow.animate()
            .translationX(0f)
            .setDuration(ANIMATION_DURATION_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        isOpen = true
    }

    fun close() {
        navScrim?.animate()
            ?.alpha(0f)
            ?.setDuration(ANIMATION_DURATION_MS)
            ?.withEndAction { navScrim.visibility = View.GONE }
            ?.start()

        val offsetPx = -DRAWER_WIDTH_DP * resources.displayMetrics.density
        navWindow.animate()
            .translationX(offsetPx)
            .setDuration(ANIMATION_DURATION_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        isOpen = false
    }

    val isDrawerOpen: Boolean get() = isOpen
}
