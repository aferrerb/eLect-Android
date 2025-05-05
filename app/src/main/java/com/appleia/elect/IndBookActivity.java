package com.appleia.elect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.appleia.elect.db.eLectSQLiteHelper;
import com.google.android.material.textfield.TextInputEditText;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import android.net.Uri;


public class IndBookActivity extends AppCompatActivity {
    private eLectSQLiteHelper dbHelper;
    private String bookId;
    private eLectSQLiteHelper.Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ind_book);

        // 1) Status‐bar match header
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.header_blue)
            );
        }

        // 2) Fetch the book
        dbHelper = new eLectSQLiteHelper(this);
        bookId   = getIntent().getStringExtra("bookId");
        System.out.println("Book iD in IndBook " + bookId);
        book     = dbHelper.fetchBookDetailsByID(bookId);
        if (book == null) {
            Toast.makeText(this, "Book not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3) Header back + title
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvTitle    = findViewById(R.id.tv_title);
        tvTitle.setText(book.title);
        btnBack.setOnClickListener(v -> finish());

        // 4) Detail rows
        setupRow(R.id.row_title,      R.drawable.title,    "Title: "       + book.title, null);
        setupRow(R.id.row_author,     R.drawable.person,  "Author: "      + book.authorName, null);
        //setupRow(R.id.row_link,       R.drawable.link,    "Link: "        + book.link, v ->
        //        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(book.link)))
        //);
        setupRow(
                R.id.row_link,
                R.drawable.link,
                "Link: " + book.link,
                v -> {
                    Uri uri = Uri.parse(book.link);
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

                    // optional: match your app’s header color
                    builder.setToolbarColor(
                            ContextCompat.getColor(IndBookActivity.this, R.color.header_blue)
                    );
                    // optional: show page title in the toolbar
                    builder.setShowTitle(true);
                    // optional: add a default share button
                    builder.addDefaultShareMenuItem();

                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(IndBookActivity.this, uri);
                }
        );
        setupRow(R.id.row_mainpoints, R.drawable.points,  "Main Points: " + book.mainPoints, null);

        String comments =
                !TextUtils.isEmpty(book.comments)     ? book.comments :
                        !TextUtils.isEmpty(book.description)  ? book.description :
                                !TextUtils.isEmpty(book.ca)           ? book.ca :
                                        !TextUtils.isEmpty(book.cb)           ? book.cb :
                                                "";  // fallback if all are null/empty

        setupRow(
                R.id.row_comments,
                R.drawable.comments,
                "Comments: " + comments,
                null
        );

        setupRow(R.id.row_summary,    R.drawable.summary, "Click for summary", v -> {
            // TODO: replace with real GPT call
            Toast.makeText(this, "Summary coming soon", Toast.LENGTH_SHORT).show();
        });

        boolean isRead = dbHelper.isBookRead(bookId);
        setupRow(R.id.row_read,
                isRead ? R.drawable.book_read : R.drawable.book_read,
                isRead ? "Book Read." : "Add to list of books read.",
                v -> {
                    dbHelper.setBookRead(bookId, !isRead);
                    recreate();
                }
        );

        // now tint it:
        View rowRead = findViewById(R.id.row_read);
        int readColor = ContextCompat.getColor(this,
                isRead ? R.color.green : R.color.header_blue);
        ((TextView) rowRead.findViewById(R.id.row_text))
                .setTextColor(readColor);
        ((ImageView) rowRead.findViewById(R.id.row_icon))
                .setImageTintList(ColorStateList.valueOf(readColor));


        boolean inWish = dbHelper.isBookWish(bookId);
        setupRow(R.id.row_wish,
                R.drawable.book_to_read,
                inWish ? "Book in wishlist." : "Add to wishlist.",
                v -> {
                    // ❗️ Add this for debugging:
                    //Log.d("IndBookActivity", "🔔 row_wish clicked!" + bookId);
                    //Toast.makeText(this, "Wish clicked", Toast.LENGTH_SHORT).show();

                    dbHelper.setBookWish(bookId, !inWish);
                    recreate();
                }
        );

        // now tint it:
        View rowWish = findViewById(R.id.row_wish);
        int wishColor = ContextCompat.getColor(this,
                inWish ? R.color.green : R.color.header_blue);
        ((TextView) rowWish.findViewById(R.id.row_text))
                .setTextColor(wishColor);
        ((ImageView) rowWish.findViewById(R.id.row_icon))
                .setImageTintList(ColorStateList.valueOf(wishColor));

        // 5) Notes input + save
        TextInputEditText etNote = findViewById(R.id.et_note);
        etNote.setText(dbHelper.fetchNoteForBookID(bookId));

        Button btnSave = findViewById(R.id.btn_save_note);
        btnSave.setOnClickListener(v -> {
            dbHelper.saveOrUpdateNoteForBookID(bookId, etNote.getText().toString());
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        });

        // 6) Bottom nav
        // Each nav_item.xml has an ImageView id=nav_icon and TextView id=nav_label
        int[][] navConfig = {
                { R.id.nav_home,        R.drawable.home,           R.string.nav_home       },
                { R.id.nav_catalogue,   R.drawable.catalogue,      R.string.nav_catalogue  },
                { R.id.nav_plan,        R.drawable.trending,       R.string.nav_plan       },
                { R.id.nav_books_read,  R.drawable.book_read,      R.string.nav_books_read },
                { R.id.nav_wishlist,    R.drawable.book_to_read,   R.string.nav_wishlist   },
        };
        // highlight index=3
        for (int i = 0; i < navConfig.length; i++) {
            int viewId    = navConfig[i][0];
            int iconRes   = navConfig[i][1];
            int labelRes  = navConfig[i][2];
            View  navItem = findViewById(viewId);
            ImageView iv  = navItem.findViewById(R.id.nav_icon);
            TextView  tv  = navItem.findViewById(R.id.nav_label);
            iv.setImageResource(iconRes);
            tv.setText(labelRes);
            boolean selected = (i == 3);
            int tintColor = ContextCompat.getColor(this,
                    selected ? android.R.color.black : R.color.header_blue
            );
            iv.setImageTintList(ColorStateList.valueOf(tintColor));
            tv.setTextColor(tintColor);

            final Class<?> dest = (
                    i == 0 ? HomePageActivity.class :
                            i == 1 ? CatalogueActivity.class :
                                    i == 2 ? ProgressivePlanActivity.class :
                                            i == 3 ? BooksReadActivity.class : // change this later
                                                    WishListActivity.class
            );
            navItem.setOnClickListener(v -> startActivity(new Intent(this, dest)));
        }
    }

    /**
     * Populate one of the <include layout="@layout/row_icon_text"/> rows.
     */
    private void setupRow(int rowId,
                          int iconRes,
                          String text,
                          View.OnClickListener click) {
        View        row = findViewById(rowId);
        ImageView   iv  = row.findViewById(R.id.row_icon);
        TextView    tv  = row.findViewById(R.id.row_text);
        iv.setImageResource(iconRes);
        tv.setText(text);
        if (click != null) {
            row.setOnClickListener(click);
        } else {
            row.setOnClickListener(null);
        }
    }
}
