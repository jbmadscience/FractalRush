package us.jbmadscience.fractalrush

import android.opengl.GLES20

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by Joshua on 2/21/2016.
 */
class Mandelbrot(viewVertexShaderCode: String, mandelbrotShaderCode: String) {
    private val _vertexBuffer: FloatBuffer
    private val _drawListBuffer: ShortBuffer
    private val _program: Int
    private var _positionHandle: Int = 0
    private var _mvpMatrixHandle: Int = 0

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices
    private val vertexRecordSize = DIMENSIONS * 4 // 4 bytes per vertex

    init {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                viewVertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        _vertexBuffer = bb.asFloatBuffer()
        _vertexBuffer.put(viewVertices)
        _vertexBuffer.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        _drawListBuffer = dlb.asShortBuffer()
        _drawListBuffer.put(drawOrder)
        _drawListBuffer.position(0)

        // Prepare shaders
        val vertexShader = FractalRushRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                viewVertexShaderCode)
        val fragmentShader = FractalRushRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                mandelbrotShaderCode)

        _program = GLES20.glCreateProgram()

        GLES20.glAttachShader(_program, vertexShader)
        GLES20.glAttachShader(_program, fragmentShader)
        GLES20.glLinkProgram(_program)
    }

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(_program)

        // get handle to vertex shader's vPosition member
        _positionHandle = GLES20.glGetAttribLocation(_program, "vPosition")
        _mvpMatrixHandle = GLES20.glGetUniformLocation(_program, "uMVPMatrix")

        GLES20.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glEnableVertexAttribArray(_positionHandle)
        GLES20.glVertexAttribPointer(
                _positionHandle, DIMENSIONS,
                GLES20.GL_FLOAT, false,
                vertexRecordSize, _vertexBuffer)

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.size,
                GLES20.GL_UNSIGNED_SHORT, _drawListBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(_positionHandle)
    }

    companion object {

        // number of coordinates per vertex in this array
        private val DIMENSIONS = 3

        private val viewVertices = floatArrayOf(-1.0f, 1.0f, 0.0f, // top left
                -1.0f, -1.0f, 0.0f, // bottom left
                1.0f, -1.0f, 0.0f, // bottom right
                1.0f, 1.0f, 0.0f) // top right
    }


}
