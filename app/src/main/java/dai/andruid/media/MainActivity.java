package dai.andruid.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import dai.andruid.media.decode.Decoder;

public class MainActivity extends AppCompatActivity {

    private static final String VideoFile = "/sdcard/v1080.mp4";
    private static final int PERMISSION_EXTERNAL_STORAGE = 1;

    private SurfaceView surfaceView;
    private MediaCodecPlayer player;
    private boolean permission = false;
    private Decoder decoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permission = checkReadExternalStoragePermission();

        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != decoder) {
            decoder.stop();
            decoder = null;
        }
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
//            if (null == player) {
//                player = new MediaCodecPlayer(VideoFile, holder.getSurface());
//                player.start();
//            }
            if (null == decoder) {
                decoder = new Decoder();
                decoder.setDataSource(VideoFile);
                decoder.setSurface(holder.getSurface());
                decoder.start();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
//            if (null != player) {
//                player.stop();
//            }

            if (null != decoder) {
                decoder.stop();
            }
        }
    };

    private boolean checkReadExternalStoragePermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_EXTERNAL_STORAGE);
        }
        return permission == PackageManager.PERMISSION_GRANTED;
    }
}