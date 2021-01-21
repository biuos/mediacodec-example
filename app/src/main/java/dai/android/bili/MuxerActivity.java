package dai.android.bili;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import dai.android.app.R;

public class MuxerActivity extends AppCompatActivity {

    private Button downloading = null;
    private FMP4Save muxer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muxer);

        muxer = new FMP4Save(saveFilePath());

        downloading = findViewById(R.id.downloading);
        downloading.setOnClickListener(mOnClickListener);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.downloading) {
                dealDownloadClick();
            }
        }
    };

    private void dealDownloadClick() {
        if (null == muxer) {
            return;
        }

        if (muxer.isStarted()) {
            muxer.stop();
            downloading.setText(getString(R.string.strGoToDownload));
        } else {
            muxer.start();
            downloading.setText(getString(R.string.strStopDownload));
        }
    }

    private String saveFilePath() {
        return getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString() + "/";
    }
}
