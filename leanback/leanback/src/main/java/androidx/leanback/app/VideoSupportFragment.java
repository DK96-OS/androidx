/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package androidx.leanback.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Subclass of {@link PlaybackSupportFragment} that is responsible for providing a {@link SurfaceView}
 * and rendering video.
 * See base class {@link PlaybackSupportFragment} for how to setup controls and how to
 * customize showing/hiding controls.
 */
public class VideoSupportFragment extends PlaybackSupportFragment {
    static final int SURFACE_NOT_CREATED = 0;
    static final int SURFACE_CREATED = 1;

    SurfaceView mVideoSurface;
    SurfaceHolder.Callback mMediaPlaybackCallback;

    int mState = SURFACE_NOT_CREATED;

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        mVideoSurface = (SurfaceView) LayoutInflater.from(getContext()).inflate(
                R.layout.lb_video_surface, root, false);
        root.addView(mVideoSurface, 0);
        mVideoSurface.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMediaPlaybackCallback != null) {
                    mMediaPlaybackCallback.surfaceCreated(holder);
                }
                mState = SURFACE_CREATED;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlaybackCallback != null) {
                    mMediaPlaybackCallback.surfaceChanged(holder, format, width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mMediaPlaybackCallback != null) {
                    mMediaPlaybackCallback.surfaceDestroyed(holder);
                }
                mState = SURFACE_NOT_CREATED;
            }
        });
        setBackgroundType(PlaybackSupportFragment.BG_LIGHT);
        return root;
    }

    /**
     * Adds {@link SurfaceHolder.Callback} to {@link android.view.SurfaceView}.
     */
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        mMediaPlaybackCallback = callback;

        if (callback != null) {
            if (mState == SURFACE_CREATED) {
                mMediaPlaybackCallback.surfaceCreated(mVideoSurface.getHolder());
            }
        }
    }

    @Override
    protected void onVideoSizeChanged(int videoWidth, int videoHeight) {
        int screenWidth = getView().getWidth();
        int screenHeight = getView().getHeight();

        ViewGroup.LayoutParams p = mVideoSurface.getLayoutParams();
        if (screenWidth * videoHeight > videoWidth * screenHeight) {
            // fit in screen height
            p.height = screenHeight;
            p.width = screenHeight * videoWidth / videoHeight;
        } else {
            // fit in screen width
            p.width = screenWidth;
            p.height = screenWidth * videoHeight / videoWidth;
        }
        mVideoSurface.setLayoutParams(p);
    }

    /**
     * Returns the surface view.
     */
    public SurfaceView getSurfaceView() {
        return mVideoSurface;
    }

    @Override
    public void onDestroyView() {
        mVideoSurface = null;
        mState = SURFACE_NOT_CREATED;
        super.onDestroyView();
    }
}
