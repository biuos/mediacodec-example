package dai.android.app.test.audio;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dai.android.debug.Logger;
import dai.android.media.encoder.MediaCodecInputStream;
import dai.android.media.encoder.StreamPublisher;
import dai.android.media.encoder.audio.AACEncoder;

public class TestAudioEncoder {

    private AACEncoder aacEncoder;
    private byte[] writeBuffer = new byte[1024 * 64];
    private OutputStream os;
    private boolean isStart;

    public TestAudioEncoder(Context ctx) {

        try {
            os = new FileOutputStream(ctx.getExternalFilesDir(null) + File.separator + "test_aac_encode.aac");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void prepareEncoder() {
        try {
            StreamPublisher.Parameter.Builder builder = new StreamPublisher.Parameter.Builder();
            builder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            StreamPublisher.Parameter streamPublisherParam = builder.createStreamPublisherParam();
            aacEncoder = new AACEncoder(streamPublisherParam);
            aacEncoder.setOnDataComingCallback(new AACEncoder.OnDataComingCallback() {
                @Override
                public void onComing() {
                    write();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (!isStart) {
            aacEncoder.start();
            isStart = true;
        }
    }


    public void stop() {
        isStart = false;
        if (aacEncoder != null) {
            aacEncoder.close();
            aacEncoder = null;
        }
    }

    public boolean isStart() {
        return isStart;
    }

    public void write() {
        MediaCodecInputStream mediaCodecInputStream = aacEncoder.getMediaCodecInputStream();
        MediaCodecInputStream.readAll(mediaCodecInputStream, writeBuffer, new MediaCodecInputStream.OnReadAllCallback() {
            boolean shouldAddPacketHeader = true;
            byte[] header = new byte[7];

            @Override
            public void onReadOnce(byte[] buffer, int readSize, MediaCodec.BufferInfo bufferInfo) {
                if (readSize <= 0) {
                    return;
                }
                try {
                    Logger.d("TestAudioEncoder", String.format("onReadOnce: readSize:%d, bufferInfo:%d", readSize, bufferInfo.size));
                    if (shouldAddPacketHeader) {
                        Logger.d("TestAudioEncoder", String.format("onReadOnce: add packet header"));
                        AACEncoder.addADTStoPacket(header, 7 + bufferInfo.size);
                        os.write(header);
                    }
                    os.write(buffer, 0, readSize);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                shouldAddPacketHeader = readSize >= bufferInfo.size;
            }
        });
    }


}
