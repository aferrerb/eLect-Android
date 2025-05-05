package com.appleia.elect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class DatePickerActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    public static final String EXTRA_BOOK_ID       = "bookId";
    public static final String EXTRA_SELECTED_DATE = "selectedDate";
    public static final int    REQUEST_CODE        = 1001;

    private String bookId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the bookId from the Intent
        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);

        // Show the DatePicker immediately
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(
                this,
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    @Override
    public void onDateSet(DatePicker view,
                          int year,
                          int month,
                          int dayOfMonth) {
        // month is zero-based
        String mm = String.format("%02d", month + 1);
        String dd = String.format("%02d", dayOfMonth);
        String formatted = dd + "-" + mm + "-" + year;

        // Return the result
        Intent result = new Intent();
        result.putExtra(EXTRA_BOOK_ID, bookId);
        result.putExtra(EXTRA_SELECTED_DATE, formatted);
        setResult(RESULT_OK, result);
        finish();
    }
}

