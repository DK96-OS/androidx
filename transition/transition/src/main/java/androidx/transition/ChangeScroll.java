/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This transition captures the scroll properties of targets before and after
 * the scene change and animates any changes.
 */
public class ChangeScroll extends Transition {

    private static final String PROPNAME_SCROLL_X = "android:changeScroll:x";
    private static final String PROPNAME_SCROLL_Y = "android:changeScroll:y";

    private static final String[] PROPERTIES = {
            PROPNAME_SCROLL_X,
            PROPNAME_SCROLL_Y,
    };

    public ChangeScroll() {}

    public ChangeScroll(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void captureStartValues(@NonNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(@NonNull TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public boolean isSeekingSupported() {
        return true;
    }

    @Override
    public String @Nullable [] getTransitionProperties() {
        return PROPERTIES;
    }

    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_SCROLL_X, transitionValues.view.getScrollX());
        transitionValues.values.put(PROPNAME_SCROLL_Y, transitionValues.view.getScrollY());
    }

    @Override
    public @Nullable Animator createAnimator(@NonNull ViewGroup sceneRoot,
            @Nullable TransitionValues startValues, @Nullable TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        final View view = endValues.view;
        int startX = (Integer) startValues.values.get(PROPNAME_SCROLL_X);
        int endX = (Integer) endValues.values.get(PROPNAME_SCROLL_X);
        int startY = (Integer) startValues.values.get(PROPNAME_SCROLL_Y);
        int endY = (Integer) endValues.values.get(PROPNAME_SCROLL_Y);
        Animator scrollXAnimator = null;
        Animator scrollYAnimator = null;
        if (startX != endX) {
            view.setScrollX(startX);
            scrollXAnimator = ObjectAnimator.ofInt(view, "scrollX", startX, endX);
        }
        if (startY != endY) {
            view.setScrollY(startY);
            scrollYAnimator = ObjectAnimator.ofInt(view, "scrollY", startY, endY);
        }
        return TransitionUtils.mergeAnimators(scrollXAnimator, scrollYAnimator);
    }

}
