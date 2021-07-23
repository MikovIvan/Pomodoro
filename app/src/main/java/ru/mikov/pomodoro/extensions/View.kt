package ru.mikov.pomodoro.extensions

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView

fun ImageView.startAnimation(objAnimator: ObjectAnimator) {
    this.visible()
    objAnimator.apply {
        duration = 1000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = Animation.INFINITE
    }.start()
}

fun ImageView.stopAnimation(objAnimator: ObjectAnimator) {
    this.invisible()
    objAnimator.cancel()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}