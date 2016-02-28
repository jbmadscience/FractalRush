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
package us.jbmadscience.fractalrush

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class MandelbrotSurfaceView(context: Context, viewVertexShaderCode: String, mandelbrotShaderCode: String) : GLSurfaceView(context) {

    private val _renderer: FractalRushRenderer
    private val _scaleDetector: ScaleGestureDetector

    init {

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        _renderer = FractalRushRenderer(viewVertexShaderCode, mandelbrotShaderCode)
        setRenderer(_renderer)
        _scaleDetector = ScaleGestureDetector(getContext(), ScaleListener())

        // Render the view only when there is a change in the drawing data
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    // Position represents focus while _twoFingerGesture is true and previous position otherwise
    internal var _previousX: Float = 0.toFloat()
    internal var _previousY: Float = 0.toFloat()

    private var _twoFingerGesture = false

    override fun onTouchEvent(e: MotionEvent): Boolean {
        _scaleDetector.onTouchEvent(e)

        when (e.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                _previousX = e.getX(0)
                _previousY = e.getY(0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                _twoFingerGesture = true

                _previousX = (e.getX(0) + e.getX(1)) / 2
                _previousY = (e.getY(0) + e.getY(1)) / 2
            }

            MotionEvent.ACTION_POINTER_UP -> {
                _twoFingerGesture = false

                val remainingFinger = 1 - e.actionIndex

                _previousX = e.getX(remainingFinger)
                _previousY = e.getY(remainingFinger)
            }

            MotionEvent.ACTION_MOVE -> {
                val tempX: Float
                val tempY: Float
                if (_twoFingerGesture) {
                    // If there are two points, track focus. Translations will be done when the focus changes
                    // For instance, when both fingers move in parallel, it should act as a pan
                    tempX = (e.getX(0) + e.getX(1)) / 2
                    tempY = (e.getY(0) + e.getY(1)) / 2
                } else {
                    // If there is only one point, track it
                    tempX = e.getX(0)
                    tempY = e.getY(0)
                }

                _renderer.translate((tempX - _previousX).toDouble(), (tempY - _previousY).toDouble())

                _previousX = tempX
                _previousY = tempY

                requestRender()
            }
        }

        return true

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            _renderer.scale(detector.scaleFactor.toDouble(), detector.focusX.toDouble(), detector.focusY.toDouble())
            return true
        }
    }
}
