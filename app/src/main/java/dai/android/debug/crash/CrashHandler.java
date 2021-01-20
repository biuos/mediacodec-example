package dai.android.debug.crash;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final CrashHandler instance = new CrashHandler();
    private final Thread.UncaughtExceptionHandler defaultHandler;

    private static Context application;
    private static String crashPath;

    public static CrashHandler init(Context applicationContext) {
        if (null == application) {
            application = applicationContext.getApplicationContext();
            crashPath = application.getExternalFilesDir(null) + File.separator + "crash";
        }
        return instance;
    }

    private static String convertYYMMDDHHmm(long time) {
        Date date = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        return dateFormat.format(date);
    }

    private CrashHandler() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private boolean handleException(Throwable tr) {
        if (tr == null) {
            return false;
        }
        saveErrorInfo(tr);
        return true;
    }

    private void saveErrorInfo(Throwable tr) {
        File path = new File(crashPath);
        if (!path.exists()) {
            path.mkdirs();
        }

        File file = new File(crashPath + File.separator + "log_" + convertYYMMDDHHmm(System.currentTimeMillis()) + ".txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            Writer writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            tr.printStackTrace(pw);
            pw.close();
            String error = writer.toString();
            fos.write(error.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable tr) {
        handleException(tr);
        defaultHandler.uncaughtException(thread, tr);
    }
}
