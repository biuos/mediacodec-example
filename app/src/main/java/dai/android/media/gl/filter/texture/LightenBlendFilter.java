package dai.android.media.gl.filter.texture;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;


public class LightenBlendFilter extends TwoTextureFilter {
    public static final String LIGHTEN_BLEND_FRAGMENT_SHADER =
            "precision mediump float; \n" +
            "varying vec2 " + VARYING_TEXTURE_COORD + ";\n" +
            " varying vec2 " + VARYING_TEXTURE_COORD2 + ";\n" +
            "\n" +
            " uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM + ";\n" +
            " uniform sampler2D " + UNIFORM_TEXTURE_SAMPLER2 + ";\n" +
            " \n" +
            " void main() {\n" +
            " " +
            "    lowp vec4 textureColor = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", " + VARYING_TEXTURE_COORD + ");\n" +
            "    lowp vec4 textureColor2 = texture2D(" + UNIFORM_TEXTURE_SAMPLER2 + ", " + VARYING_TEXTURE_COORD2 + ");\n" +
            "    \n" +
            "    gl_FragColor = max(textureColor, textureColor2);\n" +
            " }";


    public LightenBlendFilter(@NonNull Bitmap bitmap) {
        super(bitmap);
    }

    public LightenBlendFilter() {
    }

    @Override
    public String getFragmentShader() {
        return LIGHTEN_BLEND_FRAGMENT_SHADER;
    }
}
