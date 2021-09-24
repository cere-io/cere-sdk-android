package io.cere.cere_sdk.utils

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.os.Build
import android.util.Log
import android.view.animation.LinearInterpolator


class AnimationHelper {

    companion object {
        private const val TAG = "AnimationHelper"
        private const val INITIAL_VALUE_DEFAULT = 0f
        private const val TARGET_VALUE_DEFAULT = 1f
        private const val DURATION_MS_DEFAULT = 1000L
    }

    private var valueAnimator: ValueAnimator? = null

    fun startTimingEngineOfFloat(
        initialValue: Float = INITIAL_VALUE_DEFAULT,
        targetValue: Float = TARGET_VALUE_DEFAULT,
        durationMs: Long = DURATION_MS_DEFAULT,
        updateCallback: (Float) -> Unit
    ) {
        valueAnimator =
            ValueAnimator
                .ofFloat(initialValue, targetValue)
                .apply {
                    // Issue on samsung
                    //https://stackoverflow.com/questions/25505622/androids-objectanimator-offloat-doesnt-work-properly
                    if (Build.MANUFACTURER?.contentEquals("samsung") == true) {
                        try {
                            this::class.java
                                .getMethod(
                                    "setDurationScale",
                                    Float::class.javaPrimitiveType
                                )
                                .invoke(null, 1F)
                        } catch (throwable: Throwable) {
                            Log.e(TAG, "samsung animation issue", throwable)
                        }
                    }
                    interpolator = LinearInterpolator()
                    repeatCount = INFINITE
                    duration = durationMs
                    addUpdateListener { animation: ValueAnimator ->
                        (animation.animatedValue as? Float)?.let {
                            updateCallback(it)
                        }
                    }
                    start()
                }
    }

    fun stopAnimation() {
        valueAnimator?.run {
            removeAllUpdateListeners()
            end()
        }
        valueAnimator = null
    }
}