/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ink.geometry

import androidx.annotation.FloatRange
import androidx.annotation.RestrictTo
import androidx.ink.nativeloader.UsedByNative
import kotlin.math.atan2

/**
 * Mutable parallelogram (i.e. a quadrilateral with parallel sides), defined by its [center],
 * [width], [height], [rotation], and [shearFactor].
 *
 * @constructor Create the [MutableParallelogram] from an existing [MutableVec] instance and
 *   primitive [Float] parameters. Note that this instances will become the internal state of this
 *   [MutableParallelogram], so modifications made to it directly or through setters on this
 *   [MutableParallelogram] will modify the input [MutableVec] instances too. This is to allow
 *   performance-critical code to avoid any unnecessary allocations. This can be tricky to manage,
 *   especially in multithreaded code, so when calling code is unable to guarantee ownership of the
 *   nested mutable data at a particular time, it may be safest to construct this with a copy of the
 *   data to give this [MutableSegment] exclusive ownership of that copy.
 */
public class MutableParallelogram
private constructor(
    override var center: MutableVec,
    width: Float,
    override var height: Float,
    @AngleRadiansFloat rotation: Float,
    override var shearFactor: Float,
) : Parallelogram() {

    @AngleRadiansFloat private var _rotation: Float = Angle.normalized(rotation)

    override var rotation: Float
        @AngleRadiansFloat get() = _rotation
        set(@AngleRadiansFloat value) {
            _rotation = Angle.normalized(value)
        }

    private var _width: Float = width
    override var width: Float
        @FloatRange(from = 0.0) get() = _width
        set(@FloatRange(from = 0.0) value) {
            // A [Parallelogram] may *not* have a negative width. If an operation is performed on
            // [Parallelogram] resulting
            // in a negative width, it will be normalized.
            normalizeAndRun(value, height, rotation) { w: Float, h: Float, r: Float ->
                _width = w
                height = h
                rotation = r
                this@MutableParallelogram
            }
        }

    public constructor() : this(MutableVec(), 0f, 0f, Angle.ZERO, 0f)

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // NonPublicApi
    @UsedByNative
    public fun setCenterDimensionsRotationAndShear(
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        @AngleRadiansFloat rotation: Float,
        shearFactor: Float,
    ): Unit = run {
        normalizeAndRun(width, height, rotation) { w: Float, h: Float, r: Float ->
            this.width = w
            this.height = h
            this.rotation = r
            this.shearFactor = shearFactor
            this.center.x = centerX
            this.center.y = centerY
            this
        }
    }

    /** Fills this [MutableParallelogram] with the same values contained in [input]. */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // PublicApiNotReadyForJetpackReview
    public fun populateFrom(input: Parallelogram): MutableParallelogram {
        center.x = input.center.x
        center.y = input.center.y
        width = input.width
        height = input.height
        rotation = input.rotation
        shearFactor = input.shearFactor
        return this
    }

    override fun equals(other: Any?): Boolean =
        other === this || (other is Parallelogram && Parallelogram.areEquivalent(this, other))

    // NOMUTANTS -- not testing exact hashCode values, just that equality implies same hashCode
    override fun hashCode(): Int = hash(this)

    override fun toString(): String = "Mutable${string(this)}"

    public companion object {

        /**
         * Constructs a [MutableParallelogram] with a given [center], [width] and [height]. The
         * resulting [Parallelogram] has zero [rotation] and [shearFactor]. If the [width] is less
         * than zero, the [Parallelogram] will be normalized.
         */
        @JvmStatic
        public fun fromCenterAndDimensions(
            center: MutableVec,
            @FloatRange(from = 0.0) width: Float,
            height: Float,
        ): MutableParallelogram =
            normalizeAndRun(width, height, rotation = Angle.ZERO) { w: Float, h: Float, r: Float ->
                MutableParallelogram(center, w, h, r, shearFactor = 0f)
            }

        /**
         * Constructs a [MutableParallelogram] with a given [center], [width], [height] and
         * [rotation]. The resulting [Parallelogram] has zero [shearFactor]. If the [width] is less
         * than zero or if the [rotation] is not in the range [0, 2π), the [Parallelogram] will be
         * normalized.
         */
        @JvmStatic
        public fun fromCenterDimensionsAndRotation(
            center: MutableVec,
            @FloatRange(from = 0.0) width: Float,
            height: Float,
            @AngleRadiansFloat rotation: Float,
        ): MutableParallelogram =
            normalizeAndRun(width, height, rotation) { w: Float, h: Float, r: Float ->
                MutableParallelogram(center, w, h, r, shearFactor = 0f)
            }

        /**
         * Constructs a [MutableParallelogram] with a given [center], [width], [height], [rotation]
         * and [shearFactor]. If the [width] is less than zero or if the [rotation] is not in the
         * range [0, 2π), the [Parallelogram] will be normalized.
         */
        @JvmStatic
        public fun fromCenterDimensionsRotationAndShear(
            center: MutableVec,
            @FloatRange(from = 0.0) width: Float,
            height: Float,
            @AngleRadiansFloat rotation: Float,
            shearFactor: Float,
        ): MutableParallelogram =
            normalizeAndRun(width, height, rotation) { w: Float, h: Float, r: Float ->
                MutableParallelogram(center, w, h, r, shearFactor)
            }

        /**
         * Constructs a [MutableParallelogram] that is aligned with the [segment] and whose bounds
         * are [padding] units away from the segment and whose [shear] is zero.
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // PublicApiNotReadyForJetpackReview
        @JvmStatic
        public fun fromSegmentAndPadding(segment: Segment, padding: Float): MutableParallelogram =
            normalizeAndRun(
                width = segment.computeLength() + 2 * padding,
                height = 2 * padding,
                rotation =
                    atan2((segment.start.y - segment.end.y), (segment.start.x - segment.end.x)),
            ) { w: Float, h: Float, r: Float ->
                MutableParallelogram(
                    center =
                        MutableVec(
                            segment.end.x / 2f + segment.start.x / 2f,
                            segment.end.y / 2f + segment.start.y / 2f,
                        ),
                    width = w,
                    height = h,
                    rotation = r,
                    shearFactor = 0f,
                )
            }
    }
}
