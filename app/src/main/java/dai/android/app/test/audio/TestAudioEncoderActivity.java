package dai.android.app.test.audio;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import dai.android.app.R;

public class TestAudioEncoderActivity extends AppCompatActivity {

    private TestAudioEncoder testAudioEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_audio_encoder);

        testAudioEncoder = new TestAudioEncoder(getApplicationContext());
        testAudioEncoder.prepareEncoder();
    }


    @Override
    protected void onPause() {
        super.onPause();
        testAudioEncoder.stop();
    }

    public void clickStartTest(View view) {
        TextView textView = (TextView) view;
        if (testAudioEncoder.isStart()) {
            testAudioEncoder.stop();
            textView.setText("RECORD");
        } else {
            testAudioEncoder.prepareEncoder();
            testAudioEncoder.start();
            textView.setText("PAUSE");
        }
    }
}