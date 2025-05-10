package com.appleia.elect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

public class CatalogueActivity extends AppCompatActivity {
    private WebView webView;
    private FrameLayout root;
    private LinearLayout bottomToolbar;
    private eLectSQLiteHelper dbHelper;
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

        selectedTabIndex=1;
        // Root layout container
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
        title.setText("Catalogue");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMarginStart(dpToPx(8));
        header.addView(title, tlp);
    }

    private void setupWebView() {
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
                toolbarHeight + getBottomPadding()// push up above bottom toolbar
        );
        root.addView(webView, webParams);



        // ---- Load your catalogue HTML into webView ----
        dbHelper = new eLectSQLiteHelper(this);
        Map<String, List<Map<String, Object>>> data = dbHelper.fetchAllBooksGroupedByCategoryAndTopic();
        Set<String> readIds     = dbHelper.fetchReadBookIds();
        Set<String> wishListIds = dbHelper.fetchWishListBookIds();
        String html = generateHtmlFromData(data, readIds, wishListIds);
       // String html = generateHtmlFromData(data);
        Log.d("CATALOG_HTML", html);
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

    private void onToolbarItemClicked(int index) {
        switch (index) {
            case 0: startActivity(new Intent(this, HomePageActivity.class)); break;
            case 1: /* Already here */ break;
            case 2: startActivity(new Intent(this, ProgressivePlanActivity.class)); break;
            case 3: startActivity(new Intent(this, BooksReadActivity.class)); break;
            case 4: startActivity(new Intent(this, HomePageActivity.class)); break;
        }
    }

    // TODO: Generate the same HTML string as the iOS generateHTMLFromData: method
    private String generateHtmlFromData(Map<String, List<Map<String, Object>>> nestedData, Set<String> readIds,
                                        Set<String> wishListIds) {
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
                .append("a.book-item {")
                .append("  display: block;")
                .append("  margin: 4px 0 4px 20%;")
                .append("  color: #0645AD;")
                .append("  text-decoration: underline;")
                .append("}")

                // 2) now the overrides, *outside* the previous block
                .append("a.book-item.book-read {")
                .append("  color: #4CAF50 !important;")
                .append("}")

                .append("a.book-item.book-wishlist {")
                .append("  color: #0D47A1 !important;")
                .append("  font-weight: bold;")
                .append("}")
                .append("  text-decoration: underline;")


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
                    String id     = book.get("id");
                    String title  = book.get("title");
                    String author = book.get("author");
                    String caVal  = book.get("ca");
                    String cbVal  = book.get("cb");

                    // decide CSS class…
                    String cls = "book-item";
                    if (readIds.contains(id))      cls += " book-read";
                    else if (wishListIds.contains(id)) cls += " book-wishlist";

                    // 1) Title + author
                    html.append("<a class='").append(cls)
                            .append("' href='customscheme://book/").append(id).append("'>")
                            .append(title);
                    if (!author.isEmpty()) {
                        html.append(" <em>(")
                                .append(author)
                                .append(")</em>");
                    }
                    html.append("</a>");

                    // 2) ca / cb lines
                    if (!caVal.isEmpty() || !cbVal.isEmpty()) {
                        html.append("<div style='margin-left:20%;font-size:0.9em;color:#555;'>");
                        if (!caVal.isEmpty()) {
                            html.append("-").append(caVal);
                        }
                        if (!caVal.isEmpty() && !cbVal.isEmpty()) {
                            html.append(" | ");
                        }
                        if (!cbVal.isEmpty()) {
                            html.append("-").append(cbVal);
                        }
                        html.append("</div>");
                    }
                }
                    //html.append("<a class='book-item' href='customscheme://book/")
                    //        .append(book.get("id")).append("'>")
                    //        .append(book.get("title")).append("</a>");
                //}
                html.append("</div>");
            }

            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString();
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
        int colorActive   = Color.parseColor("#3359A2");  // blue
        int colorInactive = Color.parseColor("#888888");  // grey

        bottomToolbar.removeAllViews();
        //int blue = Color.parseColor("#3359A2");

        for (int i = 0; i < titles.length; i++) {
            boolean isActive = (i == selectedTabIndex);
            int iconColor = isActive ? colorInactive : colorActive;
            int textColor = iconColor;
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
            btn.setImageTintList(ColorStateList.valueOf(iconColor));
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




}

