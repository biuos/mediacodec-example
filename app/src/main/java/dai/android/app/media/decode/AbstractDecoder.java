package dai.android.app.media.decode;

import android.media.MediaCodec;
import android.media.MediaExtractor;

public abstract class AbstractDecoder {

    protected final String dataSource;
    protected MediaCodec mediaCodec;
    protected MediaExtractor mediaExtractor;

    public AbstractDecoder(String url) {
        dataSource = url;
    }




}
