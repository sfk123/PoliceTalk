package com.sheng.android.policetalk;

import android.app.Application;

/**
 * Created by Administrator on 2017/2/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(getApplicationContext());
    }
}
