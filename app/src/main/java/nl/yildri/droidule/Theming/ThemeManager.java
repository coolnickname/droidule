package nl.yildri.droidule.Theming;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import nl.yildri.droidule.Droidule;
import nl.yildri.droidule.R;


/**
 * Created by Yildri on 04/11/2017.
 */

public class ThemeManager {

    public static int getTheme(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Droidule.getContext());
        int theme = -1;

        switch (sharedPref.getString("pref_applied_theme", "XEDROID")) {
            case "XEDROID":
                theme = R.style.XedroidTheme;
                break;
            case "BLUE":
                theme = R.style.BlueTheme;
                break;
            case "PASTEL":
                theme = R.style.PastelTheme;
                break;
        }

        return theme;
    }
}
