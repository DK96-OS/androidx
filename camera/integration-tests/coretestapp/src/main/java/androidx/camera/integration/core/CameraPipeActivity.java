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

package androidx.camera.integration.core;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.jspecify.annotations.Nullable;

/**
 * An activity to launch "Camera Core Test App" with Camera Pipe configuration
 *
 * <p>Launches CameraXActivity with Camera Pipe configuration.
 */
public class CameraPipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(CameraPipeActivity.this, CameraXActivity.class);
        Bundle b = new Bundle();
        b.putString(CameraXActivity.INTENT_EXTRA_CAMERA_IMPLEMENTATION,
                CameraXViewModel.CAMERA_PIPE_IMPLEMENTATION_OPTION);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }
}
