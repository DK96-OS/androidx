/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.compose.foundation.benchmark

import androidx.compose.foundation.benchmark.lazy.doFramesUntilIdle
import androidx.compose.testutils.ComposeExecutionControl
import androidx.compose.testutils.benchmark.ComposeBenchmarkRule
import androidx.compose.testutils.benchmark.benchmarkDrawPerf
import androidx.compose.testutils.benchmark.benchmarkFirstCompose
import androidx.compose.testutils.benchmark.benchmarkFirstDraw
import androidx.compose.testutils.benchmark.benchmarkFirstLayout
import androidx.compose.testutils.benchmark.benchmarkFirstMeasure
import androidx.compose.testutils.benchmark.benchmarkLayoutPerf
import androidx.compose.testutils.benchmark.benchmarkReuseFor
import androidx.compose.testutils.benchmark.toggleStateBenchmarkDraw
import androidx.compose.testutils.benchmark.toggleStateBenchmarkLayout
import androidx.compose.testutils.benchmark.toggleStateBenchmarkMeasure
import androidx.compose.ui.platform.ViewRootForTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ScrollerBenchmark {
    @get:Rule val benchmarkRule = ComposeBenchmarkRule()

    private val scrollerCaseFactory = { ScrollerTestCase() }

    @Test
    fun first_compose() {
        benchmarkRule.benchmarkFirstCompose(scrollerCaseFactory)
    }

    @Test
    fun first_measure() {
        benchmarkRule.benchmarkFirstMeasure(scrollerCaseFactory)
    }

    @Test
    fun first_layout() {
        benchmarkRule.benchmarkFirstLayout(scrollerCaseFactory)
    }

    @Test
    fun first_draw() {
        benchmarkRule.benchmarkFirstDraw(scrollerCaseFactory)
    }

    @Test
    fun reuse() {
        benchmarkRule.benchmarkReuseFor { ScrollerTestCase().MeasuredContent() }
    }

    @Test
    fun changeScroll_measure() {
        benchmarkRule.toggleStateBenchmarkMeasure(
            scrollerCaseFactory,
            toggleCausesRecompose = false
        )
    }

    @Test
    fun changeScroll_layout() {
        benchmarkRule.toggleStateBenchmarkLayout(scrollerCaseFactory, toggleCausesRecompose = false)
    }

    @Test
    fun changeScroll_draw() {
        benchmarkRule.toggleStateBenchmarkDraw(scrollerCaseFactory, toggleCausesRecompose = false)
    }

    @Test
    fun mouseWheelScroll_initialScroll() {
        with(benchmarkRule) {
            runBenchmarkFor({ MouseWheelScrollerTestCase() }) {
                measureRepeatedOnUiThread {
                    runWithTimingDisabled {
                        doFrame()
                        assertNoPendingRecompositionMeasureOrLayout()
                    }

                    performToggle(getTestCase())

                    runWithTimingDisabled {
                        // This benchmark only cares about initial cost of adding the scroll node
                        disposeContent()
                    }
                }
            }
        }
    }

    @Test
    fun mouseWheelScroll_successiveScrolls() {
        with(benchmarkRule) {
            runBenchmarkFor({ MouseWheelScrollerTestCase() }) {
                runOnUiThread {
                    doFrame()
                    performToggle(getTestCase())
                    doFrame()
                }

                measureRepeatedOnUiThread {
                    performToggle(getTestCase())
                    runWithTimingDisabled { doFramesUntilIdle() }
                }
            }
        }
    }

    @Test
    fun layout() {
        benchmarkRule.benchmarkLayoutPerf(scrollerCaseFactory)
    }

    @Test
    fun draw() {
        benchmarkRule.benchmarkDrawPerf(scrollerCaseFactory)
    }
}

// Below are forked from LazyBenchmarkCommon
private fun ComposeExecutionControl.performToggle(testCase: MouseWheelScrollerTestCase) {
    testCase.toggleState()
    if (hasPendingChanges()) {
        recompose()
    }
    if (hasPendingMeasureOrLayout()) {
        getViewRoot().measureAndLayoutForTest()
    }
}

private fun ComposeExecutionControl.assertNoPendingRecompositionMeasureOrLayout() {
    if (hasPendingChanges() || hasPendingMeasureOrLayout()) {
        throw AssertionError("Expected no pending changes but there were some.")
    }
}

private fun ComposeExecutionControl.getViewRoot(): ViewRootForTest =
    getHostView() as ViewRootForTest
