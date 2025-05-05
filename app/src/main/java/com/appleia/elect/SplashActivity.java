package com.appleia.elect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import com.appleia.elect.db.eLectSQLiteHelper;


public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME = 2000; // 2 seconds
    private eLectSQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 1) Ensure your prepackaged DB is copied into place
        eLectSQLiteHelper dbHelper = new eLectSQLiteHelper(this);
        dbHelper.ensureDatabaseCopied();      // your method to copy from assets
        dbHelper.getReadableDatabase();       // actually open it (triggers onCreate/onUpgrade if needed)

        // ───────────────────────────────────────────────────────
        // 2) Use your helper to get the version and write prefs
        String currentDbVersion = dbHelper.fetchDBVersionFromDatabase();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("dbversion", currentDbVersion)
                .apply();
        // ───────────────────────────────────────────────────────

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomePageActivity.class));
            finish();
        }, SPLASH_TIME);
    }
}
