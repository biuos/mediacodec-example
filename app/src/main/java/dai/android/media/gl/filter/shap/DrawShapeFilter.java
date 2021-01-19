package dai.android.media.gl.filter.shap;

import dai.android.media.gl.ICanvasGL;

public interface DrawShapeFilter {
    String getVertexShader();

    String getFragmentShader();

    void onPreDraw(int program, ICanvasGL canvas);

    void destroy();
}
