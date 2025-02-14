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

package androidx.camera.core.processing.concurrent;

import androidx.camera.core.processing.util.OutConfig;

import com.google.auto.value.AutoValue;

import org.jspecify.annotations.NonNull;

/**
 * An internal augmented {@link OutConfig} for dual concurrent cameras.
 */
@AutoValue
public abstract class DualOutConfig {

    /**
     * Primary camera {@link OutConfig}.
     */
    public abstract @NonNull OutConfig getPrimaryOutConfig();

    /**
     * Secondary camera {@link OutConfig}.
     */
    public abstract @NonNull OutConfig getSecondaryOutConfig();

    /**
     * Creates {@link DualOutConfig}.
     */
    public static @NonNull DualOutConfig of(
            @NonNull OutConfig primaryOutConfig,
            @NonNull OutConfig secondaryOutConfig) {
        return new AutoValue_DualOutConfig(
                primaryOutConfig, secondaryOutConfig);
    }
}
