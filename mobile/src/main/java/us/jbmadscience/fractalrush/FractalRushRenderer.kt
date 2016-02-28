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

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log

class FractalRushRenderer(private val _viewVertexShaderCode: String, private val _mandelbrotShaderCode: String) : GLSurfaceView.Renderer {
    private var _mandelbrot: Mandelbrot? = null

    private var _height: Int = 0
    private var _width: Int = 0

    private var _aspectRatio: Double = 0.toDouble()
    private var _realOrigin = 1.0
    private var _imaginaryOrigin = 1.0
    private var _scaleFactor = 0.5

    private var _zoomIncrease = 1.5

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        _mandelbrot = Mandelbrot(_viewVertexShaderCode, _mandelbrotShaderCode)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // MVPMatrix is an abbreviation for "Model View Projection Matrix"
        val mvpMatrix = floatArrayOf((-1.0 / _scaleFactor).toFloat(), 0.0f, 0.0f, 0.0f, 0.0f, (1.0 / (_scaleFactor * _aspectRatio)).toFloat(), 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, (-_imaginaryOrigin).toFloat(), (-_realOrigin).toFloat(), 0.0f, 1.0f)

        _mandelbrot!!.draw(mvpMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        _width = width
        _height = height

        GLES20.glViewport(0, 0, width, height)

        _aspectRatio = width.toDouble() / height
    }

    fun translate(dx: Double, dy: Double) {
        _imaginaryOrigin += dx / (_scaleFactor * _height)
        _realOrigin += dy / (_scaleFactor * _height)
    }

    fun scale(scaleFactor: Double, x: Double, y: Double) {
        val newScaleFactor = (scaleFactor - 1) * _zoomIncrease + 1

        val newX = x - (_width / 2).toDouble()

        val scale = Math.log(newScaleFactor)

        //Move towards new origin
        translate(-scale * newX, -scale * y)

        _scaleFactor *= newScaleFactor
    }

    companion object {

        private val TAG = "FractalRushRenderer"

        /**
         * Utility method for compiling a OpenGL shader.

         *
         * **Note:** When developing shaders, use the checkGlError()
         * method to debug shader coding errors.

         * @param type - Vertex or fragment shader type.
         * *
         * @param shaderCode - String containing the shader code.
         * *
         * @return - Returns an id for the shader.
         */
        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            val shader = GLES20.glCreateShader(type)

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }

        /**
         * Utility method for debugging OpenGL calls. Provide the name of the call
         * just after making it:

         *
         * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
         * FractalRushRenderer.checkGlError("glGetUniformLocation");

         * If the operation is not successful, the check throws an error.

         * @param glOperation - Name of the OpenGL call to check.
         */
        fun checkGlError(glOperation: String) {
            val error = GLES20.glGetError();
            if (error != GLES20.GL_NO_ERROR) {
                Log.e(TAG, glOperation + ": glError " + error)
                throw RuntimeException(glOperation + ": glError " + error)
            }
        }
    }
}