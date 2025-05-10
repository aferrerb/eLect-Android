package com.appleia.elect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
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
import java.util.Set;

import android.graphics.Typeface;

public class ProgressivePlanActivity extends AppCompatActivity {
    private WebView webView;
    private View headerView;
    private LinearLayout bottomToolbar;
    private eLectSQLiteHelper dbHelper;
    private FrameLayout root;
    private int selectedTabIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        // Draw and color the status bar to match header
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.header_blue));
        }
        selectedTabIndex=2;
        // Root layout container
        root = new FrameLayout(this);
        setContentView(root);
        setupHeader();
        setupWebView();
        setupBottomToolbar();
    }

    private void setupHeader () {
            // ---- Header with Back Button + Title ----
            int headerHeight = dpToPx(56);

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
            titleView.setText("Progressive Plan");
            titleView.setTextColor(Color.WHITE);
            titleView.setTextSize(20);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            titleLp.setMarginStart(dpToPx(8));
            headerContainer.addView(titleView, titleLp);
    }
        private void setupWebView () {
            int headerHeight = dpToPx(56);

            int toolbarHeight = dpToPx(56);         // your desired 40dp
            //+ getBottomPadding();  // plus nav-bar inset if you still want it
            // ---- WebView (fills between header & toolbar) ----
            webView = new WebView(this);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            //webView.setWebViewClient(new WebViewClient());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    Uri uri = request.getUrl();
                    if ("customscheme".equals(uri.getScheme())) {
                        String host = uri.getHost();
                        if ("back".equals(host)) {
                            finish();               // mimic your iOS “back”
                            return true;
                        }
                        if ("book".equals(host)) {
                            // last path segment is the book ID
                            String bookId = uri.getLastPathSegment();
                            System.out.println("this is the bookid " + bookId);
                            if (bookId != null) {
                                Intent i = new Intent(view.getContext(), IndBookActivity.class);
                                i.putExtra("bookId", bookId);
                                view.getContext().startActivity(i);
                            }
                            return true;
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, request);
                }
            });

            FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            webParams.setMargins(
                    0,
                    headerHeight,   // push down below header
                    0,
                    toolbarHeight + getBottomPadding()  // push up above bottom toolbar
            );
            root.addView(webView, webParams);

            // ---- Bottom toolbar ----
            bottomToolbar = new LinearLayout(this);
            bottomToolbar.setOrientation(LinearLayout.HORIZONTAL);
            bottomToolbar.setWeightSum(5f);
            bottomToolbar.setBackgroundColor(Color.WHITE);

            FrameLayout.LayoutParams toolbarParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    toolbarHeight,    // the *same* 40dp+inset we used above
                    Gravity.BOTTOM
            );
            root.addView(bottomToolbar, toolbarParams);


            // ---- Load your catalogue HTML into webView ----
            dbHelper = new eLectSQLiteHelper(this);
            Map<String, Map<String, List<Map<String, String>>>> data =
                    dbHelper.fetchBooksGroupedByYearAndCategory();
            Set<String> readIds     = dbHelper.fetchReadBookIds();
            Set<String> wishListIds = dbHelper.fetchWishListBookIds();

            //String html = generatePlanHtml(data);
            String html = generatePlanHtml(data, readIds, wishListIds);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
            //webView.loadDataWithBaseURL(
            //        "file:///android_asset/",
            //        html, "text/html", "utf-8", null
            //);
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

    // TODO: Populate bottomToolbar with icon+label items matching the iOS layout
    private void setupToolbarItems() {
        String[] titles = {"Home","Catalogue","Plan","Books Read","Wishlist"};
        int[] icons    = {R.drawable.home, R.drawable.catalogue,R.drawable.trending,R.drawable.book_read,R.drawable.book_to_read};

        int colorActive   = Color.parseColor("#3359A2");  // blue
        int colorInactive = Color.parseColor("#888888");  // grey
        bottomToolbar.removeAllViews();

        for (int i = 0; i < titles.length; i++) {
            boolean isActive = (i == selectedTabIndex);
            int iconColor = isActive ? colorInactive : colorActive;
            int textColor = iconColor;
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
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
// give the icon itself some extra top margin
            iconLp.topMargin = dpToPx(10);
            btn.setLayoutParams(iconLp);

            btn.setImageTintList(ColorStateList.valueOf(iconColor));
            // btn.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            final int idx = i;
            btn.setOnClickListener(v -> onToolbarItemClicked(idx));
            item.addView(btn);

            TextView tv = new TextView(this);
            tv.setText(titles[i]);
            tv.setTextSize(10);
            tv.setTextColor(textColor);
            tv.setGravity(Gravity.CENTER);
            item.addView(tv);

            bottomToolbar.addView(item);
        }
    }

    private void onToolbarItemClicked(int index) {
        switch (index) {
            case 0: startActivity(new Intent(this, HomePageActivity.class)); break;
            case 1: startActivity(new Intent(this, CatalogueActivity.class)); break;
            case 2: /* Already here */ break;
            case 3: startActivity(new Intent(this, BooksReadActivity.class)); break;
            case 4: startActivity(new Intent(this, WishListActivity.class)); break;
        }
    }

    // TODO: Generate the same HTML string as the iOS generateHTMLFromData: method
    private String generatePlanHtml(Map<String, Map<String, List<Map<String, String>>>> nestedData, Set<String> readIds,
                                    Set<String> wishListIds) {
        StringBuilder html = new StringBuilder();

        // --- HEAD with CSS & JS ---
        html.append("<!doctype html><html><head>")
                // viewport
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                // CSS
                .append("<style>")
                // base
                .append("body{margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#dee3ef;}")

                // search bar
                .append("#searchBar{width:90%;margin:16px auto;display:block;padding:10px;font-size:16px;border:none;border-radius:12px;background:#f0f0f0;box-shadow:inset 0 0 2px rgba(0,0,0,0.1);}")

                // Year toggle
                .append(".year-toggle{display:block;width:90%;margin:8px auto;padding:14px;background:#f0f0f0;border:none;border-radius:8px;font-size:18px;font-weight:bold;color:#1a4c96;text-align:left;}")
                .append(".year-toggle:hover{background:#e0e0e0;cursor:pointer;}")

                // Year content (hidden by default)
                .append(".year-content{display:none;margin:0 5% 16px;}")

                // PPCAT toggle
                .append(".ppcat-toggle{display:block;width:80%;margin:6px auto;padding:12px;background:#ffffff;border:none;border-radius:6px;font-size:16px;color:#1a4c96;text-align:left;}")
                .append(".ppcat-toggle:hover{background:#f0f0f7;cursor:pointer;}")

                // PPCAT content (hidden by default)
                .append(".ppcat-content{display:none;margin:0 10% 8px;padding-left:12px;}")

                // Book link
                //.append("a.book-item{display:block;margin:4px 0 4px 20%;color:#0645AD;text-decoration:underline;}")
                .append("a.book-item {")
                .append("display: block;")
                .append("margin: 4px 0 4px 20%;")
                .append("color: #0645AD;")

                .append("}")
// 2) overrides for read & wishlist
                .append("a.book-item.book-read {")
                .append("color: #4CAF50 !important;")
                .append("}")
                .append("a.book-item.book-wishlist {")
                .append("color: #0D47A1 !important;")
                .append("font-weight: bold;")
                .append("}")
                .append("text-decoration: underline;")
                .append("</style>")

                // JS toggle + search
                .append("<script>")
                // toggle show/hide
                .append("function toggle(el){")
                .append("let nxt=el.nextElementSibling;")
                .append("nxt.style.display = (nxt.style.display==='block'?'none':'block');")
                .append("}")
                // search across all levels
                .append("function searchBooks(){")
                .append("let f=document.getElementById('searchBar').value.toLowerCase();")
                .append("document.querySelectorAll('.year-toggle').forEach(year=>{")
                .append("let yCont=year.nextElementSibling, yMatch = year.textContent.toLowerCase().includes(f), showYear=false;")
                .append("yCont.querySelectorAll('.ppcat-toggle').forEach(cat=>{")
                .append("let cCont=cat.nextElementSibling, cMatch=cat.textContent.toLowerCase().includes(f), showCat=false;")
                .append("cCont.querySelectorAll('.book-item').forEach(b=>{")
                .append("let ok=b.textContent.toLowerCase().includes(f)||cMatch||yMatch;")
                .append("b.style.display=ok?'block':'none';")
                .append("if(ok) showCat=true;")
                .append("});")
                .append("cat.style.display    = (showCat||cMatch) ? 'block':'none';")
                .append("cCont.style.display = (showCat||cMatch) ? 'block':'none';")
                .append("if(cat.style.display==='block') showYear=true;")
                .append("});")
                .append("year.style.display  = (showYear||yMatch) ? 'block':'none';")
                .append("yCont.style.display = (showYear||yMatch) ? 'block':'none';")
                .append("});")
                .append("}")
                .append("</script>")
                .append("</head>");

        // --- BODY with search input ---
        html.append("<body>")
                .append("<input id='searchBar' type='text' placeholder='Search books…' onkeyup='searchBooks()'>");

        // --- Sort years by numeric prefix ---
        List<String> years = new ArrayList<>(nestedData.keySet());
        Collections.sort(years, Comparator.comparing(k -> Integer.parseInt(k.split(" - ")[0])));

                // --- Build the accordion ---
        for (String yearKey : years) {
            html.append("<button class='year-toggle' onclick='toggle(this)'>")
                    .append(yearKey)
                    .append("</button>")
                    .append("<div class='year-content'>");

            // sort PPCAT keys
            Map<String, List<Map<String,String>>> cats = nestedData.get(yearKey);
            List<String> catKeys = new ArrayList<>(cats.keySet());
            Collections.sort(catKeys, Comparator.comparing(k -> Integer.parseInt(k.split(" - ")[0])));

            for (String catKey : catKeys) {
                html.append("<button class='ppcat-toggle' onclick='toggle(this)'>")
                        .append(catKey)
                        .append("</button>")
                        .append("<div class='ppcat-content'>");

                for (Map<String,String> book : cats.get(catKey)) {
                    String id    = book.get("id");
                    String title = book.get("title");

// pick the right CSS class
                    String cls = "book-item";
                    if (readIds.contains(id)) {
                        cls += " book-read";
                    } else if (wishListIds.contains(id)) {
                        cls += " book-wishlist";
                    }

                    html.append("<a class='").append(cls)
                            .append("' href='customscheme://book/").append(id).append("'>")
                            .append(title)
                            .append("</a>");
                    //html.append("<a class='book-item' href='customscheme://book/")
                    //        .append(book.get("id"))
                    //        .append("'>")
                    //        .append(book.get("title"))
                    //        .append("</a>");
                }

                html.append("</div>");  // .ppcat-content
            }

            html.append("</div>");  // .year-content
        }

        html.append("</body></html>");
        return html.toString();
    }

}


