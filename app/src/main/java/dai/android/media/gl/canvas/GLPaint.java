package dai.android.media.gl.canvas;

import android.graphics.Color;
import android.graphics.Paint;


public class GLPaint {
    private float mLineWidth = 1f;
    private int mColor = Color.WHITE;
    private Paint.Style style = Paint.Style.FILL;

    public void setColor(int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public void setLineWidth(float width) {
        mLineWidth = width;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setStyle(Paint.Style style) {
        this.style = style;
    }

    public Paint.Style getStyle() {
        return style;
    }
}
