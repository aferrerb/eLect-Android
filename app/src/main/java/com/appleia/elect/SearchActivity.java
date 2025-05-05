package com.appleia.elect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.appleia.elect.db.eLectSQLiteHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private LinearLayout header;
    private SearchView searchView;
    private WebView webView;
    private LinearLayout bottomToolbar;

    private eLectSQLiteHelper dbHelper;
    private List<String> allBooks, allAuthors, allTopics;
    private List<String> recentSearches;
    private SharedPreferences prefs;
    private static final String PREF_RECENT = "recentSearches";
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

        setupHeader();
        setupSearchView();
        setupWebView();
        setupBottomToolbar();

        dbHelper = new eLectSQLiteHelper(this);
        allBooks = dbHelper.fetchAllBookTitles();
        allAuthors = dbHelper.fetchAllAuthors();
        allTopics = dbHelper.fetchAllTopics();

        prefs = getSharedPreferences("eLectPrefs", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(PREF_RECENT, new HashSet<>());
        recentSearches = new ArrayList<>(set);

        updateWebView("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                saveRecent(query);
                updateWebView(query);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                updateWebView(newText);
                return true;
            }
        });
    }

    private void setupHeader() {
        int height = dpToPx(56);
        header = new LinearLayout(this);
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
        title.setText("Search");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
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
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(128, 255, 255, 255));  // semi‑transparent white
        bg.setCornerRadius(dpToPx(12));
        searchView.setBackground(bg);
        searchView.setPadding(dpToPx(12), 0, dpToPx(12), 0);
    }

    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri uri = req.getUrl();
                if ("customscheme".equals(uri.getScheme())) {
                    String host = uri.getHost();
                    String id = uri.getLastPathSegment();
                    switch (host) {
                        case "book":
                            String encoded = uri.getLastPathSegment();
                            String title   = Uri.decode(encoded);
                            // look up its numeric ID
                            String ti      = dbHelper.fetchBookIDForTitle(title);
                            if (ti != null) {
                                startActivity(new Intent(SearchActivity.this, IndBookActivity.class)
                                        .putExtra("bookId", ti));
                            } else {
                                Toast.makeText(SearchActivity.this,
                                        "Book Not Found",
                                        Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        case "author":
                            String encodedAuthor = uri.getLastPathSegment();
                            String authorName    = Uri.decode(encodedAuthor);
                            // ← lookup the real ID:
                            String authorId      = dbHelper.fetchAuthorIDForName(authorName);
                            Intent ia = new Intent(SearchActivity.this, BooksViewActivity.class);
                            ia.putExtra("authorID",   authorId);
                            ia.putExtra("headerTitle","Books by " + authorName);
                            startActivity(ia);
                            return true;
                        case "topic":
                            String encodedTopic = uri.getLastPathSegment();
                            String topicName    = Uri.decode(encodedTopic);
                            String topicId      = dbHelper.fetchTopicIDForTopicName(topicName);
                            Intent it = new Intent(SearchActivity.this, BooksViewActivity.class);
                            it.putExtra("topicID",    topicId);
                            it.putExtra("headerTitle","Books on " + topicName);
                            startActivity(it);
                            return true;
                        case "recent":
                            String term = Uri.decode(id);
                            searchView.setQuery(term, true);
                            return true;
                        case "clear":
                            clearRecents();
                            updateWebView("");
                            return true;
                    }
                }
                return false;
            }
        });
        int top = dpToPx(56 * 2);
        int bottom = dpToPx(56) + getBottomPadding();
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
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
    private void updateWebView(String text) {
        if (text.isEmpty()) {
            webView.loadDataWithBaseURL(null, generateRecentsHtml(), "text/html", "utf-8", null);
        } else {
            String lower = text.toLowerCase();
            List<String> b = new ArrayList<>(), a = new ArrayList<>(), t = new ArrayList<>();
            for (String s : allBooks) if (s.toLowerCase().contains(lower)) b.add(s);
            for (String s : allAuthors) if (s.toLowerCase().contains(lower)) a.add(s);
            for (String s : allTopics) if (s.toLowerCase().contains(lower)) t.add(s);
            webView.loadDataWithBaseURL(null, generateSearchHtml(b, a, t), "text/html", "utf-8", null);
        }
    }

    private String generateSearchHtml(List<String> books, List<String> authors, List<String> topics) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>")
                .append("body{font-family:-apple-system,sans-serif;margin:0;padding:16px;background:#dee3ef;color:black;}")
                .append("h2{margin-top:24px;color:#1a4c96;font-size:18px;}")
                .append("ul{list-style:none;padding:0;margin-top:8px;}")
                .append("li{margin-bottom:8px;font-size:16px;}")
                .append("a{color:#0645AD;text-decoration:none;}")
                .append("</style></head><body>");
        if (!books.isEmpty()) {
            sb.append("<h2>Books</h2><ul>");
            for (String s : books) sb.append("<li><a href='customscheme://book/")
                    .append(Uri.encode(s)).append("'>📖 ").append(s).append("</a></li>");
            sb.append("</ul>");
        }
        if (!authors.isEmpty()) {
            sb.append("<h2>Authors</h2><ul>");
            for (String s : authors) sb.append("<li><a href='customscheme://author/")
                    .append(Uri.encode(s)).append("'>👤 ").append(s).append("</a></li>");
            sb.append("</ul>");
        }
        if (!topics.isEmpty()) {
            sb.append("<h2>Topics</h2><ul>");
            for (String s : topics) sb.append("<li><a href='customscheme://topic/")
                    .append(Uri.encode(s)).append("'>📚 ").append(s).append("</a></li>");
            sb.append("</ul>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String generateRecentsHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>")
                .append("body{font-family:-apple-system,sans-serif;margin:0;padding:16px;background:#dee3ef;color:black;}")
                .append("button{background:#3359A2;color:white;padding:10px;border:none;border-radius:8px;font-size:16px;display:block;margin:0 auto 16px;}")
                .append("h2{color:#1a4c96;font-size:18px;}")
                .append("ul{list-style:none;padding:0;}")
                .append("li{margin-bottom:8px;font-size:16px;}")
                .append("a{color:#0645AD;text-decoration:none;}")
                .append("</style></head><body>")
                .append("<button onclick=\"window.location.href='customscheme://clear'\">Clear Recent Searches</button>")
                .append("<h2>Recent Searches</h2><ul>");
        for (String s : recentSearches) sb.append("<li><a href='customscheme://recent/")
                .append(Uri.encode(s)).append("'>🔍 ").append(s).append("</a></li>");
        sb.append("</ul></body></html>");
        return sb.toString();
    }

    private void saveRecent(String term) {
        recentSearches.remove(term);
        recentSearches.add(0, term);
        if (recentSearches.size() > 10) recentSearches.remove(recentSearches.size() - 1);
        Set<String> set = new HashSet<>(recentSearches);
        prefs.edit().putStringSet(PREF_RECENT, set).apply();
    }

    private void clearRecents() {
        recentSearches.clear();
        prefs.edit().remove(PREF_RECENT).apply();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {}

    private int dpToPx(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    private int getBottomPadding() {
        int resId = getResources().getIdentifier("navigation_bar_height","dimen","android");
        return resId > 0? getResources().getDimensionPixelSize(resId): 0;
    }
}
