
package dai.android.media.gl.filter.texture;

import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.canvas.BasicTexture;
import dai.android.media.gl.canvas.GLES20Canvas;

public class BasicTextureFilter implements TextureFilter {


    public static final String MATRIX_UNIFORM = GLES20Canvas.MATRIX_UNIFORM;
    public static final String TEXTURE_MATRIX_UNIFORM = GLES20Canvas.TEXTURE_MATRIX_UNIFORM;
    public static final String POSITION_ATTRIBUTE = GLES20Canvas.POSITION_ATTRIBUTE;

    public static final String VARYING_TEXTURE_COORD = "vTextureCoord";

    public static final String TEXTURE_VERTEX_SHADER = ""
            + "uniform mat4 " + MATRIX_UNIFORM + ";\n"
            + "uniform mat4 " + TEXTURE_MATRIX_UNIFORM + ";\n"
            + "attribute vec2 " + POSITION_ATTRIBUTE + ";\n"
            + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
            + "void main() {\n"
            + "  vec4 pos = vec4(" + POSITION_ATTRIBUTE + ", 0.0, 1.0);\n"
            + "  gl_Position = " + MATRIX_UNIFORM + " * pos;\n"
            + "  " + VARYING_TEXTURE_COORD + " = (" + TEXTURE_MATRIX_UNIFORM + " * pos).xy;\n"
            + "}\n";

    public static final String ALPHA_UNIFORM = GLES20Canvas.ALPHA_UNIFORM;
    public static final String TEXTURE_SAMPLER_UNIFORM = GLES20Canvas.TEXTURE_SAMPLER_UNIFORM;

    public static final String SAMPLER_2D = "sampler2D";
    public static final String TEXTURE_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
            + "uniform float " + ALPHA_UNIFORM + ";\n"
            + "uniform " + SAMPLER_2D + " " + TEXTURE_SAMPLER_UNIFORM + ";\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", " + VARYING_TEXTURE_COORD + ");\n"
            + "  gl_FragColor *= " + ALPHA_UNIFORM + ";\n"
            + "}\n";
    public static final String SAMPLER_EXTERNAL_OES = "samplerExternalOES";

    @Override
    public String getVertexShader() {
        return TEXTURE_VERTEX_SHADER;
    }

    @Override
    public String getFragmentShader() {
        return TEXTURE_FRAGMENT_SHADER;
    }

    @Override
    public String getOesFragmentProgram() {
        return "#extension GL_OES_EGL_image_external : require\n" + getFragmentShader().replace(SAMPLER_2D, SAMPLER_EXTERNAL_OES);
    }

    @Override
    public void onPreDraw(int program, BasicTexture texture, ICanvasGL canvas) {
    }

    @Override
    public void destroy() {

    }
}
