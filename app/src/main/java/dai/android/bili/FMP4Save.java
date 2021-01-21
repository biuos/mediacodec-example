package dai.android.bili;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FMP4Save {
    private static final String TAG = "FMP4Save";

    // B 站 6 号房间
    static final String BASE_URL = "http://d1--cn-gotcha204.bilivideo.com/live-bvc/157654/live_50329118_9516950/";
    static final String URL = "http://d1--cn-gotcha204.bilivideo.com/live-bvc/157654/live_50329118_9516950/index.m3u8?expires=1611133684&len=0&oi=3030954244&pt=android&qn=250&trid=7dd884bcf7cb47f0833f58371f3e1556&sigparams=cdn,expires,len,oi,pt,qn,trid&cdn=cn-gotcha04&sign=468d5b195daea1216ddd66d658290a5e&p2p_type=1&src=13&sl=3";

    final Object startLock = new Object();
    boolean hadStarted = false;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    // save the m4s file header in m3u8
    volatile String m4sHeader = null;
    final AtomicBoolean headerFileChanged = new AtomicBoolean(false);

    final Queue<Long> readPieceList = new LinkedList<>();


    private final ParseRunnable parseRunnable;
    private final WriteRunnable writeRunnable;


    public FMP4Save(String path) {
        parseRunnable = new ParseRunnable(this);
        writeRunnable = new WriteRunnable(this, path);
    }


    public boolean isStarted() {
        synchronized (startLock) {
            return hadStarted;
        }
    }


    public void start() {
        synchronized (startLock) {
            if (!hadStarted) {
                threadPool.submit(parseRunnable);
                threadPool.submit(writeRunnable);

                hadStarted = true;
            }
        }
    }

    public void stop() {
        synchronized (startLock) {
            if (hadStarted) {
                hadStarted = false;
            }
        }
    }
}

