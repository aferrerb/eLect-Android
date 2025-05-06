package com.appleia.elect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.appleia.elect.db.eLectSQLiteHelper;

import org.json.JSONObject;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;

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

        // Branch logging for debugging
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Branch.sessionBuilder(this).withCallback(new Branch.BranchUniversalReferralInitListener() {
            @Override
            public void onInitFinished(BranchUniversalObject branchUniversalObject, LinkProperties linkProperties, BranchError error) {
                if (error != null) {
                    Log.e("BranchSDK_Tester", "branch init failed. Caused by -" + error.getMessage());
                } else {
                    Log.i("BranchSDK_Tester", "branch init complete!");
                    if (branchUniversalObject != null) {
                        Log.i("BranchSDK_Tester", "title " + branchUniversalObject.getTitle());
                        Log.i("BranchSDK_Tester", "CanonicalIdentifier " + branchUniversalObject.getCanonicalIdentifier());
                        Log.i("BranchSDK_Tester", "metadata " + branchUniversalObject.getContentMetadata().convertToJson());
                    }

                    if (linkProperties != null) {
                        Log.i("BranchSDK_Tester", "Channel " + linkProperties.getChannel());
                        Log.i("BranchSDK_Tester", "control params " + linkProperties.getControlParams());
                    }
                }
            }
        }).withData(this.getIntent().getData()).init();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        if (intent != null && intent.hasExtra("branch_force_new_session") && intent.getBooleanExtra("branch_force_new_session",false)) {
            Branch.sessionBuilder(this).withCallback(new Branch.BranchReferralInitListener() {
                @Override
                public void onInitFinished(JSONObject referringParams, BranchError error) {
                    if (error != null) {
                        Log.e("BranchSDK_Tester", error.getMessage());
                    } else if (referringParams != null) {
                        Log.i("BranchSDK_Tester", referringParams.toString());
                    }
                }
            }).reInit();
        }
    }
}

