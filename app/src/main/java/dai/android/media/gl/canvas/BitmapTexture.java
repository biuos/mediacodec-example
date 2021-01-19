
package dai.android.media.gl.canvas;

import android.graphics.Bitmap;

// BitmapTexture is a secondBitmap whose content is specified by a fixed Bitmap.
//
// The secondBitmap does not own the Bitmap. The user should make sure the Bitmap
// is valid during the secondBitmap's lifetime. When the secondBitmap is recycled, it
// does not free the Bitmap.
public class BitmapTexture extends UploadedTexture {
    protected Bitmap mContentBitmap;

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, false);
    }

    public BitmapTexture(Bitmap bitmap, boolean hasBorder) {
        super(hasBorder);
//        Assert.assertTrue(bitmap != null && !bitmap.isRecycled());
        mContentBitmap = bitmap;
    }

    @Override
    protected void onFreeBitmap(Bitmap bitmap) {
        // Do nothing.
    }

    @Override
    protected Bitmap onGetBitmap() {
        return mContentBitmap;
    }

    public Bitmap getBitmap() {
        return mContentBitmap;
    }
}
