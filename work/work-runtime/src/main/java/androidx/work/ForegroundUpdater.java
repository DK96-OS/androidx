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

package androidx.work;

import android.content.Context;

import com.google.common.util.concurrent.ListenableFuture;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * Manages updating {@link android.app.Notification}s when a {@link ListenableWorker} transitions
 * to running in the context of a foreground {@link android.app.Service}.
 */
public interface ForegroundUpdater {

    /**
     * @param context        The application {@link Context}.
     * @param id             The {@link UUID} identifying the {@link ListenableWorker}
     * @param foregroundInfo The {@link ForegroundInfo}
     * @return @return The {@link ListenableFuture} which resolves after the
     * {@link ListenableWorker} transitions to running in the context of a foreground
     * {@link android.app.Service}.
     */
    @NonNull ListenableFuture<Void> setForegroundAsync(
            @NonNull Context context,
            @NonNull UUID id,
            @NonNull ForegroundInfo foregroundInfo);
}
