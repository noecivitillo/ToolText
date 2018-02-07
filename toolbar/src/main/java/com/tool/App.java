package com.tool;


import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context context;

    @Override public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();

    }
    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);

    }
    public static Context getContext(){
        return App.context;
    }


}

