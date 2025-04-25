package com.appleia.elect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.gridlayout.widget.GridLayout;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private final String[] titles = {
            "Catalogue", "Progressive Plan", "Books Read", "Wishlist",
            "Search", "Suggest a Book", "Why Read?", "Info"
    };

    private final int[] icons = {
            R.drawable.books_white, R.drawable.graph_icon,
            R.drawable.graph_icon, R.drawable.graph_icon,
            R.drawable.graph_icon, R.drawable.graph_icon,
            R.drawable.graph_icon, R.drawable.graph_icon
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full-screen flags if you still want them…
        setContentView(R.layout.activity_home_page);

        GridLayout grid = findViewById(R.id.iconGrid);
        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < titles.length; i++) {
            View item = inflater.inflate(R.layout.home_icon_item, grid, false);

            // bind icon + label as before
            ImageView img = item.findViewById(R.id.iconImage);
            TextView lbl = item.findViewById(R.id.iconLabel);
            img.setImageResource(icons[i]);
            lbl.setText(titles[i]);
            final int index = i;
            item.setOnClickListener(v -> handleIconTap(index));

            // *** Key part: give each item a 0dp width and 1f column weight ***
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;  // 0dp
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            // span exactly 1 column, but take up 1 part of the available space
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            // optional: vertical spacing
            params.setMargins(10, 10, 10, 10);

            item.setLayoutParams(params);
            grid.addView(item);
        }
    }

    private void handleIconTap(int index) {
        Toast.makeText(this, "Tapped: " + titles[index], Toast.LENGTH_SHORT).show();
        // TODO: navigate based on index…
    }
}