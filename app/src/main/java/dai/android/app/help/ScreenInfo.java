package dai.android.app.help;

import android.content.Context;

public class ScreenInfo {

    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
