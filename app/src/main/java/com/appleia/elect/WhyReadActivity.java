package com.appleia.elect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * WhyReadActivity: shows introduction, prayers, and toggle functionality in a WebView.
 * Header and bottom toolbar are flat (no curves) to match the rest of the app.
 */
public class WhyReadActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout root;
    private LinearLayout bottomToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        // match status bar to header color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.header_blue));
        }

        root = new FrameLayout(this);
        setContentView(root);
        setupHeader();
        setupWebView();
        setupBottomToolbar();
    }

    private void setupHeader() {
        int height = dpToPx(56);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(Color.parseColor("#3359A2"));
        FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height, Gravity.TOP);
        root.addView(header, hlp);

        ImageButton back = new ImageButton(this);
        back.setImageResource(R.drawable.baseline_chevron_left_24);
        back.setBackgroundColor(Color.TRANSPARENT);
        back.setColorFilter(Color.WHITE);
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        blp.setMarginStart(dpToPx(16));
        header.addView(back, blp);

        TextView title = new TextView(this);
        title.setText("Why Read");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMarginStart(dpToPx(8));
        header.addView(title, tlp);
    }

    private void setupWebView() {
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        int top = dpToPx(56);
        int bottom = dpToPx(56);// + getBottomPadding();
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.setMargins(0, top, 0, bottom+getBottomPadding());
        root.addView(webView, wlp);

 //       String html = buildHtml();
 //       webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        //webView.loadDataWithBaseURL(null, generateWhyReadHtml(), "text/html", "utf-8", null);
        String html = generateWhyReadHtml();
        webView.loadDataWithBaseURL(
                "file:///android_asset/",    // any fixed non‑null origin
                html,
                "text/html",
                "utf-8",
                null
        );

    }

    private void setupBottomToolbar() {
        // create the toolbar container
        bottomToolbar = new LinearLayout(this);
        bottomToolbar.setOrientation(LinearLayout.HORIZONTAL);
        bottomToolbar.setWeightSum(5f);
        bottomToolbar.setBackgroundColor(Color.WHITE);

        // ask system how tall the nav‑bar inset is
        int inset = getBottomPadding();

        // total toolbar height = 56dp for icons + nav‑bar inset
        int totalHeight = dpToPx(56) + inset;

        // make it span the full width and sit at the very bottom
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                totalHeight,
                Gravity.BOTTOM
        );
        root.addView(bottomToolbar, lp);

        // push its children up by the inset so the 56dp area is for your icons
        bottomToolbar.setPadding(0, 0, 0, inset);

        // now populate it
        setupToolbarItems();
    }

    private void setupToolbarItems() {
        String[] titles = {"Home","Catalogue","Plan","Books Read","Wishlist"};
        int[] icons    = {
                R.drawable.home,
                R.drawable.catalogue,
                R.drawable.trending,
                R.drawable.book_read,
                R.drawable.book_to_read
        };
        bottomToolbar.removeAllViews();
        int blue = Color.parseColor("#3359A2");

        for (int i = 0; i < titles.length; i++) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));

            ImageButton btn = new ImageButton(this);
            btn.setImageResource(icons[i]);
            btn.setBackgroundColor(Color.TRANSPARENT);
            // 24dp icon, 10dp top margin (centers in 56dp)
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                    dpToPx(24), dpToPx(24)
            );
            iconLp.topMargin = dpToPx(10);
            btn.setLayoutParams(iconLp);
            btn.setImageTintList(ColorStateList.valueOf(blue));
            final int idx = i;
            btn.setOnClickListener(v -> onToolbarItemClicked(idx));
            item.addView(btn);

            TextView tv = new TextView(this);
            tv.setText(titles[i]);
            tv.setTextSize(10);
            tv.setTextColor(blue);
            tv.setGravity(Gravity.CENTER);
            item.addView(tv);

            bottomToolbar.addView(item);
        }
    }


    private void onToolbarItemClicked(int index) {
        Intent intent = null;
        switch (index) {
            case 0: intent = new Intent(this, HomePageActivity.class); break;
            case 1: intent = new Intent(this, CatalogueActivity.class); break;
            case 2: intent = new Intent(this, ProgressivePlanActivity.class); break;
            case 3: intent = new Intent(this, BooksReadActivity.class); break;
            case 4: intent = new Intent(this, WishListActivity.class); break;
        }
        if (intent != null) startActivity(intent);
        finish();
    }

    private String generateWhyReadHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<style>")
                .append("body{font-family:-apple-system,sans-serif;background:#dee3ef;padding:20px;color:#333;}")
                .append("h2{color:#1a4c96;margin-top:24px;}")
                .append(".toggle-button{color:#007aff;font-weight:bold;cursor:pointer;margin:16px 0;display:block;}")
                .append("</style>")
                .append("<script>")
                .append("function toggle(){")
                .append("  var c=document.getElementById('intro');")
                .append("  if(c.style.display==='none'){c.style.display='block';localStorage.intro='true';}")
                .append("  else{c.style.display='none';localStorage.intro='false';}")
                .append("}")
                .append("window.onload=function(){")
                .append("  var cvis=localStorage.intro;")
                .append("  if(cvis==='false') document.getElementById('intro').style.display='none';")
                .append("};")
                .append("</script>")
                .append("</head><body>")
                // Intro section
                .append("<div id='intro'>")
                .append("<p>Welcome to our spiritual reading guide, thoughtfully designed to accompany you on your journey of faith and personal growth. Inspired by the rich tradition of spiritual reading in the Catholic Church, this app offers a carefully curated collection of books that nourish the soul, deepen understanding of God’s truth, and inspire a life of virtue.</p>")
                .append("<p>This list is tailored especially for lay people — busy Catholics striving to live their faith in the heart of the world. Whether you are raising a family, pursuing a career, or engaging in your community, daily spiritual reading can become a source of strength and renewal. Just a few minutes a day can illuminate your thoughts, guide your decisions, and help you bring the light of Christ to those around you through your ordinary circumstances.</p>")
                .append("<p>You’ll find everything from the great spiritual classics to the latest contemporary titles, offering wisdom that resonates across the centuries. Books are categorized into 25 themes, reflecting different aspects of the interior life. Additionally, each book is assigned five keywords, allowing it to appear in multiple relevant categories. For those who prefer a more structured path, our Progressive Reading Plan suggests titles based on your current spiritual maturity, encouraging growth at your own pace.</p>")
                .append("<p>While the app draws from Catholic wisdom, its reflections on the spiritual life can inspire readers of all backgrounds. Start exploring, and let the voices of saints, theologians, and spiritual writers guide your heart and mind — because even in the busiest lives, a few moments of spiritual reading can make all the difference.</p>")
                .append("<p>It’s easy to desire a deeper spiritual life — the challenge is making space for it. That’s why it’s helpful to set yourself a simple, concrete goal: 15 minutes a day of spiritual reading (yes, listening to an audiobook while folding laundry still counts!). This small habit can ground your good intentions in action and gradually shape your heart and mind. You may wish to begin with the traditional prayer to the Holy Spirit, asking for light and guidance, and end with a brief prayer of thanksgiving. Both are included below to accompany you in this daily moment of grace.</p>")
                .append("</div>")
                // toggle button
                .append("<div class='toggle-button' onclick='toggle()'>Hide Introduction</div>")
                // Starting Prayer
                .append("<h2>Starting Prayer</h2>")
                .append("<p>Come, Holy Spirit, fill the hearts of your faithful and kindle in them the fire of your love.</p>")
                .append("<p>Send forth your Spirit and they shall be created, and you shall renew the face of the earth.</p>")
                .append("<p><i>Let us pray:</i></p>")
                .append("<p>O God, who have taught the hearts of the faithful by the light of the Holy Spirit, grant that in the same Spirit we may be truly wise and ever rejoice in his consolation. Through Christ our Lord. Amen.</p>")
                // Finishing Prayer
                .append("<h2>Finishing Prayer</h2>")
                .append("<p>Thank You, Lord, for the light and grace received through this reading. Let it take root in my soul and bear fruit in my actions. May I carry Your word into the day ahead and share Your love with all those I meet. Amen.</p>")
                .append("</body></html>");
        return sb.toString();
    }


    private String buildHtml() {
        return "<html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body{font-family:-apple-system,sans-serif;background:#dee3ef;padding:20px;color:#333;}" +
                "h2{color:#1a4c96;margin-top:24px;}" +
                ".toggle-button{color:#007aff;font-weight:bold;cursor:pointer;margin:16px 0;}" +
                "</style>" +
                "<script>" +
                "function toggle(){var c=document.getElementById('intro');" +
                "if(c.style.display==='none'){c.style.display='block';" +
                "localStorage.intro='true';}else{c.style.display='none';localStorage.intro='false';}}" +
                "window.onload=function(){var cvis=localStorage.intro; if(cvis==='false') document.getElementById('intro').style.display='none';};" +
                "</script></head><body>" +
                "<div id='intro'>" +
                "<p>Welcome to our spiritual reading guide... [FULL text here with all paragraphs as in iOS]</p>" +
                "<p>This list is tailored especially for lay people...</p>" +
                "<p>You’ll find everything from the great spiritual classics...</p>" +
                "<p>While the app draws from Catholic wisdom...</p>" +
                "<p>It’s easy to desire a deeper spiritual life...</p>" +
                "</div>" +
                "<div class='toggle-button' onclick='toggle()'>Toggle Introduction</div>" +
                "<h2>Starting Prayer</h2>" +
                "<p>Come, Holy Spirit, fill the hearts...</p>" +
                "<h2>Finishing Prayer</h2>" +
                "<p>Thank You, Lord, for the light...</p>" +
                "</body></html>";
    }

    private int dpToPx(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    private int getBottomPadding() {
        int resId = getResources().getIdentifier("navigation_bar_height","dimen","android");
        return resId>0 ? getResources().getDimensionPixelSize(resId) : 0;
    }
}
