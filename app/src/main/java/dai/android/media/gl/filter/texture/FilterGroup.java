package dai.android.media.gl.filter.texture;


import java.util.ArrayList;
import java.util.List;

import dai.android.media.gl.canvas.BasicTexture;
import dai.android.media.gl.canvas.GLCanvas;
import dai.android.media.gl.canvas.RawTexture;
import dai.android.media.util.Loggers;

/**
 * Created by Chilling on 2016/10/27.
 */

public class FilterGroup extends BasicTextureFilter {
    private static final String TAG = "FilterGroup";


    protected List<TextureFilter> mFilters;
    protected List<TextureFilter> mMergedFilters;
    private final List<RawTexture> rawTextureList = new ArrayList<>();
    private BasicTexture outputTexture;
    private BasicTexture initialTexture;

    public FilterGroup(List<TextureFilter> mFilters) {
        this.mFilters = mFilters;
        updateMergedFilters();

    }

    private void createTextures(BasicTexture initialTexture) {
        recycleTextures();
        for (int i = 0; i < mMergedFilters.size(); i++) {
            rawTextureList.add(new RawTexture(initialTexture.getWidth(), initialTexture.getHeight(), false));
        }
    }

    private void recycleTextures() {
        for (RawTexture rawTexture : rawTextureList) {
            rawTexture.recycle();
        }
        rawTextureList.clear();
    }


    public BasicTexture draw(BasicTexture initialTexture, GLCanvas glCanvas, OnDrawListener onDrawListener) {
        if (initialTexture instanceof RawTexture) {
            if (!((RawTexture) initialTexture).isNeedInvalidate()) {
                return outputTexture;
            }
        } else if (this.initialTexture == initialTexture && outputTexture != null) {
            return outputTexture;
        }

        if (rawTextureList.size() != mMergedFilters.size() || this.initialTexture != initialTexture) {
            createTextures(initialTexture);
        }
        this.initialTexture = initialTexture;

        BasicTexture drawTexture = initialTexture;
        for (int i = 0, size = rawTextureList.size(); i < size; i++) {
            RawTexture rawTexture = rawTextureList.get(i);
            TextureFilter textureFilter = mMergedFilters.get(i);
            glCanvas.beginRenderTarget(rawTexture);
            onDrawListener.onDraw(drawTexture, textureFilter, i == 0);
            glCanvas.endRenderTarget();
            drawTexture = rawTexture;
        }
        outputTexture = drawTexture;

        return drawTexture;
    }

    @Override
    public void destroy() {
        super.destroy();
        Loggers.d(TAG, "destroy");
        recycleTextures();
    }

    public List<TextureFilter> getMergedFilters() {
        return mMergedFilters;
    }


    public void updateMergedFilters() {
        if (mFilters == null) {
            return;
        }

        if (mMergedFilters == null) {
            mMergedFilters = new ArrayList<>();
        } else {
            mMergedFilters.clear();
        }

        List<TextureFilter> filters;
        for (TextureFilter filter : mFilters) {
            if (filter instanceof FilterGroup) {
                ((FilterGroup) filter).updateMergedFilters();
                filters = ((FilterGroup) filter).getMergedFilters();
                if (filters == null || filters.isEmpty())
                    continue;
                mMergedFilters.addAll(filters);
                continue;
            }
            mMergedFilters.add(filter);
        }
    }

    public interface OnDrawListener {
        void onDraw(BasicTexture drawTexture, TextureFilter textureFilter, boolean isFirst);
    }
}
