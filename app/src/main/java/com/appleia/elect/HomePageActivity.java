package com.appleia.elect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.gridlayout.widget.GridLayout;
import android.view.ViewGroup;
import android.content.Intent;
import com.appleia.elect.CatalogueActivity;
import com.appleia.elect.db.eLectSQLiteHelper;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.validators.IntegrationValidator;

public class HomePageActivity extends AppCompatActivity {

    private eLectSQLiteHelper dbHelper;

    private final String[] titles = {
            "Catalogue", "Progressive Plan", "Books Read", "Wishlist",
            "Search", "Suggest a Book", "Why Read?", "Info"
    };

    private final int[] icons = {
            R.drawable.catalogue, R.drawable.trending,
            R.drawable.book_read, R.drawable.book_to_read,
            R.drawable.search, R.drawable.scanner,
            R.drawable.why_read, R.drawable.info
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full-screen flags if you still want them…
        setContentView(R.layout.activity_home_page);

        GridLayout grid = findViewById(R.id.iconGrid);
        grid.setColumnCount(2);
        grid.setRowCount(4);
        LayoutInflater inflater = getLayoutInflater();


        for (int i = 0; i < titles.length; i++) {
            View item = inflater.inflate(R.layout.home_icon_item, grid, false);
            ImageView img = item.findViewById(R.id.iconImage);
            TextView lbl  = item.findViewById(R.id.iconLabel);
            img.setImageResource(icons[i]);
            img.setContentDescription(titles[i]);
            lbl.setText(titles[i]);
            final int idx = i;
            item.setOnClickListener(v -> handleIconTap(idx));

            // *** Crucial: 0dp + 1f weight on BOTH width and height ***
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            params.width  = 0;   // share equally across columns
            params.height = 0;   // share equally across rows
            params.setMargins(8,8,8,8);
            item.setLayoutParams(params);

            grid.addView(item);
        }
    }

    private void handleIconTap(int index) {

        switch (index) {
            case 0:
                // Catalogue
                startActivity(new Intent(this, CatalogueActivity.class));
                break;
            case 1:
                // Progressive Plan
                startActivity(new Intent(this, ProgressivePlanActivity.class));
                break;
            case 2:
                // Books Read
                startActivity(new Intent(this, BooksReadActivity.class));
                break;
            case 3:
                // Wishlist
                startActivity(new Intent(this, WishListActivity.class));
                break;
            // …and so on for the other tiles
            case 4:
                // Search Activity
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case 5:
                showSuggestDialog();
                break;
            //
            case 6:
                startActivity(new Intent(this, WhyReadActivity.class));
                break;
            case 7:
                showAppInfoDialog();
                break;
                // To show App Information
                // TODO: navigate based on index…
        }
    }
    private void showSuggestDialog() {
        String[] options = {
                "Send an email with suggestion",
                "Scan ISBN",
                "Cancel"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Suggest a Book")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:  // Send email
                            // reuse your ScanAndSuggestActivity’s suggestion flow:
                            Intent manual = new Intent(HomePageActivity.this, ScanAndSuggestActivity.class);
                            manual.putExtra("mode", "manual");
                            startActivity(manual);
                            break;
                        case 1:  // Scan ISBN
                            Intent scan = new Intent(HomePageActivity.this, ScanAndSuggestActivity.class);
                            scan.putExtra("mode", "scan");
                            startActivity(scan);
                            break;
                        case 2:  // Cancel
                        default:
                            dialog.dismiss();
                    }
                });
        builder.show();
    }
    private void showAppInfoDialog() {
        // 1) Grab version & build
        String versionName = "N/A";
        int versionCode = -1;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            // versionCode is deprecated on newer APIs; if yours returns a long, cast or use pInfo.getLongVersionCode()
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 2) Build the main “About” message
        String infoMessage = "App Version: " + versionName +
                "\nBuild Number: " + versionCode +
                "\n\nPurpose:\nThis app helps users select a new spiritual reading book.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("About This App")
                .setMessage(infoMessage)

                // “Check Database Version” button
                .setPositiveButton("Check Database Version", (dialog, which) -> {
                   // DBManager dbManager = new DBManager(this);
                    dbHelper = new eLectSQLiteHelper(this);
                    String dbVersionFromDatabase = dbHelper
                            .fetchDBVersionFromDatabase()
                            .trim();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    String dbVersionFromDefaults = prefs
                            .getString("dbversion", "")
                            .trim();

                    if (!dbVersionFromDatabase.equals(dbVersionFromDefaults)) {
                        new AlertDialog.Builder(this)
                                .setTitle("Update Required")
                                .setMessage("The database version is outdated. Please update the database.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Database Up-to-Date")
                                .setMessage("The database is already up-to-date.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })

                // “Contact Support” button
                .setNeutralButton("Contact Support", (dialog, which) -> {
                    Intent email = new Intent(Intent.ACTION_SENDTO,
                            Uri.parse("mailto:support@igroglobal.com"));
                    email.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
                    email.putExtra(Intent.EXTRA_TEXT, "Describe your issue here.");
                    startActivity(Intent.createChooser(email, "Send email…"));
                })

                // Dismiss
                .setNegativeButton("OK", null);

        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // (Optional) sanity-check your manifest
        //IntegrationValidator.validate(this);

        // This init() will *always* fire your callback when launched via Branch link:
        Branch.sessionBuilder(this)
                .withCallback(new Branch.BranchReferralInitListener() {
                    @Override
                    public void onInitFinished(JSONObject referringParams, BranchError error) {
                        if (error != null) {
                            Log.e("HomePage", "Branch init error: " + error.getMessage());
                            return;
                        }
                        // Grab your custom param:
                        String bookId = referringParams.optString("book_id");
                        if (!bookId.isEmpty()) {
                            Log.d("HomePage", "Deep-link to book " + bookId);
                            startActivity(
                                    new Intent(HomePageActivity.this, IndBookActivity.class)
                                            .putExtra("bookId", bookId)
                            );
                        }
                    }
                })
                .withData(getIntent().getData())
                .init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // update the Intent so getIntent().getData() returns the new link URI
        setIntent(intent);
        // (Optional) you can re-call the same init() here if you like,
        // but with branch_force_new_session in the manifest it isn’t required.
    }
}