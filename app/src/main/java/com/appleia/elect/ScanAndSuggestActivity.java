package com.appleia.elect;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import android.content.pm.ActivityInfo;


public class ScanAndSuggestActivity extends AppCompatActivity {
    private Button scanButton;
    private Button suggestButton;
    private ProgressBar progressBar;
    private TextView resultView;
    private String mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // read mode extra passed from dialog: "scan" or "manual"
        mode = getIntent().getStringExtra("mode");

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.background));
        setContentView(root);

        scanButton = new Button(this);
        scanButton.setText("Scan ISBN & Email Result");
        root.addView(scanButton,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        resultView = new TextView(this);
        resultView.setPadding(0, dpToPx(16), 0, dpToPx(16));
        root.addView(resultView,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        suggestButton = new Button(this);
        suggestButton.setText("Send Suggestion by Email");
        root.addView(suggestButton,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        // wire actions
        scanButton.setOnClickListener(v -> startScan());
        suggestButton.setOnClickListener(v -> sendSuggestion());

        // if mode specified, trigger immediately and hide buttons
        if ("scan".equals(mode)) {
            scanButton.setVisibility(View.GONE);
            suggestButton.setVisibility(View.GONE);
            startScan();
        } else if ("manual".equals(mode)) {
            scanButton.setVisibility(View.GONE);
            // directly open email suggestion form
            sendSuggestion();
        }
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan ISBN barcode");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String isbn = result.getContents();
            if (isbn != null) {
                fetchBookInfo(isbn);
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fetchBookInfo(String isbn) {
        progressBar.setVisibility(View.VISIBLE);
        resultView.setText("");
        new GoogleBooksTask().execute(isbn);
    }

    private class GoogleBooksTask extends AsyncTask<String, Void, String> {
        private String isbn;

        @Override
        protected String doInBackground(String... args) {
            isbn = args[0];
            try {
                String query = URLEncoder.encode("isbn:" + isbn, "UTF-8");
                URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                StringBuilder sb = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1) sb.append((char) c);
                return sb.toString();
            } catch (IOException e) {
                Log.e("GoogleBooksTask", "Error fetching", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            progressBar.setVisibility(View.GONE);
            if (json == null) {
                Toast.makeText(ScanAndSuggestActivity.this, "Error fetching book info", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject root = new JSONObject(json);
                JSONArray items = root.optJSONArray("items");
                if (items != null && items.length() > 0) {
                    JSONObject info = items.getJSONObject(0).getJSONObject("volumeInfo");
                    String title = info.optString("title", "");
                    String authors = info.has("authors") ? info.getJSONArray("authors").join(", ") : "";
                    String desc = info.optString("description", "");
                    String msg = "ISBN: " + isbn + "\nTitle: " + title + "\nAuthors: " + authors + "\n\n" + desc;
                    resultView.setText(msg);
                    emailText("Book Info: " + title, msg);
                } else {
                    resultView.setText("No book found for ISBN: " + isbn);
                }
            } catch (JSONException e) {
                Log.e("GoogleBooksTask", "JSON error", e);
                Toast.makeText(ScanAndSuggestActivity.this, "Parse error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void emailText(String subject, String body) {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto: support@igroglobal.com"));
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(email, "Send email via:"));
    }

    private void sendSuggestion() {
        String subject = "Book Suggestion";
        String body = "I would like to suggest the following book:\n"
                + "Title: \n"
                + "Author: \n"
                + "Comments: \n";

        // call emailText with default recipient
        emailTextWithRecipient("default@you.com", subject, body);
    }

    private void emailTextWithRecipient(String to, String subject, String body) {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto: support@igroglobal.com"));
        email.putExtra(Intent.EXTRA_EMAIL,   new String[]{ to });      // ← default “to”
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT,    body);
        startActivity(Intent.createChooser(email, "Send email via:"));
    }

    private int dpToPx(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }
}
