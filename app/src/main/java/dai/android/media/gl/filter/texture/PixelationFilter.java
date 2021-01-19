package dai.android.media.gl.filter.texture;

import android.opengl.GLES20;

import androidx.annotation.FloatRange;

import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.OpenGLUtil;
import dai.android.media.gl.canvas.BasicTexture;

public class PixelationFilter extends BasicTextureFilter implements OneValueFilter {

    public static final String UNIFORM_IMAGE_WIDTH_FACTOR = "imageWidthFactor";
    public static final String UNIFORM_IMAGE_HEIGHT_FACTOR = "imageHeightFactor";
    public static final String UNIFORM_PIXEL = "pixel";
    public static final String PIXELATION_FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            " varying vec2 " + VARYING_TEXTURE_COORD + ";\n" +
            "uniform float " + UNIFORM_IMAGE_WIDTH_FACTOR + ";\n" +
            "uniform float " + UNIFORM_IMAGE_HEIGHT_FACTOR + ";\n" +
            " uniform float " + ALPHA_UNIFORM + ";\n" +
            "uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM + ";\n" +
            "uniform float " + UNIFORM_PIXEL + ";\n" +
            "void main() {\n" +
            "" +
            "  vec2 uv  = " + VARYING_TEXTURE_COORD + ".xy;\n" +
            "  float dx = " + UNIFORM_PIXEL + " * " + UNIFORM_IMAGE_WIDTH_FACTOR + ";\n" +
            "  float dy = " + UNIFORM_PIXEL + " * " + UNIFORM_IMAGE_HEIGHT_FACTOR + ";\n" +
            "  vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
            "  vec4 tc = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", coord);\n" +
            "  gl_FragColor = vec4(tc);\n" +
            "    gl_FragColor *= " + ALPHA_UNIFORM + ";\n" +
            "}";

    private int mImageWidthFactorLocation;
    private int mImageHeightFactorLocation;
    private int mPixelLocation;
    private float mPixel;

    public PixelationFilter(@FloatRange(from = 1, to = 100) float pixel) {
        this.mPixel = pixel;
    }

    @Override
    public String getFragmentShader() {
        return PIXELATION_FRAGMENT_SHADER;
    }

    @Override
    public void onPreDraw(int program, BasicTexture texture, ICanvasGL canvas) {
        super.onPreDraw(program, texture, canvas);
        mImageWidthFactorLocation = GLES20.glGetUniformLocation(program, UNIFORM_IMAGE_WIDTH_FACTOR);
        mImageHeightFactorLocation = GLES20.glGetUniformLocation(program, UNIFORM_IMAGE_HEIGHT_FACTOR);
        mPixelLocation = GLES20.glGetUniformLocation(program, UNIFORM_PIXEL);

        OpenGLUtil.setFloat(mImageWidthFactorLocation, 1.0f / texture.getWidth());
        OpenGLUtil.setFloat(mImageHeightFactorLocation, 1.0f / texture.getHeight());
        OpenGLUtil.setFloat(mPixelLocation, mPixel);
    }

    @Override
    public void setValue(@FloatRange(from = 1, to = 100) final float pixel) {
        mPixel = pixel;
    }
}
