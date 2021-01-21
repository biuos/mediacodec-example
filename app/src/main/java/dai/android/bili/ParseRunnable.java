package dai.android.bili;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import dai.android.debug.Logger;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dai.android.bili.FMP4Save.URL;

class ParseRunnable extends BaseRunnable {
    private static final String TAG = "ParseRunnable";

    private final OkHttpClient client = new OkHttpClient();
    private final Request request = new Request.Builder().url(URL).build();

    protected ParseRunnable(FMP4Save _preserve) {
        super(_preserve);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            synchronized (fmp4Save.startLock) {
                if (!fmp4Save.hadStarted) {
                    Logger.w(TAG, "stopped by user");
                    return;
                }
            }

            // 如果存放队列数量 > 6 就进行简单的 sleep操作
            boolean isQueueAbundant = false;
            synchronized (fmp4Save.readPieceList) {
                isQueueAbundant = fmp4Save.readPieceList.size() > 6;
            }
            if (isQueueAbundant) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                }
                continue;
            }

            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if (response.code() != 200) {
                    Log.w(TAG, "Get the m3u8 not good. code=" + response.code());
                    continue;
                }

                String content = null;
                if (null != response.body()) {
                    content = response.body().string();
                    m3u8Parse(content);
                }

            } catch (Exception e) {
                Logger.w(TAG, "OkHttpClient request failed", e);
            }
        }
    }

    private void m3u8Parse(String m3u8) {
        if (TextUtils.isEmpty(m3u8))
            return;

        String[] array = m3u8.split("\n");
        if (array.length <= 1) {
            return;
        }

        if (!"#EXTM3U".equals(array[0])) {
            Logger.w(TAG, "m3u8 not start #EXTM3U");
            return;
        }

        ArrayList<Long> parsedList = new ArrayList<>(10);
        for (int i = 1; i < array.length; ++i) {
            // parse start with #
            if (array[i].charAt(0) == '#') {
                if (!array[i].startsWith("#EXT-X-MAP:URI="))
                    continue;

                // parse the m4a head file
                // #EXT-X-MAP:URI="h33600080.m4s"
                int startPos = 16, endPos = array[i].length() - 1;
                String headerFile = array[i].substring(startPos, endPos);
                if (TextUtils.isEmpty(headerFile)) {
                    Logger.w(TAG, "No valid head in m3u8");
                    return;
                }
                if (fmp4Save.m4sHeader == null) {
                    fmp4Save.m4sHeader = headerFile;
                } else if (!fmp4Save.m4sHeader.equals(headerFile)) {
                    Logger.w(TAG, "File head has changed");
                    fmp4Save.headerFileChanged.set(true);
                    fmp4Save.m4sHeader = headerFile;
                }
            }

            // parse the real file index
            // 33600576.m4s
            int endPos = array[i].length() - 4;
            String strValue = array[i].substring(0, endPos);
            try {
                long value = Long.parseLong(strValue);
                parsedList.add(value);
            } catch (NumberFormatException ignore) {
            }
        }

        synchronized (fmp4Save.readPieceList) {
            for (Long it : parsedList) {
                // exits same piece skip it
                if (fmp4Save.readPieceList.contains(it))
                    continue;

                fmp4Save.readPieceList.offer(it);
            }
        }
    }
}
