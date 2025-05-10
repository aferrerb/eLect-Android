package com.appleia.elect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.appleia.elect.db.eLectSQLiteHelper;

import org.json.JSONObject;

import java.util.Map;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // … your DB copy + prefs logic …

        // Simply delay then go home
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomePageActivity.class)
                    .setData(getIntent().getData())  // carry forward any deep-link URI
            );
            finish();
        }, SPLASH_TIME);
    }
}

