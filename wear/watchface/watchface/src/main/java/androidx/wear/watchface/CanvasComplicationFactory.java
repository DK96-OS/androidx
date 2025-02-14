/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.wear.watchface;

import androidx.annotation.WorkerThread;

import org.jspecify.annotations.NonNull;

/**
 * Factory for creating a CanvasComplication. This decouples construction of Complications and their
 * CanvasComplication renderers.
 */
// TODO(b/188035300): Put links into the comments.
public interface CanvasComplicationFactory {
    /**
     * Creates a CanvasComplication. This will be called on a background thread, however all
     * CanvasComplication rendering will be done on the UI thread and there's a memory barrier
     * between construction and usage so no special threading primitives are required. If the
     * CanvasComplication needs to share state with the Renderer then it should override
     * onRendererCreated.
     *
     * @param watchState The current WatchState
     * @param invalidateCallback The CanvasComplication.InvalidateCallback the constructed
     *     CanvasComplication can use to request redrawing. This is typically used in conjunction
     *     with asynchronous loading of Drawables to update the watch face once the drawable has
     *     loaded.
     * @return The constructed CanvasComplication.
     */
    @WorkerThread
    @SuppressWarnings("ExecutorRegistration") // This is UI thread only.
    @NonNull CanvasComplication create(
            @NonNull WatchState watchState,
            CanvasComplication.@NonNull InvalidateCallback invalidateCallback);
}
