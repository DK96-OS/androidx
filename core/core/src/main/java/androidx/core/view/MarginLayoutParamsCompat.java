/*
 * Copyright 2018 The Android Open Source Project
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


package androidx.core.view;

import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.NonNull;

/**
 * Helper for accessing API features in
 * {@link ViewGroup.MarginLayoutParams MarginLayoutParams} in a backwards compatible
 * way.
 *
 * @deprecated Use {@link ViewGroup.MarginLayoutParams} directly.
 */
@Deprecated
public final class MarginLayoutParamsCompat {
    /**
     * Get the relative starting margin that was set.
     *
     * <p>On platform versions supporting bidirectional text and layouts
     * this value will be resolved into the LayoutParams object's left or right
     * margin as appropriate when the associated View is attached to a window
     * or when the layout direction of that view changes.</p>
     *
     * @param lp LayoutParams to query
     * @return the margin along the starting edge in pixels
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#getMarginStart} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.getMarginStart()")
    @Deprecated
    public static int getMarginStart(ViewGroup.@NonNull MarginLayoutParams lp) {
        return lp.getMarginStart();
    }

    /**
     * Get the relative ending margin that was set.
     *
     * <p>On platform versions supporting bidirectional text and layouts
     * this value will be resolved into the LayoutParams object's left or right
     * margin as appropriate when the associated View is attached to a window
     * or when the layout direction of that view changes.</p>
     *
     * @param lp LayoutParams to query
     * @return the margin along the ending edge in pixels
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#getMarginStart} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.getMarginEnd()")
    @Deprecated
    public static int getMarginEnd(ViewGroup.@NonNull MarginLayoutParams lp) {
        return lp.getMarginEnd();
    }

    /**
     * Set the relative start margin.
     *
     * <p>On platform versions supporting bidirectional text and layouts
     * this value will be resolved into the LayoutParams object's left or right
     * margin as appropriate when the associated View is attached to a window
     * or when the layout direction of that view changes.</p>
     *
     * @param lp LayoutParams to query
     * @param marginStart the desired start margin in pixels
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#setMarginStart} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.setMarginStart(marginStart)")
    @Deprecated
    public static void setMarginStart(ViewGroup.@NonNull MarginLayoutParams lp, int marginStart) {
        lp.setMarginStart(marginStart);
    }

    /**
     * Set the relative end margin.
     *
     * <p>On platform versions supporting bidirectional text and layouts
     * this value will be resolved into the LayoutParams object's left or right
     * margin as appropriate when the associated View is attached to a window
     * or when the layout direction of that view changes.</p>
     *
     * @param lp LayoutParams to query
     * @param marginEnd the desired end margin in pixels
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#setMarginEnd} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.setMarginEnd(marginEnd)")
    @Deprecated
    public static void setMarginEnd(ViewGroup.@NonNull MarginLayoutParams lp, int marginEnd) {
        lp.setMarginEnd(marginEnd);
    }

    /**
     * Check if margins are relative.
     *
     * @return true if either marginStart or marginEnd has been set.
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#isMarginRelative} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.isMarginRelative()")
    @Deprecated
    public static boolean isMarginRelative(ViewGroup.@NonNull MarginLayoutParams lp) {
        return lp.isMarginRelative();
    }

    /**
     * Returns the layout direction. Can be either {@link ViewCompat#LAYOUT_DIRECTION_LTR} or
     * {@link ViewCompat#LAYOUT_DIRECTION_RTL}.
     *
     * @return the layout direction.
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#getLayoutDirection} directly.
     */
    @Deprecated
    public static int getLayoutDirection(ViewGroup.@NonNull MarginLayoutParams lp) {
        int result;
        result = lp.getLayoutDirection();

        if ((result != View.LAYOUT_DIRECTION_LTR)
                && (result != View.LAYOUT_DIRECTION_RTL)) {
            // This can happen on older platform releases where the default (unset) layout direction
            // is -1
            result = View.LAYOUT_DIRECTION_LTR;
        }
        return result;
    }

    /**
     * Set the layout direction.
     *
     * @param lp LayoutParameters for which to set the layout direction.
     * @param layoutDirection the layout direction.
     *        Should be either {@link ViewCompat#LAYOUT_DIRECTION_LTR}
     *                     or {@link ViewCompat#LAYOUT_DIRECTION_RTL}.
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#setLayoutDirection} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.setLayoutDirection(layoutDirection)")
    @Deprecated
    public static void setLayoutDirection(ViewGroup.@NonNull MarginLayoutParams lp,
            int layoutDirection) {
        lp.setLayoutDirection(layoutDirection);
    }

    /**
     * This will be called by {@link View#requestLayout()}. Left and Right margins
     * may be overridden depending on layout direction.
     *
     * @deprecated Use {@link ViewGroup.MarginLayoutParams#resolveLayoutDirection} directly.
     */
    @androidx.annotation.ReplaceWith(expression = "lp.resolveLayoutDirection(layoutDirection)")
    @Deprecated
    public static void resolveLayoutDirection(ViewGroup.@NonNull MarginLayoutParams lp,
            int layoutDirection) {
        lp.resolveLayoutDirection(layoutDirection);
    }

    private MarginLayoutParamsCompat() {
    }

}
