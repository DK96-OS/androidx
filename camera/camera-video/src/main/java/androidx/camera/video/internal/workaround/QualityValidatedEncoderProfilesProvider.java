/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.camera.video.internal.workaround;

import static android.media.CamcorderProfile.QUALITY_1080P;
import static android.media.CamcorderProfile.QUALITY_2160P;
import static android.media.CamcorderProfile.QUALITY_480P;
import static android.media.CamcorderProfile.QUALITY_720P;
import static android.media.CamcorderProfile.QUALITY_HIGH;
import static android.media.CamcorderProfile.QUALITY_LOW;

import static androidx.camera.video.Quality.FHD;
import static androidx.camera.video.Quality.HD;
import static androidx.camera.video.Quality.HIGHEST;
import static androidx.camera.video.Quality.LOWEST;
import static androidx.camera.video.Quality.SD;
import static androidx.camera.video.Quality.UHD;

import androidx.camera.core.impl.CameraInfoInternal;
import androidx.camera.core.impl.EncoderProfilesProvider;
import androidx.camera.core.impl.EncoderProfilesProxy;
import androidx.camera.core.impl.Quirk;
import androidx.camera.core.impl.Quirks;
import androidx.camera.core.internal.compat.quirk.SurfaceProcessingQuirk;
import androidx.camera.video.Quality;
import androidx.camera.video.internal.compat.quirk.VideoQualityQuirk;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation that provides the {@link EncoderProfilesProxy} only when the quality is
 * capable of video recording
 *
 * @see VideoQualityQuirk
 */
public class QualityValidatedEncoderProfilesProvider implements EncoderProfilesProvider {

    private static final Map<Integer, Quality> CAMCORDER_TO_VIDEO_QUALITY_MAP = new HashMap<>();
    static {
        CAMCORDER_TO_VIDEO_QUALITY_MAP.put(QUALITY_HIGH, HIGHEST);
        CAMCORDER_TO_VIDEO_QUALITY_MAP.put(QUALITY_2160P, UHD);
        CAMCORDER_TO_VIDEO_QUALITY_MAP.put(QUALITY_1080P, FHD);
        CAMCORDER_TO_VIDEO_QUALITY_MAP.put(QUALITY_720P, HD);
        CAMCORDER_TO_VIDEO_QUALITY_MAP.put(QUALITY_480P, SD);
        CAMCORDER_TO_VIDEO_QUALITY_MAP.put(QUALITY_LOW, LOWEST);
    }

    private final @NonNull EncoderProfilesProvider mProvider;
    private final @NonNull CameraInfoInternal mCameraInfo;
    private final @NonNull Quirks mQuirks;

    public QualityValidatedEncoderProfilesProvider(@NonNull EncoderProfilesProvider provider,
            @NonNull CameraInfoInternal cameraInfo, @NonNull Quirks quirks) {
        mProvider = provider;
        mCameraInfo = cameraInfo;
        mQuirks = quirks;
    }

    @Override
    public boolean hasProfile(int quality) {
        return mProvider.hasProfile(quality) && isDeviceValidQuality(quality);
    }

    @Override
    public @Nullable EncoderProfilesProxy getAll(int quality) {
        if (!hasProfile(quality)) {
            return null;
        }

        return mProvider.getAll(quality);
    }

    private boolean isDeviceValidQuality(int quality) {
        Quality videoQuality = CAMCORDER_TO_VIDEO_QUALITY_MAP.get(quality);

        // Check if the quality is not problematic or can be workaround.
        if (videoQuality != null) {
            for (VideoQualityQuirk quirk : mQuirks.getAll(VideoQualityQuirk.class)) {
                // All quirks must be able to be workaround, then it can be considered valid.
                if (quirk != null && quirk.isProblematicVideoQuality(mCameraInfo, videoQuality)) {
                    if (!workaroundBySurfaceProcessing(quirk)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean workaroundBySurfaceProcessing(@NonNull Quirk quirk) {
        return quirk instanceof SurfaceProcessingQuirk
                && ((SurfaceProcessingQuirk) quirk).workaroundBySurfaceProcessing();
    }
}
