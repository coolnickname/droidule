package nl.yildri.droidule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import nl.yildri.droidule.Theming.ThemeManager;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Update theme
        setTheme(ThemeManager.getTheme());

        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Update theme
        setTheme(ThemeManager.getTheme());

    }
}
