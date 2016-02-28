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

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.View

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : Activity() {

    private var _glView: GLSurfaceView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        _glView = MandelbrotSurfaceView(this,
                readRawTextFile(this, R.raw.view_vertex_shader),
                readRawTextFile(this, R.raw.mandelbrot))
        setContentView(_glView)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //Change to immersive mode. This is only available in kitkat and later, so check the os version
        if (hasFocus && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }


    override fun onPause() {
        super.onPause()

        _glView!!.onPause()
    }

    override fun onResume() {
        super.onResume()

        _glView!!.onResume()
    }

    companion object {

        fun readRawTextFile(ctx: Context, resId: Int): String {
            val inputStream = ctx.resources.openRawResource(resId)

            val inputreader = InputStreamReader(inputStream)
            val buffreader = BufferedReader(inputreader)
            val text = StringBuilder()

            buffreader.forEachLine { line ->
                text.append(line)
                text.append('\n')
            }

            return text.toString()
        }
    }
}