package dai.android.bili;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

class WriteRunnable extends BaseRunnable {
    private static final String TAG = "WriteRunnable";

    private final OkHttpClient client; // = new OkHttpClient();

    private final String filePath;

    protected WriteRunnable(FMP4Save _preserve, String _filePath) {
        super(_preserve);
        filePath = _filePath;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.writeTimeout(15, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);

        client = builder.build();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (!fmp4Save.isStarted()) {
                Log.w(TAG, "A: WriteRunnable will stop by user");
                return;
            }

            if (TextUtils.isEmpty(fmp4Save.m4sHeader)) {
                Log.w(TAG, "No fmp4 file head will wait a litter time");
                try {
                    Thread.sleep(600);
                } catch (InterruptedException ignore) {
                }
                continue;
            }

            String strOutputFile = filePath + "fmp4-out.mp4";
            {
                File file = new File(filePath);
                file.mkdirs();

                File outputFile = new File(strOutputFile);
                if (outputFile.exists()) {
                    Log.w(TAG, "delete exist file: " + strOutputFile);
                    outputFile.delete();
                }
            }


            //
            // ---------- write the head file ----------
            //
            {
                String strHeaderUrl = FMP4Save.BASE_URL + fmp4Save.m4sHeader;
                Log.i(TAG, "Write head file: " + strHeaderUrl);

                Request request = new Request.Builder().url(strHeaderUrl).build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    Sink sink = Okio.sink(new File(strOutputFile));
                    BufferedSink bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());
                    bufferedSink.flush();
                    bufferedSink.close();

//                    FileOutputStream fos = new FileOutputStream(outputFile);
//                    InputStream is = response.body().byteStream();
//                    int len = 0;
//                    byte[] buffer = new byte[2048];
//                    while (-1 != (len = is.read(buffer))) {
//                        fos.write(buffer, 0, len);
//                    }
//                    fos.flush();
//                    fos.close();
//                    is.close();
                } catch (Exception e) {
                    Log.e(TAG, "Write file head filed.", e);
                    return;
                }
            }


            while (true) {
                if (!fmp4Save.isStarted()) {
                    Log.w(TAG, "B: WriteRunnable will stop by user");
                    return;
                }

                Long index = -1L;
                synchronized (fmp4Save.readPieceList) {
                    index = fmp4Save.readPieceList.poll();
                }
                if (index < 0) {
                    continue;
                }

                String pieceFile = FMP4Save.BASE_URL + index + ".m4s";

                Log.i(TAG, "Write file: " + pieceFile);

                Request request = new Request.Builder().get().url(pieceFile).build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();

//                    Sink sink = Okio.sink(new File(strOutputFile));
//                    BufferedSink bufferedSink = Okio.buffer(sink);
//                    bufferedSink.writeAll(response.body().source());
//                    bufferedSink.flush();
//                    bufferedSink.close();

                    FileOutputStream fos = new FileOutputStream(new File(strOutputFile), true);
                    InputStream is = response.body().byteStream();
                    int len = 0;
                    byte[] buffer = new byte[2048];
                    while (-1 != (len = is.read(buffer))) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    Log.w(TAG, "Write file filed.", e);
                }
            }

        }
    }
}
