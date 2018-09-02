package nl.yildri.droidule.Util;

import android.app.Activity;
import android.widget.Toast;

public class MessageUtil {
    public static void showToast(String text, Activity activity){
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
    }
}
