package com.appleia.elect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.appleia.elect.db.eLectSQLiteHelper;
import com.appleia.elect.model.BookItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BooksViewActivity extends AppCompatActivity {
    private LinearLayout header;
    private SearchView searchView;
    private WebView webView;
    private LinearLayout bottomToolbar;

    private eLectSQLiteHelper dbHelper;
    private List<BookItem> allBooks;
    private List<BookItem> filteredBooks;
    private String headerTitle;
    private FrameLayout root;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.header_blue));
        }

        root = new FrameLayout(this);
        //root.setBackgroundColor(Color.parseColor("#DEE3EF"));
        setContentView(root);

        // read extras
        headerTitle = getIntent().getStringExtra("headerTitle");
        String authorId = getIntent().getStringExtra("authorID");
        String topicId  = getIntent().getStringExtra("topicID");
        Log.d("BooksViewActivity", "authorID=" + authorId + " topicID=" + topicId);
        setupHeader();
        setupSearchView();
        setupWebView();
        setupBottomToolbar();

        dbHelper = new eLectSQLiteHelper(this);
        if (authorId != null) {
            allBooks = dbHelper.fetchBooksForAuthorID(authorId);
        } else if (topicId != null) {
            allBooks = dbHelper.fetchBooksForTopicID(topicId);
        } else {
            allBooks = new ArrayList<>();
        }
        filteredBooks = new ArrayList<>(allBooks);

        loadBooksHtml();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String text) {
                filter(text);
                return true;
            }
        });
    }

    private void setupHeader() {
        int h = dpToPx(56);
        header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(Color.parseColor("#3359A2"));
        FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.TOP);
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
        title.setText(headerTitle != null ? headerTitle : "Books");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMarginStart(dpToPx(8));
        header.addView(title, tlp);
    }

    private void setupSearchView() {
        searchView = new SearchView(this);
        FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(56));
        slp.setMargins(dpToPx(8), dpToPx(56), dpToPx(8), 0);
        root.addView(searchView, slp);
    }

    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri uri = req.getUrl();
                if ("customscheme".equals(uri.getScheme())) {
                    if ("book".equals(uri.getHost())) {
                        String id = uri.getLastPathSegment();
                        startActivity(new Intent(BooksViewActivity.this, IndBookActivity.class)
                                .putExtra("bookId", id));
                        return true;
                    }
                }
                return false;
            }
        });
        int top = dpToPx(56*2);
        int bottom = dpToPx(56) + getBottomPadding();
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.setMargins(0, top, 0, bottom);
        root.addView(webView, wlp);
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
            case 3: /*  intent = new Intent(this, BooksReadActivity.class); break;*/
            case 4: intent = new Intent(this, WishListActivity.class); break;
        }
        if (intent != null) startActivity(intent);
        finish();
    }

    private void loadBooksHtml() {
        //StringBuilder sb = new StringBuilder();
        // grab the read & wishlist ID‐sets
        Set<String> readIds     = dbHelper.fetchReadBookIds();
        Set<String> wishListIds = dbHelper.fetchWishListBookIds();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>")
                          // base styles
                          .append("body{font-family:-apple-system,sans-serif;margin:0;padding:16px;background:#dee3ef;color:black;}")
                          .append("h2{margin-top:24px;color:#1a4c96;font-size:18px;}")
                          .append("ul{list-style:none;padding-left:0;margin-top:8px;}")
                          .append("li{margin-bottom:8px;font-size:16px;}")
                          // generic link rule, fully closed
                          .append("a.book-item{")
                            .append("text-decoration:none;")
                            .append("color:#0645AD;")
                          .append("}")
                          // overrides for read & wishlist
                          .append("a.book-item.book-read{")
                            .append("color:#4CAF50!important;")
                          .append("}")
                          .append("a.book-item.book-wishlist{")
                            .append("color:#0D47A1!important;")
                            .append("font-weight:bold;")
                          .append("}")
                          .append("</style></head><body>");

        for (BookItem b : filteredBooks) {
            String id  = b.getId();
            String cls = "book-item";
            if (readIds.contains(id)) {
                cls += " book-read";
            } else if (wishListIds.contains(id)) {
                cls += " book-wishlist";
            }
            sb.append("<li><a class='").append(cls)
                    .append("' href='customscheme://book/").append(id).append("'>")
                    .append("📖 ").append(b.getTitle())
                    .append("</a></li>");
        }
        sb.append("</ul></body></html>");
        webView.loadDataWithBaseURL(null, sb.toString(), "text/html", "utf-8", null);
    }

    private void filter(String text) {
        filteredBooks.clear();
        if (text.isEmpty()) {
            filteredBooks.addAll(allBooks);
        } else {
            String lower = text.toLowerCase();
            for (BookItem b : allBooks) {
                if (b.getTitle().toLowerCase().contains(lower)) {
                    filteredBooks.add(b);
                }
            }
        }
        loadBooksHtml();
    }

    private int dpToPx(int dp) { return Math.round(getResources().getDisplayMetrics().density * dp); }
    private int getBottomPadding() {
        int resId = getResources().getIdentifier("navigation_bar_height","dimen","android");
        return resId>0?getResources().getDimensionPixelSize(resId):0;
    }
}
