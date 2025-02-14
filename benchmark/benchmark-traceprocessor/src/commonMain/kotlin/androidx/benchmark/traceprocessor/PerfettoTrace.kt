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

package androidx.benchmark.traceprocessor

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP

public class PerfettoTrace(
    /**
     * Absolute file path of the trace.
     *
     * Note that the trace is not guaranteed to be placed into an app-accessible directory, and may
     * require shell commands to access.
     */
    @get:RestrictTo(LIBRARY_GROUP) public val path: String
) {
    // this companion object exists to enable PerfettoTrace.Companion.record to be declared
    public companion object
}
