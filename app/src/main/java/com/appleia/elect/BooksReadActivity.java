package com.appleia.elect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.appleia.elect.db.eLectSQLiteHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class BooksReadActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    private WebView webView;
    private ImageButton backButton;
    private LinearLayout bottomToolbar;
    private eLectSQLiteHelper dbHelper;
    private String pendingBookId;
    private FrameLayout root;
    private int selectedTabIndex;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        selectedTabIndex=3;
        // match status bar to header color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.header_blue)
            );
        }

        root = new FrameLayout(this);
        setContentView(root);

        setupHeader();
        setupWebView();
        setupBottomToolbar();
    }

    private void setupHeader() {
        // Header
        int headerHeight = dpToPx(56);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(Color.parseColor("#3359A2"));
        FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                headerHeight,
                Gravity.TOP
        );
        root.addView(header, hlp);

        backButton = new ImageButton(this);
        backButton.setImageResource(R.drawable.baseline_chevron_left_24);
        backButton.setBackgroundColor(Color.TRANSPARENT);
        backButton.setColorFilter(Color.WHITE);
        backButton.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                dpToPx(24), dpToPx(24)
        );
        blp.setMarginStart(dpToPx(16));
        header.addView(backButton, blp);

        TextView title = new TextView(this);
        title.setText("Books Read");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tlp.setMarginStart(dpToPx(8));
        header.addView(title, tlp);
    }

    private void setupWebView() {
        int headerHeight = dpToPx(56);

        int toolbarHeight = dpToPx(56);         // your desired 40dp
        //+ getBottomPadding();  // plus nav-bar inset if you still want it
        // ---- WebView (fills between header & toolbar) ----
        // WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("customscheme".equals(uri.getScheme())) {
                    String host = uri.getHost();
                    if ("book".equals(host)) {
                        pendingBookId = uri.getLastPathSegment();
                        startActivity(new Intent(
                                BooksReadActivity.this,
                                IndBookActivity.class
                        ).putExtra("bookId", pendingBookId));
                        return true;
                    }
                    if ("selectdate".equals(host)) {
                        pendingBookId = uri.getPathSegments().get(1);
                        Calendar now = Calendar.getInstance();
                        new DatePickerDialog(
                                BooksReadActivity.this,
                                BooksReadActivity.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        ).show();
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        wlp.setMargins(0, headerHeight, 0, toolbarHeight + getBottomPadding() );
        root.addView(webView, wlp);

        // Data
        dbHelper = new eLectSQLiteHelper(this);
        loadBooksRead();
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


    private void onToolbarItemClicked(int index) {
        Intent intent = null;
        switch (index) {
            case 0: startActivity(new Intent(this, HomePageActivity.class)); break;
            case 1: startActivity(new Intent(this, CatalogueActivity.class)); break;
            case 2: startActivity(new Intent(this, ProgressivePlanActivity.class)); break;
            case 3: /*  intent = new Intent(this, BooksReadActivity.class); break;*/ break;
            case 4: startActivity(new Intent(this, WishListActivity.class)); break;
        }
    }

    private void loadBooksRead() {
        List<Map<String,Object>> books = dbHelper.fetchBooksRead();
        if (books.isEmpty()) {
            String emptyHtml =
                    "<html><head>"
                            + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                            + "<style>"
                            + "body{"
                            +   "background:#DEE3EF;"
                            +   "font-family:-apple-system,sans-serif;"
                            +   "margin:0;"
                            +   "padding:40px;"
                            +   "color:gray;"
                            +   "font-size:20px;"
                            +   "text-align:center;"
                            + "}"
                            + "</style>"
                            + "</head><body>"
                            + "No books to show"
                            + "</body></html>";
            webView.loadDataWithBaseURL(null, emptyHtml, "text/html", "utf-8", null);
        } else {
            String html = generateHtmlFromReadBooks(books);
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        }
    }

    private String generateHtmlFromReadBooks(List<Map<String,Object>> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<style>")
                .append("body{font-family:-apple-system,sans-serif;margin:0;padding:20px;background:#DEE3EF;}")
                .append("#searchBar{width:90%;margin:16px auto;padding:10px;font-size:16px;border:none;border-radius:12px;background:#f0f0f0;}")
                .append(".book-item{background:#fff;margin:10px auto;padding:15px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);cursor:pointer;max-width:600px;}")
                .append(".book-title{font-weight:bold;font-size:18px;color:#0645AD;margin-bottom:4px;}")
                .append(".book-author{font-size:16px;color:#0645AD;margin-bottom:8px;}")
                .append(".book-read-date{font-size:16px;color:#0645AD;}")
                .append(".change-date{margin-left:8px;text-decoration:underline;color:#0645AD;cursor:pointer;}")
                .append("</style>")
                .append("<script>")
                .append("function searchBooks(){")
                .append("  var q=document.getElementById('searchBar').value.toLowerCase();")
                .append("  document.querySelectorAll('.book-item').forEach(function(item){")
                .append("    var txt=item.innerText.toLowerCase();")
                .append("    item.style.display = txt.includes(q)?'block':'none';")
                .append("  });")
                .append("}")
                .append("</script>")
                .append("</head><body>")
                .append("<input id='searchBar' type='text' placeholder='Search by title or author…' onkeyup='searchBooks()' />");

        for (Map<String,Object> row : books) {
            String id       = row.get("id").toString();
            String title    = row.get("BookTitle").toString();
            String author   = row.get("AuthorName").toString();
            String readDate = row.get("ReadDate").toString();

            sb.append("<div class='book-item' onclick=\"window.location.href='customscheme://book/")
                    .append(id).append("';\">")
                    .append("<div class='book-title'>").append(title).append("</div>")
                    .append("<div class='book-author'>By: ").append(author).append("</div>")
                    .append("<div class='book-read-date'>Read on: ").append(readDate)
                    .append("<span class='change-date' onclick=\"event.stopPropagation();window.location.href='customscheme://selectdate/book/")
                    .append(id).append("';\">(change date)</span>")
                    .append("</div>")
                    .append("</div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String mm = String.format("%02d", month + 1);
        String dd = String.format("%02d", dayOfMonth);
        String formatted = dd + "-" + mm + "-" + year;
        dbHelper.updateBookReadDate(pendingBookId, formatted);
        loadBooksRead();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getBottomPadding() {
        int resId = getResources().getIdentifier("navigation_bar_height","dimen","android");
        return resId > 0
                ? getResources().getDimensionPixelSize(resId)
                : 0;
    }
}
