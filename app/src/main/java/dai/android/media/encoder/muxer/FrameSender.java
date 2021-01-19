package dai.android.media.encoder.muxer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.LinkedList;
import java.util.List;

public class FrameSender {
    private static final int KEEP_COUNT = 30;
    private static final int MESSAGE_READY_TO_CLOSE = 4;
    private static final int MSG_ADD_FRAME = 3;
    private static final int MSG_START = 2;

    private List<FramePool.Frame> frameQueue = new LinkedList<>();
    private FramePool framePool = new FramePool(KEEP_COUNT + 10);

    private Handler sendHandler;
    private ICallback callback;


    public FrameSender(final ICallback _callback) {
        this.callback = _callback;

        final HandlerThread sendHandlerThread = new HandlerThread("FrameSendThread");
        sendHandlerThread.start();
        sendHandler = new Handler(sendHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == MESSAGE_READY_TO_CLOSE) {
                    if (msg.obj != null) {
                        addFrame((FramePool.Frame) msg.obj);
                    }
                    sendFrame(msg.arg1);

                    _callback.close();
                    sendHandlerThread.quitSafely();
                } else if (msg.what == MSG_ADD_FRAME) {
                    if (msg.obj != null) {
                        addFrame((FramePool.Frame) msg.obj);
                    }
                    sendFrame(msg.arg1);
                } else if (msg.what == MSG_START) {
                    _callback.onStart();
                }
            }
        };
    }


    private void addFrame(FramePool.Frame frame) {
        frameQueue.add(frame);
        FramePool.Frame.sortFrame(frameQueue);
    }

    private void sendFrame(int keepCount) {
        while (frameQueue.size() > keepCount) {
            FramePool.Frame sendFrame = frameQueue.remove(0);
            if (sendFrame.type == FramePool.Frame.TYPE_VIDEO) {
                callback.onSendVideo(sendFrame);
            } else if (sendFrame.type == FramePool.Frame.TYPE_AUDIO) {
                callback.onSendAudio(sendFrame);
            }
            framePool.release(sendFrame);
        }
    }

    public void sendStartMessage() {
        Message message = Message.obtain();
        message.what = MSG_START;
        sendHandler.sendMessage(message);
    }

    public void sendAddFrameMessage(byte[] data, int offset, int length, BufferInfoEx bufferInfo, int type) {
        FramePool.Frame frame = framePool.obtain(data, offset, length, bufferInfo, type);
        Message message = Message.obtain();
        message.what = MSG_ADD_FRAME;
        message.obj = frame;
        message.arg1 = KEEP_COUNT;
        sendHandler.sendMessage(message);
    }

    public void sendCloseMessage() {
        Message message = Message.obtain();
        message.arg1 = 0;
        message.what = MESSAGE_READY_TO_CLOSE;
        sendHandler.sendMessage(message);
    }

    public interface ICallback {
        void onStart();

        void onSendVideo(FramePool.Frame sendFrame);

        void onSendAudio(FramePool.Frame sendFrame);

        void close();
    }
}
