package com.appleia.elect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private String[] titles = {
            "Catalogue", "Progressive Plan", "Books Read", "Wishlist",
            "Search", "Suggest a Book", "Why Read?", "Info"
    };

    private int[] icons = {
            R.drawable.books_white, R.drawable.graph_icon, R.drawable.graph_icon, R.drawable.graph_icon,
            R.drawable.graph_icon, R.drawable.graph_icon, R.drawable.graph_icon, R.drawable.graph_icon
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_home_page);

        View icon1 = findViewById(R.id.icon1); // icon1 = root view of first include
        ImageView img1 = icon1.findViewById(R.id.iconImage);
        TextView txt1 = icon1.findViewById(R.id.iconLabel);
        img1.setImageResource(R.drawable.books_white);
        txt1.setText("Catalogue");

        // Row 1, right icon
        View icon2 = findViewById(R.id.icon2);
        ImageView img2 = icon2.findViewById(R.id.iconImage);
        TextView txt2 = icon2.findViewById(R.id.iconLabel);
        img2.setImageResource(R.drawable.graph_icon);
        txt2.setText("Progressive Plan");

        View icon3 = findViewById(R.id.icon3); // icon1 = root view of first include
        ImageView img3 = icon3.findViewById(R.id.iconImage);
        TextView txt3 = icon3.findViewById(R.id.iconLabel);
        img3.setImageResource(R.drawable.books_white);
        txt3.setText("Catalogue");

        // Row 1, right icon
        View icon4 = findViewById(R.id.icon4);
        ImageView img4 = icon4.findViewById(R.id.iconImage);
        TextView txt4 = icon4.findViewById(R.id.iconLabel);
        img4.setImageResource(R.drawable.graph_icon);
        txt4.setText("Progressive Plan");

        View icon5 = findViewById(R.id.icon5); // icon1 = root view of first include
        ImageView img5 = icon5.findViewById(R.id.iconImage);
        TextView txt5 = icon5.findViewById(R.id.iconLabel);
        img5.setImageResource(R.drawable.books_white);
        txt5.setText("Catalogue");

        // Row 1, right icon
        View icon6 = findViewById(R.id.icon6);
        ImageView img6 = icon6.findViewById(R.id.iconImage);
        TextView txt6 = icon6.findViewById(R.id.iconLabel);
        img6.setImageResource(R.drawable.graph_icon);
        txt6.setText("Progressive Plan");

        View icon7 = findViewById(R.id.icon7); // icon1 = root view of first include
        ImageView img7 = icon7.findViewById(R.id.iconImage);
        TextView txt7 = icon7.findViewById(R.id.iconLabel);
        img7.setImageResource(R.drawable.books_white);
        txt7.setText("Catalogue");

        // Row 1, right icon
        View icon8 = findViewById(R.id.icon2);
        ImageView img8 = icon8.findViewById(R.id.iconImage);
        TextView txt8 = icon8.findViewById(R.id.iconLabel);
        img8.setImageResource(R.drawable.graph_icon);
        txt8.setText("Progressive Plan");


        for (int i = 0; i < titles.length; i++) {
            View item = getLayoutInflater().inflate(R.layout.home_icon_item, null);

            ImageView icon = item.findViewById(R.id.iconImage);
            TextView label = item.findViewById(R.id.iconLabel);

            icon.setImageResource(icons[i]);
            label.setText(titles[i]);

            final int index = i;
            item.setOnClickListener(v -> handleIconTap(index));


        }
    }

    private void handleIconTap(int index) {
        Toast.makeText(this, "Tapped: " + titles[index], Toast.LENGTH_SHORT).show();
        // TODO: Add actual navigation logic here
    }
}
