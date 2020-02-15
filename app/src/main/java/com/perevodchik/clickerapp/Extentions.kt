package com.perevodchik.clickerapp

import android.content.Context
import android.util.Log

private const val TAG = "ClickApp"

fun Any.logwtf(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.wtf(tag, this)
    } else {
        Log.wtf(tag, this.toString())
    }
}

fun Any.logw(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.w(tag, this)
    } else {
        Log.w(tag, this.toString())
    }
}

fun Any.logv(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.v(tag, this)
    } else {
        Log.v(tag, this.toString())
    }

}
fun Any.logi(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.i(tag, this)
    } else {
        Log.i(tag, this.toString())
    }
}

fun Any.logd(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.d(tag, this)
    } else {
        Log.d(tag, this.toString())
    }
}

fun Any.loge(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.e(tag, this)
    } else {
        Log.e(tag, this.toString())
    }
}

fun Context.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Int.getInPercent(max : Int): Int {
    return (this / max) * 100
}

fun Int.getFromPercent(max: Int): Int {
    return max * this
}