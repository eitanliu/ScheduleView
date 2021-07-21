@file:Suppress("NOTHING_TO_INLINE")

package com.wittyneko.schedule.extensions

import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

class ViewExt

@Suppress("UNCHECKED_CAST")
inline fun <T : ViewGroup.LayoutParams> View.asLayoutParams() = layoutParams as T

@Suppress("UNCHECKED_CAST")
inline fun <T : View> View.parentAsView() = parent as T

@Suppress("UNCHECKED_CAST")
inline fun <T : View> View.asView() = this as T


/**
 * 获得 View 相对 父View 的坐标
 */
inline val View.frameOnParent get() = Rect(left, top, right, bottom)

/**
 * 获取控件 相对 窗口Window 的坐标
 */
inline val View.frameOnWindow
    get() = Rect().also {
        val location = locationOnWindow
        it.left = location[0]
        it.top = location[1]
        it.right = it.left + width
        it.bottom = it.top + height
    }

/**
 * 获得 View 相对 屏幕 的绝对坐标
 */
inline val View.frameOnScreen
    get() = Rect().also {
        val location = locationOnScreen
        it.left = location[0]
        it.top = location[1]
        it.right = it.left + width
        it.bottom = it.top + height
    }

/**
 * 获得 View 相对 父View 的坐标
 */
inline val View.locationOnParent get() = IntArray(2).also { it[0] = left; it[1] = width }

/**
 * 获取控件 相对 窗口Window 的坐标
 */
inline val View.locationOnWindow get() = IntArray(2).also { getLocationInWindow(it) }

/**
 * 获得 View 相对 屏幕 的绝对坐标
 */
inline val View.locationOnScreen get() = IntArray(2).also { getLocationOnScreen(it) }

/**
 * 获得 View 相对 Surface 的坐标
 */
inline val View.locationOnSurface
    @RequiresApi(Build.VERSION_CODES.Q)
    get() = IntArray(2).also { getLocationInSurface(it) }

/**
 * View可见部分 相对于 屏幕的坐标
 */
inline val View.visibleOnScreen get() = Rect().also { getGlobalVisibleRect(it) }

/**
 * View可见部分 相对于 屏幕的坐标
 * Desc:
 * <p>
 * Author: wittyneko
 * Date: 2020-07-21
 * @receiver View
 * @param offset Point 偏移量
 * @return Rect
 */
inline fun View.visibleOnScreen(offset: Point) = Rect().also { getGlobalVisibleRect(it, offset) }

/**
 * View可见部分 相对于 自身View位置左上角的坐标
 */
inline val View.visibleOnSelf get() = Rect().also { getLocalVisibleRect(it) }
