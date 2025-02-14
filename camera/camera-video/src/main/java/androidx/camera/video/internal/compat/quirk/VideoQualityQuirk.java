/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.camera.video.internal.compat.quirk;

import androidx.camera.core.impl.CameraInfoInternal;
import androidx.camera.core.impl.Quirk;
import androidx.camera.core.internal.compat.quirk.SurfaceProcessingQuirk;
import androidx.camera.video.Quality;
import androidx.camera.video.internal.workaround.QualityValidatedEncoderProfilesProvider;

import org.jspecify.annotations.NonNull;

/**
 * The quirk interface which denotes the quality does not work for video recording on the device.
 *
 * <p>Subclasses of this interface can denote the reason of the Quality option that not capable
 * for video recording. Subclasses of this interface can also implement
 * {@link SurfaceProcessingQuirk} if the problem can be workaround by enabling surface processing.
 * Then it may not be taken as an invalid quality in
 * {@link QualityValidatedEncoderProfilesProvider}.
 *
 * @see QualityValidatedEncoderProfilesProvider
 */
public interface VideoQualityQuirk extends Quirk {

    /** Checks if the given Quality type is a problematic quality. */
    boolean isProblematicVideoQuality(@NonNull CameraInfoInternal cameraInfo,
            @NonNull Quality quality);
}
