
package dai.android.media.gl.filter.texture;

import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.canvas.BasicTexture;

public interface TextureFilter {
    String getVertexShader();

    String getFragmentShader();

    String getOesFragmentProgram();

    void onPreDraw(int program, BasicTexture texture, ICanvasGL canvas);

    void destroy();
}
