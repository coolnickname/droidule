package nl.yildri.droidule.Theming;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;

import nl.yildri.droidule.R;

/**
 * Created by Yildri on 04/11/2017.
 */

public class Colours {
    public static int primaryColor(Context context){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int colorPrimaryDark(Context context){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    public static int colorAccent(Context context){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }
}
