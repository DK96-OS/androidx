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

package androidx.core.widget;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcelable;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.ScrollFeedbackProviderCompat;
import androidx.core.view.ViewCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class NestedScrollViewTest {

    private NestedScrollView mNestedScrollView;
    private View mChild;
    private ScrollFeedbackProviderCompat mScrollFeedbackProvider;

    @Test
    public void getBottomFadingEdgeStrength_childBottomIsBelowParentWithoutMargins_isCorrect() {
        setup(200);
        mNestedScrollView.setVerticalFadingEdgeEnabled(true);
        measureAndLayout(100);

        float expected = mNestedScrollView.getBottomFadingEdgeStrength();

        assertThat(expected, is(1.0f));
    }

    @Test
    public void getBottomFadingEdgeStrength_childBottomIsBelowParentDuetoMargins_isCorrect() {
        setup(100);
        mNestedScrollView.setVerticalFadingEdgeEnabled(true);
        setChildMargins(100, 0);
        measureAndLayout(100);

        float expected = mNestedScrollView.getBottomFadingEdgeStrength();

        assertThat(expected, is(1.0f));
    }

    @Test
    public void getBottomFadingEdgeStrength_childIsAboveButMarginIsBelowParent_isCorrect() {
        setup(100);
        mNestedScrollView.setVerticalFadingEdgeEnabled(true);
        setChildMargins(0, 100);
        measureAndLayout(100);

        float expected = mNestedScrollView.getBottomFadingEdgeStrength();

        assertThat(expected, is(1.0f));
    }

    @Test
    public void getBottomFadingEdgeStrength_childBottomIsAboveParentAndNoMargin_isZero() {
        setup(100);
        mNestedScrollView.setVerticalFadingEdgeEnabled(true);
        measureAndLayout(100);

        float expected = mNestedScrollView.getBottomFadingEdgeStrength();

        assertThat(expected, is(0f));
    }

    @Test
    public void onMeasure_fillViewPortEnabledChildSmallButWithMarginBig_childMeasuredCorrectly() {
        setup(50);
        setChildMargins(25, 25);
        mNestedScrollView.setFillViewport(true);

        measure(100);

        assertThat(mChild.getMeasuredHeight(), is(50));
    }

    @Test
    public void onMeasure_fillViewPortEnabledChildSmallWithMargins_childMeasuredCorrectly() {
        setup(50);
        setChildMargins(20, 20);
        mNestedScrollView.setFillViewport(true);

        measure(100);

        assertThat(mChild.getMeasuredHeight(), is(60));
    }

    @Test
    public void onMeasure_fillViewPortEnabledChildSmallNoMargins_childMeasuredCorrectly() {
        setup(50);
        setChildMargins(0, 0);
        mNestedScrollView.setFillViewport(true);

        measure(100);

        assertThat(mChild.getMeasuredHeight(), is(100));
    }

    @Test
    public void executeKeyEvent_spaceBarCanScrollDueToMargins_scrolls() {
        setup(75);
        setChildMargins(0, 50);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);

        mNestedScrollView.executeKeyEvent(keyEvent);

        assertThat(mNestedScrollView.getScrollY(), is(25));
    }

    @Test
    public void pageScroll_takesAccountOfMargin() {
        setup(75);
        setChildMargins(20, 30);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        mNestedScrollView.pageScroll(View.FOCUS_DOWN);

        assertThat(mNestedScrollView.getScrollY(), is(25));
    }

    @Test
    public void getScrollRange_takesAccountOfMargin() {
        setup(100);
        setChildMargins(20, 30);
        measureAndLayout(100);

        int expected = mNestedScrollView.getScrollRange();

        assertThat(expected, is(50));
    }

    @Test
    public void fullScroll_scrollsToEndOfMargin() {
        setup(300);
        setChildMargins(20, 30);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        mNestedScrollView.fullScroll(View.FOCUS_DOWN);

        assertThat(mNestedScrollView.getScrollY(), is(250));
    }

    @Test
    public void arrowScroll_canScrollHalfDownDueToSizeAndMargin_scrollsHalfDown() {
        setup(130);
        setChildMargins(10, 20);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        mNestedScrollView.arrowScroll(View.FOCUS_DOWN);

        assertThat(mNestedScrollView.getScrollY(), is(50));
    }

    @Test
    public void arrowScroll_canScrollQuarterDownDueToSizeAndMargin_scrollsQuarterDown() {
        setup(75);
        setChildMargins(25, 25);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        mNestedScrollView.arrowScroll(View.FOCUS_DOWN);

        assertThat(mNestedScrollView.getScrollY(), is(25));
    }

    @Test
    public void arrowScroll_canOnlyScrollQuarterUp_scrollsQuarterUp() {
        setup(75);
        setChildMargins(25, 25);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        mNestedScrollView.scrollTo(0, 25);
        mNestedScrollView.arrowScroll(View.FOCUS_UP);

        assertThat(mNestedScrollView.getScrollY(), is(0));
    }

    @Test
    public void arrowScroll_canScroll_returnsTrue() {
        setup(75);
        setChildMargins(20, 30);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        boolean actualResult = mNestedScrollView.arrowScroll(View.FOCUS_DOWN);

        assertThat(actualResult, is(true));
    }

    @Test
    public void arrowScroll_cantScroll_returnsFalse() {
        setup(50);
        setChildMargins(25, 25);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        boolean actualResult = mNestedScrollView.arrowScroll(View.FOCUS_DOWN);

        assertThat(actualResult, is(false));
    }

    @Test
    public void computeVerticalScrollRange_takesAccountOfMargin() {
        setup(200);
        setChildMargins(20, 30);
        measureAndLayout(100);

        int actual = mNestedScrollView.computeVerticalScrollRange();

        assertThat(actual, is(250));
    }

    @Test
    public void computeScrollDeltaToGetChildRectOnScreen_marginRespectedToMakeRoomForFadingEdge() {
        setup(200);
        setChildMargins(0, 1);
        mNestedScrollView.setVerticalFadingEdgeEnabled(true);
        mNestedScrollView.setFadingEdgeLength(25);
        measureAndLayout(100);
        Rect rect = new Rect(0, 175, 100, 200);

        int actual = mNestedScrollView.computeScrollDeltaToGetChildRectOnScreen(rect);

        assertThat(actual, is(101));
    }

    @Test
    public void computeScrollDeltaToGetChildRectOnScreen_fadingEdgeNoMargin_clampsToEnd() {
        setup(200);
        setChildMargins(0, 0);
        mNestedScrollView.setVerticalFadingEdgeEnabled(true);
        mNestedScrollView.setFadingEdgeLength(25);
        measureAndLayout(100);
        Rect rect = new Rect(0, 175, 100, 200);

        int actual = mNestedScrollView.computeScrollDeltaToGetChildRectOnScreen(rect);

        assertThat(actual, is(100));
    }

    @Test
    public void onLayout_canScrollDistanceFromSavedInstanceStateDueToMargins_scrollsDistance() {

        // Arrange.

        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        mNestedScrollView.scrollTo(0, 100);
        Parcelable savedState = mNestedScrollView.onSaveInstanceState();

        setup(100);
        setChildMargins(25, 75);
        mNestedScrollView.onRestoreInstanceState(savedState);

        // Act.

        measureAndLayout(100);

        // Assert

        assertThat(mNestedScrollView.getScrollY(), is(100));
    }

    @Test
    public void scrollTo_childHasMargins_scrollsToEndOfMargins() {
        setup(100);
        setChildMargins(25, 75);
        mNestedScrollView.setSmoothScrollingEnabled(false);
        measureAndLayout(100);

        mNestedScrollView.scrollTo(0, 100);

        assertThat(mNestedScrollView.getScrollY(), is(100));
    }

    @Test
    public void testTopEdgeEffectReversal() {
        setup(200);
        mNestedScrollView.mEdgeGlowTop = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        setChildMargins(0, 0);
        measureAndLayout(100);
        swipeDown(false);
        assertEquals(0, mNestedScrollView.getScrollY());
        swipeUp(true);
        if (Build.VERSION.SDK_INT >= 31) {
            // This should just reverse the overscroll effect
            assertEquals(0, mNestedScrollView.getScrollY());
        } else {
            // Can't catch the overscroll effect for R and earlier
            assertNotEquals(0, mNestedScrollView.getScrollY());
        }
    }

    @Test
    public void testBottomEdgeEffectReversal() {
        setup(200);
        mNestedScrollView.mEdgeGlowBottom =
                new EdgeEffectSubstitute(mNestedScrollView.getContext());
        setChildMargins(0, 0);
        measureAndLayout(100);
        int scrollRange = mNestedScrollView.getScrollRange();
        mNestedScrollView.scrollTo(0, scrollRange);
        assertEquals(scrollRange, mNestedScrollView.getScrollY());
        swipeUp(false);
        assertEquals(scrollRange, mNestedScrollView.getScrollY());
        swipeDown(true);
        if (Build.VERSION.SDK_INT >= 31) {
            // This should just reverse the overscroll effect
            assertEquals(scrollRange, mNestedScrollView.getScrollY());
        } else {
            // Can't catch the overscroll effect for R and earlier
            assertNotEquals(scrollRange, mNestedScrollView.getScrollY());
        }
    }

    @Test
    public void testFlingWhileStretchedAtTop() {
        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        EdgeEffectSubstitute edgeEffect = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        mNestedScrollView.mEdgeGlowTop = edgeEffect;
        flingDown();
        assertTrue(edgeEffect.getDistance() > 0);

        if (Build.VERSION.SDK_INT >= 31) {
            assertTrue(edgeEffect.getAbsorbed() > 0);
        } else {
            assertEquals(0, edgeEffect.getAbsorbed());
            flingUp();
            assertNotEquals(0, mNestedScrollView.getScrollY());
        }
    }

    @Test
    public void testFlingWhileStretchedAtBottom() {
        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        EdgeEffectSubstitute edgeEffect = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        mNestedScrollView.mEdgeGlowBottom = edgeEffect;

        int scrollRange = mNestedScrollView.getScrollRange();
        mNestedScrollView.scrollTo(0, scrollRange);
        assertEquals(scrollRange, mNestedScrollView.getScrollY());
        flingUp();
        assertTrue(edgeEffect.getDistance() > 0);
        assertEquals(scrollRange, mNestedScrollView.getScrollY());

        if (Build.VERSION.SDK_INT >= 31) {
            assertTrue(edgeEffect.getAbsorbed() > 0);
        } else {
            assertEquals(0, edgeEffect.getAbsorbed());
            flingDown();
            assertNotEquals(scrollRange, mNestedScrollView.getScrollY());
        }
    }

    @Test
    public void scrollFromRotaryStretchesTop() {
        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        EdgeEffectSubstitute edgeEffect = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        mNestedScrollView.mEdgeGlowTop = edgeEffect;

        sendScroll(2f, InputDevice.SOURCE_ROTARY_ENCODER);

        // Should be pulled and retracting
        assertTrue(edgeEffect.getDistance() > 0);
        assertEquals(EdgeEffectSubstitute.State.Animating, edgeEffect.getState());
    }

    @Test
    public void scrollFromRotaryStretchesBottom() {
        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        EdgeEffectSubstitute edgeEffect = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        mNestedScrollView.mEdgeGlowBottom = edgeEffect;
        int scrollRange = mNestedScrollView.getScrollRange();
        mNestedScrollView.scrollTo(0, scrollRange);

        sendScroll(-2f, InputDevice.SOURCE_ROTARY_ENCODER);

        // Should be pulled and retracting
        assertTrue(edgeEffect.getDistance() > 0);
        assertEquals(EdgeEffectSubstitute.State.Animating, edgeEffect.getState());
    }

    @Test
    public void scrollFromMouseDoesNotStretchTop() {
        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        EdgeEffectSubstitute edgeEffect = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        mNestedScrollView.mEdgeGlowTop = edgeEffect;

        sendScroll(2f, InputDevice.SOURCE_MOUSE);

        // Should not be pulled. It should be Idle at 0
        assertEquals(0f, edgeEffect.getDistance(), 0f);
        assertEquals(EdgeEffectSubstitute.State.Idle, edgeEffect.getState());
    }

    @Test
    public void scrollFromMouseDoesNotStretchBottom() {
        setup(200);
        setChildMargins(0, 0);
        measureAndLayout(100);
        EdgeEffectSubstitute edgeEffect = new EdgeEffectSubstitute(mNestedScrollView.getContext());
        mNestedScrollView.mEdgeGlowBottom = edgeEffect;
        int scrollRange = mNestedScrollView.getScrollRange();
        mNestedScrollView.scrollTo(0, scrollRange);

        sendScroll(-2f, InputDevice.SOURCE_MOUSE);

        // Should not be pulled. It should be Idle at 0
        assertEquals(0f, edgeEffect.getDistance(), 0f);
        assertEquals(EdgeEffectSubstitute.State.Idle, edgeEffect.getState());
    }

    @Test
    public void scrollFeedbackCallbacks() {
        setup(200);
        mScrollFeedbackProvider = mock(ScrollFeedbackProviderCompat.class);
        mNestedScrollView.mScrollFeedbackProvider = mScrollFeedbackProvider;
        setChildMargins(0, 0);
        measureAndLayout(100);

        MotionEvent ev = createScrollMotionEvent(
                /* scrollAmount= */ 2f,
                /* deviceId= */ 3,
                /* source= */ InputDevice.SOURCE_ROTARY_ENCODER,
                /* axis= */ MotionEvent.AXIS_SCROLL);
        // Scroll up by -2.
        mNestedScrollView.scrollBy(
                /* verticalScrollDistance= */ -2, MotionEvent.AXIS_SCROLL, ev, /* x= */ 3,
                ViewCompat.TYPE_TOUCH, /* isSourceMouseOrKeyboard= */ false);

        // Since the view was already at the very top, a scroll up by -2 should not cause a
        // onScrollProgress call, but should call onScrollLimit.
        verify(mScrollFeedbackProvider, never()).onScrollProgress(
                anyInt(), anyInt(), anyInt(), anyInt());
        verify(mScrollFeedbackProvider).onScrollLimit(
                /* inputDeviceId= */ 3, InputDevice.SOURCE_ROTARY_ENCODER, MotionEvent.AXIS_SCROLL,
                /* isStart= */ true);
        reset(mScrollFeedbackProvider);

        // Scroll down by 20.
        mNestedScrollView.scrollBy(
                /* verticalScrollDistance= */ 20, MotionEvent.AXIS_SCROLL, ev, /* x= */ 3,
                ViewCompat.TYPE_TOUCH, /* isSourceMouseOrKeyboard= */ false);

        // The height of the view is 100. Since the scroll is 20 pixels, we expect all of it to be
        // consumed. So, expect onScrollProgress with 20 pixels, and no onScrollLimit.
        verify(mScrollFeedbackProvider).onScrollProgress(
                /* inputDeviceId= */ 3, InputDevice.SOURCE_ROTARY_ENCODER, MotionEvent.AXIS_SCROLL,
                /* deltaInPixels= */ 20);
        verify(mScrollFeedbackProvider, never()).onScrollLimit(
                anyInt(), anyInt(), anyInt(), anyBoolean());

        // At this point, the view was at y=20. So a scroll of 100 pixels should do a consumed
        // scroll of 80 pixels, and also cause an onScrollLimit call.
        mNestedScrollView.scrollBy(
                /* verticalScrollDistance= */ 100, MotionEvent.AXIS_SCROLL, ev, /* x= */ 3,
                ViewCompat.TYPE_TOUCH, /* isSourceMouseOrKeyboard= */ false);

        verify(mScrollFeedbackProvider).onScrollProgress(
                /* inputDeviceId= */ 3, InputDevice.SOURCE_ROTARY_ENCODER, MotionEvent.AXIS_SCROLL,
                /* deltaInPixels= */ 80);
        verify(mScrollFeedbackProvider).onScrollLimit(
                /* inputDeviceId= */ 3, InputDevice.SOURCE_ROTARY_ENCODER, MotionEvent.AXIS_SCROLL,
                /* isStart= */ false);
    }

    @Test
    public void scrollFeedbackCallbacks_motionEventUnavailable() {
        setup(200);
        mScrollFeedbackProvider = mock(ScrollFeedbackProviderCompat.class);
        mNestedScrollView.mScrollFeedbackProvider = mScrollFeedbackProvider;
        setChildMargins(0, 0);
        measureAndLayout(100);

        mNestedScrollView.scrollBy(
                /* verticalScrollDistance= */ -2, MotionEvent.AXIS_SCROLL, /* ev= */ null,
                /* x= */ 3,  ViewCompat.TYPE_TOUCH, /* isSourceMouseOrKeyboard= */ false);

        verify(mScrollFeedbackProvider, never()).onScrollProgress(
                anyInt(), anyInt(), anyInt(), anyInt());
        verify(mScrollFeedbackProvider, never()).onScrollLimit(
                anyInt(), anyInt(), anyInt(), anyBoolean());
    }

    private void swipeDown(boolean shortSwipe) {
        float endY = shortSwipe ? mNestedScrollView.getHeight() / 2f :
                mNestedScrollView.getHeight() - 1;
        swipe(0, endY);
    }

    private void swipeUp(boolean shortSwipe) {
        float endY = shortSwipe ? mNestedScrollView.getHeight() / 2f : 0;
        swipe(mNestedScrollView.getHeight() - 1, endY);
    }

    private void swipe(float startY, float endY) {
        float x = mNestedScrollView.getWidth() / 2f;
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, x, startY, 0);
        mNestedScrollView.dispatchTouchEvent(down);
        MotionEvent move = MotionEvent.obtain(0, 10, MotionEvent.ACTION_MOVE, x, endY, 0);
        mNestedScrollView.dispatchTouchEvent(move);
        MotionEvent up = MotionEvent.obtain(0, 1000, MotionEvent.ACTION_UP, x, endY, 0);
        mNestedScrollView.dispatchTouchEvent(up);
    }

    private void flingDown() {
        fling(0, mNestedScrollView.getHeight() - 1);
    }

    private void flingUp() {
        fling(mNestedScrollView.getHeight() - 1, 0);
    }

    private void fling(float startY, float endY) {
        float x = mNestedScrollView.getWidth() / 2f;
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, x, startY, 0);
        mNestedScrollView.dispatchTouchEvent(down);
        MotionEvent move = MotionEvent.obtain(0, 10, MotionEvent.ACTION_MOVE, x, endY, 0);
        mNestedScrollView.dispatchTouchEvent(move);
        MotionEvent up = MotionEvent.obtain(0, 11, MotionEvent.ACTION_UP, x, endY, 0);
        mNestedScrollView.dispatchTouchEvent(up);
    }

    private MotionEvent createScrollMotionEvent(
            float scrollAmount, int deviceId, int source, int axis) {
        float x = mNestedScrollView.getWidth() / 2f;
        float y = mNestedScrollView.getHeight() / 2f;
        MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
        pointerProperties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
        pointerCoords.x = x;
        pointerCoords.y = y;

        pointerCoords.setAxisValue(axis, scrollAmount);

        return MotionEvent.obtain(
                0, /* downTime */
                0, /* eventTime */
                MotionEvent.ACTION_SCROLL, /* action */
                1, /* pointerCount */
                new MotionEvent.PointerProperties[] { pointerProperties },
                new MotionEvent.PointerCoords[] { pointerCoords },
                0, /* metaState */
                0, /* buttonState */
                0f, /* xPrecision */
                0f, /* yPrecision */
                deviceId,
                0, /* edgeFlags */
                source, /* source */
                0 /* flags */
        );
    }

    private void sendScroll(float scrollAmount, int source) {
        int axis = source == InputDevice.SOURCE_ROTARY_ENCODER
                ? MotionEvent.AXIS_SCROLL : MotionEvent.AXIS_VSCROLL;
        mNestedScrollView.dispatchGenericMotionEvent(
                createScrollMotionEvent(scrollAmount, /* deviceId= */ 1, source, axis));
    }

    private void setup(int childHeight) {
        Context context = ApplicationProvider.getApplicationContext();

        mChild = new View(context);
        mChild.setMinimumWidth(100);
        mChild.setMinimumHeight(childHeight);
        mChild.setBackgroundDrawable(
                new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0xFFFF0000, 0xFF00FF00}));

        mNestedScrollView = new NestedScrollView(context);
        mNestedScrollView.setBackgroundColor(0xFF0000FF);
        mNestedScrollView.addView(mChild);
    }

    private void setChildMargins(int top, int bottom) {
        NestedScrollView.LayoutParams childLayoutParams =
                new NestedScrollView.LayoutParams(100, 100);
        childLayoutParams.topMargin = top;
        childLayoutParams.bottomMargin = bottom;
        mChild.setLayoutParams(childLayoutParams);
    }

    private void measure(int height) {
        int measureSpecWidth =
                View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
        int measureSpecHeight =
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        mNestedScrollView.measure(measureSpecWidth, measureSpecHeight);
    }

    @SuppressWarnings("SameParameterValue")
    private void measureAndLayout(int height) {
        measure(height);
        mNestedScrollView.layout(0, 0, 100, height);
    }
}
