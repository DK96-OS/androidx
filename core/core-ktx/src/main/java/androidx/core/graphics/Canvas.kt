/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.core.graphics

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF

/** Wrap the specified [block] in calls to [Canvas.save] and [Canvas.restoreToCount]. */
public inline fun Canvas.withSave(block: Canvas.() -> Unit) {
    val checkpoint = save()
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.translate] and
 * [Canvas.restoreToCount].
 */
public inline fun Canvas.withTranslation(
    x: Float = 0.0f,
    y: Float = 0.0f,
    block: Canvas.() -> Unit
) {
    val checkpoint = save()
    translate(x, y)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.rotate] and [Canvas.restoreToCount].
 */
public inline fun Canvas.withRotation(
    degrees: Float = 0.0f,
    pivotX: Float = 0.0f,
    pivotY: Float = 0.0f,
    block: Canvas.() -> Unit
) {
    val checkpoint = save()
    rotate(degrees, pivotX, pivotY)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.scale] and [Canvas.restoreToCount].
 */
public inline fun Canvas.withScale(
    x: Float = 1.0f,
    y: Float = 1.0f,
    pivotX: Float = 0.0f,
    pivotY: Float = 0.0f,
    block: Canvas.() -> Unit
) {
    val checkpoint = save()
    scale(x, y, pivotX, pivotY)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.skew] and [Canvas.restoreToCount].
 */
public inline fun Canvas.withSkew(x: Float = 0.0f, y: Float = 0.0f, block: Canvas.() -> Unit) {
    val checkpoint = save()
    skew(x, y)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.concat] and [Canvas.restoreToCount].
 */
public inline fun Canvas.withMatrix(matrix: Matrix = Matrix(), block: Canvas.() -> Unit) {
    val checkpoint = save()
    concat(matrix)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.clipRect] and
 * [Canvas.restoreToCount].
 */
public inline fun Canvas.withClip(clipRect: Rect, block: Canvas.() -> Unit) {
    val checkpoint = save()
    clipRect(clipRect)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.clipRect] and
 * [Canvas.restoreToCount].
 */
public inline fun Canvas.withClip(clipRect: RectF, block: Canvas.() -> Unit) {
    val checkpoint = save()
    clipRect(clipRect)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.clipRect] and
 * [Canvas.restoreToCount].
 */
public inline fun Canvas.withClip(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    block: Canvas.() -> Unit
) {
    val checkpoint = save()
    clipRect(left, top, right, bottom)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.clipRect] and
 * [Canvas.restoreToCount].
 */
public inline fun Canvas.withClip(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    block: Canvas.() -> Unit
) {
    val checkpoint = save()
    clipRect(left, top, right, bottom)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}

/**
 * Wrap the specified [block] in calls to [Canvas.save]/[Canvas.clipPath] and
 * [Canvas.restoreToCount].
 */
public inline fun Canvas.withClip(clipPath: Path, block: Canvas.() -> Unit) {
    val checkpoint = save()
    clipPath(clipPath)
    try {
        block()
    } finally {
        restoreToCount(checkpoint)
    }
}
