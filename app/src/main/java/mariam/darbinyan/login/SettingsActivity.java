package mariam.darbinyan.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchCompat darkModeSwitch = findViewById(R.id.switch_dark_mode);
        LinearLayout aboutBtn = findViewById(R.id.btn_about);


        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        darkModeSwitch.setChecked(isDarkMode);


        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }


            sharedPreferences.edit().putBoolean("DarkMode", isChecked).apply();
        });

        aboutBtn.setOnClickListener(v ->
                Toast.makeText(this, "My Look: Your Digital Wardrobe Assistant", Toast.LENGTH_LONG).show()
        );
    }
}