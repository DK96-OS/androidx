/*
 * Copyright (C) 2013 The Android Open Source Project
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

package androidx.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.UiThread;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * ContentLoadingProgressBar implements a ProgressBar that waits a minimum time to be
 * dismissed before showing. Once visible, the progress bar will be visible for
 * a minimum amount of time to avoid "flashes" in the UI when an event could take
 * a largely variable time to complete (from none, to a user perceivable amount).
 */
public class ContentLoadingProgressBar extends ProgressBar {
    private static final int MIN_SHOW_TIME_MS = 500;
    private static final int MIN_DELAY_MS = 500;

    // These fields should only be accessed on the UI thread.
    long mStartTime = -1;
    boolean mPostedHide = false;
    boolean mPostedShow = false;
    boolean mDismissed = false;

    private final Runnable mDelayedHide = () -> {
        mPostedHide = false;
        mStartTime = -1;
        setVisibility(View.GONE);
    };

    private final Runnable mDelayedShow = () -> {
        mPostedShow = false;
        if (!mDismissed) {
            mStartTime = System.currentTimeMillis();
            setVisibility(View.VISIBLE);
        }
    };

    public ContentLoadingProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public ContentLoadingProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
    }

    private void removeCallbacks() {
        removeCallbacks(mDelayedHide);
        removeCallbacks(mDelayedShow);
    }

    /**
     * Hide the progress view if it is visible. The progress view will not be
     * hidden until it has been shown for at least a minimum show time. If the
     * progress view was not yet visible, cancels showing the progress view.
     * <p>
     * This method may be called off the UI thread.
     */
    public void hide() {
        // This method used to be synchronized, presumably so that it could be safely called off
        // the UI thread; however, the referenced fields were still accessed both on and off the
        // UI thread, e.g. not thread-safe. Now we hand-off everything to the UI thread.
        post(this::hideOnUiThread);
    }

    @UiThread
    private void hideOnUiThread() {
        mDismissed = true;
        removeCallbacks(mDelayedShow);
        mPostedShow = false;
        long diff = System.currentTimeMillis() - mStartTime;
        if (diff >= MIN_SHOW_TIME_MS || mStartTime == -1) {
            // The progress spinner has been shown long enough
            // OR was not shown yet. If it wasn't shown yet,
            // it will just never be shown.
            setVisibility(View.GONE);
        } else {
            // The progress spinner is shown, but not long enough,
            // so put a delayed message in to hide it when its been
            // shown long enough.
            if (!mPostedHide) {
                postDelayed(mDelayedHide, MIN_SHOW_TIME_MS - diff);
                mPostedHide = true;
            }
        }
    }

    /**
     * Show the progress view after waiting for a minimum delay. If
     * during that time, hide() is called, the view is never made visible.
     * <p>
     * This method may be called off the UI thread.
     */
    public void show() {
        // This method used to be synchronized, presumably so that it could be safely called off
        // the UI thread; however, the referenced fields were still accessed both on and off the
        // UI thread, e.g. not thread-safe. Now we hand-off everything to the UI thread.
        post(this::showOnUiThread);
    }

    @UiThread
    private void showOnUiThread() {
        // Reset the start time.
        mStartTime = -1;
        mDismissed = false;
        removeCallbacks(mDelayedHide);
        mPostedHide = false;
        if (!mPostedShow) {
            postDelayed(mDelayedShow, MIN_DELAY_MS);
            mPostedShow = true;
        }
    }
}
