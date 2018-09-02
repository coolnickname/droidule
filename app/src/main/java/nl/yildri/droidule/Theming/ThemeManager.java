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

        switch (sharedPref.getString("pref_applied_theme", "BLUE")) {
            case "RED":
                return R.style.RedTheme;

            case "BLUE":
                return R.style.BlueTheme;

            case "PASTELBLUE":
                return R.style.PastelBlueTheme;

            case "PURPLE":
                return R.style.PurpleTheme;

            case "PASTELPURPLE":
                return R.style.PastelPurpleTheme;

            case "GREEN":
                return R.style.GreenTheme;
        }

        return -1;
    }
}
