/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.core.view.insetscontrast;

import static androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP;
import static androidx.core.view.WindowInsetsCompat.Side.BOTTOM;
import static androidx.core.view.WindowInsetsCompat.Side.LEFT;
import static androidx.core.view.WindowInsetsCompat.Side.RIGHT;
import static androidx.core.view.WindowInsetsCompat.Side.TOP;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowInsets;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Monitors and provides necessary information of system bars that need to be protected by
 * {@link ContrastProtection}. This is used to create {@link ProtectionView}.
 */
public class SystemBarStateMonitor {

    private final View mDetector;
    private final ArrayList<Callback> mCallbacks = new ArrayList<>();
    private Insets mInsets = Insets.NONE;
    private Insets mInsetsIgnoringVisibility = Insets.NONE;
    private int mColorHint;
    private boolean mDisposed;

    /**
     * Creates an instance of SystemBarState associating with a window. This should be called
     * before the window is attached to window manager. Otherwise, it might miss callbacks of
     * {@link View.OnApplyWindowInsetsListener#onApplyWindowInsets(View, WindowInsets)}.
     *
     * @param window the window that the {@link ContrastProtection}s should be attached to.
     */
    public SystemBarStateMonitor(@NonNull Window window) {
        final ViewGroup decor = (ViewGroup) window.getDecorView();
        if (decor.isAttachedToWindow()) {
            throw new IllegalStateException(
                    "The given window must not be attached when creating the SystemBarState.");
        }

        // Make the window go edge-to-edge and prevent the system from adding the color views.
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        final Drawable drawable = decor.getBackground();
        mColorHint = drawable instanceof ColorDrawable
                ? ((ColorDrawable) drawable).getColor()
                : Color.TRANSPARENT;

        // Add a view to detect the insets and configuration change, and to monitor the insets
        // animation.
        mDetector = new View(decor.getContext()) {

            @Override
            protected void onConfigurationChanged(Configuration newConfig) {
                final Drawable drawable = decor.getBackground();
                final int color = drawable instanceof ColorDrawable
                        ? ((ColorDrawable) drawable).getColor()
                        : Color.TRANSPARENT;
                if (mColorHint != color) {
                    mColorHint = color;
                    for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                        mCallbacks.get(i).onColorHintChanged(color);
                    }
                }
            }
        };
        mDetector.setWillNotDraw(true);
        ViewCompat.setOnApplyWindowInsetsListener(mDetector, (view, windowInsets) -> {
            final Insets insets = getInsets(windowInsets);
            final Insets insetsIgnoringVis = getInsetsIgnoringVisibility(windowInsets);
            if (!insets.equals(mInsets) || !insetsIgnoringVis.equals(mInsetsIgnoringVisibility)) {
                mInsets = insets;
                mInsetsIgnoringVisibility = insetsIgnoringVis;
                for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                    mCallbacks.get(i).onInsetsChanged(insets, insetsIgnoringVis);
                }
            }
            return windowInsets;
        });
        ViewCompat.setWindowInsetsAnimationCallback(mDetector,
                new WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

                    private final HashMap<WindowInsetsAnimationCompat, Integer> mAnimationSidesMap =
                            new HashMap<>();

                    @Override
                    public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                        if (!animatesSystemBars(animation)) {
                            return;
                        }
                        for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                            mCallbacks.get(i).onAnimationStart();
                        }
                    }

                    @Override
                    public WindowInsetsAnimationCompat.@NonNull BoundsCompat onStart(
                            @NonNull WindowInsetsAnimationCompat animation,
                            WindowInsetsAnimationCompat.@NonNull BoundsCompat bounds) {
                        if (!animatesSystemBars(animation)) {
                            return bounds;
                        }
                        Insets upper = bounds.getUpperBound();
                        Insets lower = bounds.getLowerBound();
                        int sides = 0;
                        if (upper.left != lower.left) {
                            sides |= LEFT;
                        }
                        if (upper.top != lower.top) {
                            sides |= TOP;
                        }
                        if (upper.right != lower.right) {
                            sides |= RIGHT;
                        }
                        if (upper.bottom != lower.bottom) {
                            sides |= BOTTOM;
                        }
                        mAnimationSidesMap.put(animation, sides);
                        return bounds;
                    }

                    @Override
                    public @NonNull WindowInsetsCompat onProgress(
                            @NonNull WindowInsetsCompat windowInsets,
                            @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                        final RectF alpha = new RectF(1f, 1f, 1f, 1f);
                        int animatingSides = 0;
                        for (int i = runningAnimations.size() - 1; i >= 0; i--) {
                            final WindowInsetsAnimationCompat animation = runningAnimations.get(i);
                            final Integer possibleSides = mAnimationSidesMap.get(animation);
                            if (possibleSides != null) {
                                final int sides = possibleSides;
                                float animAlpha = animation.getAlpha();
                                if ((sides & LEFT) != 0) {
                                    alpha.left = animAlpha;
                                }
                                if ((sides & TOP) != 0) {
                                    alpha.top = animAlpha;
                                }
                                if ((sides & RIGHT) != 0) {
                                    alpha.right = animAlpha;
                                }
                                if ((sides & BOTTOM) != 0) {
                                    alpha.bottom = animAlpha;
                                }
                                animatingSides |= sides;
                            }
                        }
                        final Insets insets = getInsets(windowInsets);
                        for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                            mCallbacks.get(i).onAnimationProgress(animatingSides, insets, alpha);
                        }
                        return windowInsets;
                    }

                    @Override
                    public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                        if (!animatesSystemBars(animation)) {
                            return;
                        }
                        mAnimationSidesMap.remove(animation);
                        for (int i = mCallbacks.size() - 1; i >= 0; i--) {
                            mCallbacks.get(i).onAnimationEnd();
                        }
                    }

                    private boolean animatesSystemBars(@NonNull WindowInsetsAnimationCompat anim) {
                        return (anim.getTypeMask() & WindowInsetsCompat.Type.systemBars()) != 0;
                    }
                });
        decor.addView(mDetector, 0);
    }

    private Insets getInsets(WindowInsetsCompat w) {
        final Insets systemBarInsets = w.getInsets(WindowInsetsCompat.Type.systemBars());
        final Insets tappableElementInsets = w.getInsets(WindowInsetsCompat.Type.tappableElement());
        return Insets.min(systemBarInsets, tappableElementInsets);
    }

    private Insets getInsetsIgnoringVisibility(WindowInsetsCompat w) {
        final Insets systemBarInsets = w.getInsetsIgnoringVisibility(
                WindowInsetsCompat.Type.systemBars());
        final Insets tappableElementInsets = w.getInsetsIgnoringVisibility(
                WindowInsetsCompat.Type.tappableElement());
        return Insets.min(systemBarInsets, tappableElementInsets);
    }

    /**
     * Adds {@link Callback} to listen to the state change.
     *
     * @param callback the given {@link Callback}.
     */
    void addCallback(@NonNull Callback callback) {
        if (mDisposed) {
            throw new IllegalArgumentException("The SystemBarProtector has been disposed.");
        }
        if (mCallbacks.contains(callback)) {
            return;
        }
        mCallbacks.add(callback);
        callback.onInsetsChanged(mInsets, mInsetsIgnoringVisibility);
        callback.onColorHintChanged(mColorHint);
    }

    /**
     * Removes the {@link Callback} added previously.
     *
     * @param callback the given {@link Callback}.
     */
    void removeCallback(@NonNull Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * Listens to the changes of system bars that need to be protected by
     * {@link ContrastProtection}.
     */
    interface Callback {

        /**
         * Called when the any of the insets is changed.
         *
         * @param insets the system bar insets that need to be protected.
         * @param insetsIgnoringVisibility same as `insets` but as if the it is visible.
         */
        void onInsetsChanged(Insets insets, Insets insetsIgnoringVisibility);

        /**
         * Called when the suggested system bar color is changed.
         *
         * @param color the suggested color.
         */
        void onColorHintChanged(int color);

        /**
         * Called when the system bar animation is about to start.
         */
        void onAnimationStart();

        /**
         * Called when the insets or alpha change as part of running an animation.
         *
         * @param sides the animating sides.
         * @param insets the current insets.
         * @param alpha the current alpha.
         */
        void onAnimationProgress(int sides, Insets insets, RectF alpha);

        /**
         * Called when the system bar animation is finished.
         */
        void onAnimationEnd();
    }

    /**
     * Removes all the objects added by this class from the associated window.
     */
    public void dispose() {
        if (mDisposed) {
            return;
        }
        mDisposed = true;
        final ViewParent parent = mDetector.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mDetector);
        }
        mCallbacks.clear();
    }
}
