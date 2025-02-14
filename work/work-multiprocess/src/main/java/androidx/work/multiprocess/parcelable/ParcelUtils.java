/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.work.multiprocess.parcelable;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RestrictTo;

import org.jspecify.annotations.NonNull;

/**
 *
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class ParcelUtils {
    private ParcelUtils() {
        // Does nothing
    }

    /**
     * Reads a boolean value from a parcel.
     */
    public static boolean readBooleanValue(@NonNull Parcel parcel) {
        int value = parcel.readInt();
        return value == 1;
    }

    /**
     * Writes a boolean value into a parcel,
     */
    public static void writeBooleanValue(@NonNull Parcel parcel, boolean value) {
        parcel.writeInt(value ? 1 : 0);
    }

    /**
     * Reads a parcelable from a parcel.
     * <p>
     * It's safe for us to suppress warnings here, given the correct API is being used starting
     * API 33.
     */
    @SuppressWarnings("deprecation")
    public static <T extends Parcelable> T readParcelable(
            @NonNull Parcel parcel,
            @NonNull Class<T> klass
    ) {
        ClassLoader classLoader = klass.getClassLoader();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return parcel.readParcelable(classLoader, klass);
        } else {
            return parcel.readParcelable(classLoader);
        }
    }
}
