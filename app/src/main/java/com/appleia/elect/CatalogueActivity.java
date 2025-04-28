package com.appleia.elect;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.appleia.elect.db.eLectSQLiteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import android.graphics.Typeface;

public class CatalogueActivity extends AppCompatActivity {
    private WebView webView;
    private View headerView;
    private LinearLayout bottomToolbar;
    private eLectSQLiteHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw and color the status bar to match header
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.header_blue));
        }

        // Root layout container
       FrameLayout root = new FrameLayout(this);
        setContentView(root);

        // ---- Header with Back Button + Title ----
        int headerHeight = dpToPx(100);

        // a) horizontal container
        LinearLayout headerContainer = new LinearLayout(this);
        headerContainer.setOrientation(LinearLayout.HORIZONTAL);
        headerContainer.setBaselineAligned(false);
        headerContainer.setGravity(Gravity.CENTER_VERTICAL);
        headerContainer.setBackgroundColor(Color.parseColor("#3359A2"));
        FrameLayout.LayoutParams headerLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                headerHeight,
                Gravity.TOP
        );
        root.addView(headerContainer, headerLp);

        // b) back-chevron
        ImageButton backButton = new ImageButton(this);
        backButton.setImageResource(R.drawable.baseline_chevron_left_24);
        backButton.setBackgroundColor(Color.TRANSPARENT);
        backButton.setColorFilter(Color.WHITE);
        backButton.setOnClickListener(v -> onBackPressed());
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(
                dpToPx(24), dpToPx(24)
        );
        backLp.setMarginStart(dpToPx(16));
        headerContainer.addView(backButton, backLp);

        // c) title
        TextView titleView = new TextView(this);
        titleView.setText("Catalogue");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(20);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleLp.setMarginStart(dpToPx(8));
        headerContainer.addView(titleView, titleLp);
        // ---- WebView ----
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        int toolbarHeight = dpToPx(50 + getBottomPadding());
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        webParams.setMargins(0, headerHeight, 0, toolbarHeight);
        root.addView(webView, webParams);

        bottomToolbar = new LinearLayout(this);
        bottomToolbar.setOrientation(LinearLayout.HORIZONTAL);
        bottomToolbar.setWeightSum(5f);
        bottomToolbar.setBackgroundColor(Color.WHITE);

// 2) Compute its exact height (50 dp + nav-bar inset)
        toolbarHeight = dpToPx(50) + getBottomPadding();

// 3) Build LayoutParams that pin it to the bottom at that height
        FrameLayout.LayoutParams toolbarParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                toolbarHeight,            // <— explicit height
                Gravity.BOTTOM
        );

// 4) Add it **once** with those params
        root.addView(bottomToolbar, toolbarParams);

// 5) Populate your five icons + labels
        setupToolbarItems();



        // ---- Load Catalogue Data ----
        // Initialize your SQLite helper and fetch nested data
        dbHelper = new eLectSQLiteHelper(this);
        Map<String, List<Map<String, Object>>> data = dbHelper.fetchAllBooksGroupedByCategoryAndTopic();
        String html = generateHtmlFromData(data);
        webView.loadDataWithBaseURL(
                "file:///android_asset/",
                html,
                "text/html",
                "utf-8",
                null
        );
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Calculate bottom inset for devices with navigation gestures
    private int getBottomPadding() {
        int resId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resId > 0 ? getResources().getDimensionPixelSize(resId) : 0;
    }

    // TODO: Populate bottomToolbar with icon+label items matching the iOS layout
    private void setupToolbarItems() {
        String[] titles = {"Home","Catalogue","Plan","Books Read","Wishlist"};
        int[] icons    = {R.drawable.home, R.drawable.books_white,R.drawable.books_white,R.drawable.books_white,R.drawable.books_white};

        bottomToolbar.removeAllViews();
        for (int i = 0; i < titles.length; i++) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            item.setLayoutParams(lp);

            ImageButton btn = new ImageButton(this);
            btn.setImageResource(icons[i]);
            btn.setBackgroundColor(Color.TRANSPARENT);
            int iconSize = dpToPx(24);
            btn.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            final int idx = i;
            btn.setOnClickListener(v -> onToolbarItemClicked(idx));
            item.addView(btn);

            TextView tv = new TextView(this);
            tv.setText(titles[i]);
            tv.setTextSize(10);
            tv.setTextColor(Color.RED);
            tv.setGravity(Gravity.CENTER);
            item.addView(tv);

            bottomToolbar.addView(item);
        }
    }

    private void onToolbarItemClicked(int index) {
        switch (index) {
            case 0: startActivity(new Intent(this, HomePageActivity.class)); break;
            case 1: /* Already here */ break;
            case 2: startActivity(new Intent(this, HomePageActivity.class)); break;
            case 3: startActivity(new Intent(this, HomePageActivity.class)); break;
            case 4: startActivity(new Intent(this, HomePageActivity.class)); break;
        }
    }

    // TODO: Generate the same HTML string as the iOS generateHTMLFromData: method
    private String generateHtmlFromData(Map<String, List<Map<String, Object>>> nestedData) {
        StringBuilder html = new StringBuilder();

        // HEAD + CSS
        html.append("<html><head>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<style>")
                // Base page
                .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;")
                .append("margin:0;padding:0;background:#dee3ef;}")

                // Search bar
                .append("#searchBar{width:90%;margin:16px auto;display:block;")
                .append("padding:10px;font-size:16px;border:none;border-radius:12px;")
                .append("background:#f0f0f0;box-shadow:inset 0 0 2px rgba(0,0,0,0.1);}")

                // Category toggle (full width)
                .append(".category-toggle{display:block;width:90%;margin:8px auto;")
                .append("padding:12px;background:#f0f0f0;border:none;border-radius:8px;")
                .append("font-size:17px;font-weight:bold;color:#1a4c96;text-align:left;}")
                .append(".category-toggle:hover{background:#e0e0e0;cursor:pointer;}")

                // Category content container
                .append(".category-content{display:none;margin:0 5% 16px 5%;}")

                // ★ Topic toggle (stacked, full row under its category)
                .append(".topic-toggle{display:block;width:80%;margin:4px auto;")
                .append("padding:10px;background:#ffffff;border:none;border-radius:6px;")
                .append("font-size:15px;color:#1a4c96;text-align:left;}")
                .append(".topic-toggle:hover{background:#f0f0f7;cursor:pointer;}")

                // Topic content container
                .append(".topic-content{display:none;margin:0 10% 8px 10%;padding-left:16px;}")

                // Book link
                .append("a.book-item{display:block;margin:4px 0 4px 20%;")
                .append("color:#0645AD;text-decoration:underline;}")

                .append("</style>");

        // JS for toggle & search (unchanged)
        html.append("<script>")
                .append("function toggle(el){let nxt=el.nextElementSibling;")
                .append("nxt.style.display=(nxt.style.display==='block'?'none':'block');}")
                .append("function searchBooks(){let f=document.getElementById('searchBar').value.toLowerCase();")
                .append("document.querySelectorAll('.category-toggle').forEach(cat=>{")
                .append(" let cont=cat.nextElementSibling, catMatch=cat.textContent.toLowerCase().includes(f), catShow=false;")
                .append(" cont.querySelectorAll('.topic-toggle').forEach(tp=>{")
                .append("   let tcont=tp.nextElementSibling, tpMatch=tp.textContent.toLowerCase().includes(f), tpShow=false;")
                .append("   tcont.querySelectorAll('.book-item').forEach(bk=>{")
                .append("     let txt=bk.textContent.toLowerCase(), ok=txt.includes(f)||tpMatch||catMatch;")
                .append("     bk.style.display=ok?'block':'none'; if(ok) tpShow=true;")
                .append("   });")
                .append("   tp.style.display=tpShow||tpMatch||catMatch?'block':'none';")
                .append("   tcont.style.display=tpShow||tpMatch||catMatch?'block':'none';")
                .append("   if(tp.style.display==='block') catShow=true;")
                .append(" });")
                .append(" cat.style.display=catShow||catMatch?'block':'none';")
                .append(" cont.style.display=catShow||catMatch?'block':'none';")
                .append("});}")
                .append("</script>")
                .append("</head><body>")
                .append("<input id='searchBar' type='text' placeholder='Search books…' onkeyup='searchBooks()'>");

        // Sort categories by numeric ID
        List<String> categories = new ArrayList<>(nestedData.keySet());
        Collections.sort(categories, Comparator.comparing(k -> {
            String raw = k.split(" - ", 2)[0].replaceAll("^\\D+", "");
            return Integer.parseInt(raw);
        }));

        // Build accordion
        for (String key : categories) {
            String[] parts   = key.split(" - ", 2);
            String rawCatId  = parts[0], catName = parts[1];
            String displayId = rawCatId.replaceAll("^\\D+", "");

            html.append("<button class='category-toggle' onclick='toggle(this)'>")
                    .append(displayId).append(" – ").append(catName)
                    .append("</button>")
                    .append("<div class='category-content'>");

            // 1) filter out topics with no books
            List<Map<String,Object>> topics = nestedData.get(key);
            List<Map<String,Object>> nonEmpty = new ArrayList<>();
            for (Map<String,Object> t : topics) {
                @SuppressWarnings("unchecked")
                List<Map<String,String>> books = (List<Map<String,String>>)t.get("books");
                if (books != null && !books.isEmpty()) nonEmpty.add(t);
            }
            // 2) sort topics alphabetically by name
            Collections.sort(nonEmpty, Comparator.comparing(
                    t -> ((String)t.get("name")).toLowerCase()
            ));

            // 3) render
            for (Map<String,Object> topic : nonEmpty) {
                String tName = (String)topic.get("name");
                html.append("<button class='topic-toggle' onclick='toggle(this)'>")
                        .append(tName).append("</button>")
                        .append("<div class='topic-content'>");

                @SuppressWarnings("unchecked")
                List<Map<String,String>> books = (List<Map<String,String>>)topic.get("books");
                for (Map<String,String> book : books) {
                    html.append("<a class='book-item' href='customscheme://book/")
                            .append(book.get("id")).append("'>")
                            .append(book.get("title")).append("</a>");
                }
                html.append("</div>");
            }

            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString();
    }


}

