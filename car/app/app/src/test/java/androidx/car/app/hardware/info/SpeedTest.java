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

package androidx.car.app.hardware.info;

import static com.google.common.truth.Truth.assertThat;

import androidx.car.app.hardware.common.CarValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class SpeedTest {

    @Test
    public void builder_allCarValuesAreUnknownIfNotSet() {
        assertThat(new Speed.Builder().build()).isEqualTo(
                new Speed.Builder().setRawSpeedMetersPerSecond(
                        CarValue.UNKNOWN_FLOAT).setDisplaySpeedMetersPerSecond(
                        CarValue.UNKNOWN_FLOAT).setSpeedDisplayUnit(
                        CarValue.UNKNOWN_INTEGER).build());
    }
}
