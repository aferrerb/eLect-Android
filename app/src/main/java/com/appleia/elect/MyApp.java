package com.appleia.elect;

import android.app.Application;

import io.branch.referral.Branch;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Branch.enableLogging();             // optional, helps you see Branch logs
        Branch.getAutoInstance(this);       // initialize Branch globally
    }
}
