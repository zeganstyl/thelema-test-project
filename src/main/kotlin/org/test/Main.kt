package org.test

import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.*
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.texture.Texture2D
import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.gl.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val app = Lwjgl3App()

        val mesh = Mesh()

        val vertexInputs = VertexInputs(
                VertexInput(3, "aPosition", GL_FLOAT, true),
                VertexInput(2, "aUV", GL_FLOAT, true)
        )

        mesh.vertices = IVertexBuffer.build(
                DATA.bytes(4 * 5 * 4).apply {
                    floatView().apply {
                        // x, y, z    u, v
                        put(1f, 0f, 1f,   1f, 1f)
                        put(1f, 0f, -1f,   1f, 0f)
                        put(-1f, 0f, 1f,   0f, 1f)
                        put(-1f, 0f, -1f,   0f, 0f)
                    }
                }, vertexInputs)

        mesh.indices = IIndexBufferObject.build(
                DATA.bytes(6 * 2).apply {
                    shortView().apply {
                        put(
                                0, 1, 2,
                                3, 1, 2
                        )
                    }
                }, type = GL_UNSIGNED_SHORT)

        @Language("GLSL")
        val shader = Shader(
                vertCode = """
attribute vec3 aPosition;
attribute vec2 aUV;
varying vec2 vUV;
uniform mat4 projViewModelTrans;

void main() {
    vUV = aUV;
    gl_Position = projViewModelTrans * vec4(aPosition, 1.0);
}""",
                fragCode = """
varying vec2 vUV;
uniform sampler2D tex;

void main() {
    gl_FragColor = texture2D(tex, vUV);
}""")

        shader["tex"] = 0
        val texture = Texture2D(FS.internal("thelema-logo.png"))

        ActiveCamera.api = Camera(
                from = Vec3(0f, 2f, 2f),
                to = Vec3(0f, 0f, 0f),
                near = 0.1f,
                far = 100f
        )

        GL.isDepthTestEnabled = true
        GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        GL.setSimpleAlphaBlending()
        GL.isBlendEnabled = true

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            shader.bind()
            shader["projViewModelTrans"] = ActiveCamera.viewProjectionMatrix
            texture.bind(0)
            mesh.render(shader)
        }

        app.startLoop()
    }
}