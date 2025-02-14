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

package androidx.xr.scenecore.impl;

import static androidx.xr.runtime.testing.math.MathAssertions.assertPose;
import static androidx.xr.runtime.testing.math.MathAssertions.assertVector3;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import androidx.xr.runtime.math.Matrix4;
import androidx.xr.runtime.math.Pose;
import androidx.xr.runtime.math.Quaternion;
import androidx.xr.runtime.math.Vector3;
import androidx.xr.scenecore.JxrPlatformAdapter.CameraViewActivityPose;
import androidx.xr.scenecore.JxrPlatformAdapter.CameraViewActivityPose.CameraType;
import androidx.xr.scenecore.JxrPlatformAdapter.CameraViewActivityPose.Fov;
import androidx.xr.scenecore.impl.perception.PerceptionLibrary;
import androidx.xr.scenecore.impl.perception.Session;
import androidx.xr.scenecore.impl.perception.ViewProjection;
import androidx.xr.scenecore.impl.perception.ViewProjections;
import androidx.xr.scenecore.testing.FakeScheduledExecutorService;
import androidx.xr.scenecore.testing.FakeXrExtensions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class CameraViewActivityPoseImplTest {

    private final AndroidXrEntity activitySpaceRoot = Mockito.mock(AndroidXrEntity.class);
    private final FakeXrExtensions fakeExtensions = new FakeXrExtensions();
    private final PerceptionLibrary perceptionLibrary = Mockito.mock(PerceptionLibrary.class);
    private final Session session = Mockito.mock(Session.class);
    private final FakeScheduledExecutorService executor = new FakeScheduledExecutorService();
    private final ActivitySpaceImpl activitySpace =
            new ActivitySpaceImpl(
                    fakeExtensions.createNode(),
                    fakeExtensions,
                    new EntityManager(),
                    () -> fakeExtensions.fakeSpatialState,
                    executor);

    /** Creates a CameraViewActivityPoseImpl. */
    private CameraViewActivityPoseImpl createCameraViewActivityPose(@CameraType int cameraType) {
        return new CameraViewActivityPoseImpl(
                cameraType, activitySpace, activitySpaceRoot, perceptionLibrary);
    }

    @Test
    public void getCameraType_returnsCameraType() {
        CameraViewActivityPoseImpl cameraActivityPoseLeft =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_LEFT_EYE);
        assertThat(cameraActivityPoseLeft.getCameraType())
                .isEqualTo(CameraViewActivityPose.CAMERA_TYPE_LEFT_EYE);

        CameraViewActivityPoseImpl cameraActivityPoseRight =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_RIGHT_EYE);
        assertThat(cameraActivityPoseRight.getCameraType())
                .isEqualTo(CameraViewActivityPose.CAMERA_TYPE_RIGHT_EYE);
    }

    @Test
    public void getFov_returnsCorrectFov() {
        Fov fovLeft = new Fov(1, 2, 3, 4);
        Fov fovRight = new Fov(5, 6, 7, 8);

        when(perceptionLibrary.getSession()).thenReturn(session);
        ViewProjection viewProjectionLeft =
                new ViewProjection(
                        RuntimeUtils.poseToPerceptionPose(new Pose()),
                        RuntimeUtils.perceptionFovFromFov(fovLeft));
        ViewProjection viewProjectionRight =
                new ViewProjection(
                        RuntimeUtils.poseToPerceptionPose(new Pose()),
                        RuntimeUtils.perceptionFovFromFov(fovRight));
        when(session.getStereoViews())
                .thenReturn(new ViewProjections(viewProjectionLeft, viewProjectionRight));

        CameraViewActivityPoseImpl cameraActivityPoseLeft =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_LEFT_EYE);
        assertThat(cameraActivityPoseLeft.getFov().angleLeft).isEqualTo(fovLeft.angleLeft);
        assertThat(cameraActivityPoseLeft.getFov().angleRight).isEqualTo(fovLeft.angleRight);
        assertThat(cameraActivityPoseLeft.getFov().angleUp).isEqualTo(fovLeft.angleUp);
        assertThat(cameraActivityPoseLeft.getFov().angleDown).isEqualTo(fovLeft.angleDown);

        CameraViewActivityPoseImpl cameraActivityPoseRight =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_RIGHT_EYE);
        assertThat(cameraActivityPoseRight.getFov().angleLeft).isEqualTo(fovRight.angleLeft);
        assertThat(cameraActivityPoseRight.getFov().angleRight).isEqualTo(fovRight.angleRight);
        assertThat(cameraActivityPoseRight.getFov().angleUp).isEqualTo(fovRight.angleUp);
        assertThat(cameraActivityPoseRight.getFov().angleDown).isEqualTo(fovRight.angleDown);
    }

    @Test
    public void transformPoseTo_returnsCorrectPose() {
        // Set the activity space to the root of the underlying OpenXR reference space.
        this.activitySpace.setOpenXrReferenceSpacePose(Matrix4.Identity);
        Pose poseLeft = new Pose(new Vector3(1, 2, 3), new Quaternion(1, 0, 0, 0));
        Pose poseRight = new Pose(new Vector3(4, 5, 6), new Quaternion(0, 1, 0, 0));
        Fov fov = new Fov(0, 0, 0, 0);

        when(perceptionLibrary.getSession()).thenReturn(session);
        ViewProjection viewProjectionLeft =
                new ViewProjection(
                        RuntimeUtils.poseToPerceptionPose(poseLeft),
                        RuntimeUtils.perceptionFovFromFov(fov));
        ViewProjection viewProjectionRight =
                new ViewProjection(
                        RuntimeUtils.poseToPerceptionPose(poseRight),
                        RuntimeUtils.perceptionFovFromFov(fov));
        when(session.getStereoViews())
                .thenReturn(new ViewProjections(viewProjectionLeft, viewProjectionRight));

        CameraViewActivityPoseImpl cameraActivityPoseLeft =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_LEFT_EYE);

        assertPose(cameraActivityPoseLeft.transformPoseTo(new Pose(), activitySpace), poseLeft);

        CameraViewActivityPoseImpl cameraActivityPoseRight =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_RIGHT_EYE);

        assertPose(cameraActivityPoseRight.transformPoseTo(new Pose(), activitySpace), poseRight);
    }

    @Test
    public void getActivitySpaceScale_returnsInverseOfActivitySpaceWorldScale() throws Exception {
        float activitySpaceScale = 5f;
        this.activitySpace.setOpenXrReferenceSpacePose(Matrix4.fromScale(activitySpaceScale));
        CameraViewActivityPoseImpl cameraActivityPoseLeft =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_LEFT_EYE);
        assertVector3(
                cameraActivityPoseLeft.getActivitySpaceScale(),
                new Vector3(1f, 1f, 1f).div(activitySpaceScale));

        CameraViewActivityPoseImpl cameraActivityPoseRight =
                createCameraViewActivityPose(CameraViewActivityPose.CAMERA_TYPE_RIGHT_EYE);
        assertVector3(
                cameraActivityPoseRight.getActivitySpaceScale(),
                new Vector3(1f, 1f, 1f).div(activitySpaceScale));
    }
}
