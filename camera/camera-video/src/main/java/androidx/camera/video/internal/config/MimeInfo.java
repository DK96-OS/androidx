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

package androidx.camera.video.internal.config;

import androidx.camera.video.internal.VideoValidatedEncoderProfilesProxy;

import org.jspecify.annotations.NonNull;

/**
 * Data class containing information about a media mime.
 *
 * <p>The information included in this class can include the mime type, profile and any
 * compatible configuration types that can be used to resolve settings, such as
 * {@link VideoValidatedEncoderProfilesProxy}.
 */
@SuppressWarnings("NullableProblems") // Problem from AutoValue generated class.
// Base class for @AutoValue subclasses
public abstract class MimeInfo {

    /** Returns the mime type. */
    public abstract @NonNull String getMimeType();

    /**
     * Returns the profile for the given mime.
     *
     * <p>The returned integer will generally come from
     * {@link android.media.MediaCodecInfo.CodecProfileLevel}, or if no profile is required,
     * {@link androidx.camera.video.internal.encoder.EncoderConfig#CODEC_PROFILE_NONE}.
     */
    public abstract int getProfile();

    /**
     * A Builder for a {@link androidx.camera.video.internal.config.MimeInfo}.
     *
     * @param <B> The builder subclass.
     */
    // Base class for @AutoValue.Builder subclasses
    public abstract static class Builder<B> {

        // Protected since this should be passed to builder factory method.
        protected abstract @NonNull B setMimeType(@NonNull String mimeType);

        /** Sets the mime profile */
        public abstract @NonNull B setProfile(int profile);

        /** Builds the {@link androidx.camera.video.internal.config.MimeInfo}. */
        public abstract @NonNull MimeInfo build();
    }

}
