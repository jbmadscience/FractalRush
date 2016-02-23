/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.example.android.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MandelbrotSurfaceView extends GLSurfaceView {

    private final MandelbrotRenderer mRenderer;
    private ScaleGestureDetector mDetector;

    public MandelbrotSurfaceView(Context context, String viewVertexShaderCode, String mandelbrotShaderCode) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //setRenderMode(RENDERMODE_WHEN_DIRTY);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MandelbrotRenderer(viewVertexShaderCode, mandelbrotShaderCode);
        setRenderer(mRenderer);
        mDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        // Render the view only when there is a change in the drawing data
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    // Position represents focus while twoFingers is true and previous position otherwise
    float mPreviousX;
    float mPreviousY;

    private boolean twoFingers = false;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mDetector.onTouchEvent(e);

        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPreviousX = e.getX(0);
                mPreviousY = e.getY(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                twoFingers = true;

                mPreviousX = (e.getX(0) + e.getX(1)) / 2;
                mPreviousY = (e.getY(0) + e.getY(1)) / 2;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                twoFingers = false;

                int remainingFinger = 1 - e.getActionIndex();

                mPreviousX = e.getX(remainingFinger);
                mPreviousY = e.getY(remainingFinger);
                break;

            case MotionEvent.ACTION_MOVE:
                float tempX, tempY;
                if (twoFingers) {
                    // If there are two points, track focus. Translations will be done when the focus changes
                    // For instance, when both fingers move in parallel, it should act as a pan
                    tempX = (e.getX(0) + e.getX(1)) / 2;
                    tempY = (e.getY(0) + e.getY(1)) / 2;
                } else {
                    // If there is only one point, track it
                    tempX = e.getX(0);
                    tempY = e.getY(0);
                }

                mRenderer.add(tempX - mPreviousX, tempY - mPreviousY);

                mPreviousX = tempX;
                mPreviousY = tempY;

                requestRender();
                break;
        }

        return true;

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mRenderer.zoom(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }
}
