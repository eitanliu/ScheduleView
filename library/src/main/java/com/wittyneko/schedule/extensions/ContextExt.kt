package com.wittyneko.schedule.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import kotlin.math.roundToInt


/**
 * Desc: Context 扩展
 * <p>
 * Date: 2020-07-16
 * Copyright: Copyright (c) 2010 - 2020
 * Updater:
 * Update Time:
 * Update Comments:
 *
 * Author: wittyneko
 */
class ContextExt

val Context.isAppForeground: Boolean
    get() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        @Suppress("DEPRECATION")
        val tasks = am.getRunningTasks(1)
        if (tasks.isNotEmpty()) {
            val topActivity = tasks[0].topActivity
            if (topActivity?.packageName == packageName) {
                return true
            }
        }
        return false
    }

val Activity.activity get() = this

//inline fun <reified T: Activity> Context.intentFor() = Intent(this, T::class.java)

fun Context.color(@ColorRes id: Int, theme: Resources.Theme? = null) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(id, theme)
    } else {
        @Suppress("DEPRECATION")
        resources.getColor(id)
    }

fun Resources.color(@ColorRes id: Int, theme: Resources.Theme? = null) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(id, theme)
    } else {
        @Suppress("DEPRECATION")
        getColor(id)
    }

@SuppressLint("UseCompatLoadingForDrawables")
fun Context.drawable(@DrawableRes id: Int, theme: Resources.Theme? = null) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getDrawable(id, theme)
    } else {
        @Suppress("DEPRECATION")
        resources.getDrawable(id)
    }

@SuppressLint("UseCompatLoadingForDrawables")
fun Resources.drawable(@DrawableRes id: Int, theme: Resources.Theme? = null) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getDrawable(id, theme)
    } else {
        @Suppress("DEPRECATION")
        getDrawable(id)
    }

fun Resources.dp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value, this.displayMetrics
)

fun Resources.dp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value.toFloat(), this.displayMetrics
)

fun Resources.idp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value, this.displayMetrics
).roundToInt()

fun Resources.idp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value.toFloat(), this.displayMetrics
).roundToInt()

fun Resources.sp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    value, this.displayMetrics
)

fun Resources.sp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    value.toFloat(), this.displayMetrics
)

fun Resources.isp(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    value, this.displayMetrics
).roundToInt()

fun Resources.isp(value: Int) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    value.toFloat(), this.displayMetrics
).roundToInt()