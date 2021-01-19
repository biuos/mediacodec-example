
package dai.android.media.gl.filter.texture;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class DarkenBlendFilter extends TwoTextureFilter {

    public static final String DARKEN_BLEND_FRAGMENT_SHADER =
            "precision mediump float; \n" +
            "varying vec2 " + VARYING_TEXTURE_COORD + ";\n" +
            " varying vec2 " + VARYING_TEXTURE_COORD2 + ";\n" +
            "\n" +
            " uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM + ";\n" +
            " uniform sampler2D " + UNIFORM_TEXTURE_SAMPLER2 + ";\n" +
            " \n" +
            " void main() {\n" +
            " " +
            "    lowp vec4 base = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", " + VARYING_TEXTURE_COORD + ");\n" +
            "    lowp vec4 overlayer = texture2D(" + UNIFORM_TEXTURE_SAMPLER2 + ", " + VARYING_TEXTURE_COORD2 + ");\n" +
            "    \n" +
            "    gl_FragColor = vec4(min(overlayer.rgb * base.a, base.rgb * overlayer.a) + overlayer.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlayer.a), 1.0);\n" +
            " }";

    public DarkenBlendFilter(@NonNull Bitmap bitmap) {
        super(bitmap);
    }

    public DarkenBlendFilter() {
        super();
    }

    @Override
    public String getFragmentShader() {
        return DARKEN_BLEND_FRAGMENT_SHADER;
    }
}
