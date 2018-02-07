package com.tool;

import android.content.Context;
import android.util.AndroidRuntimeException;

public class MyToolText {

    private static final Object sTheLock = new Object();
    private static Context sAppContext;

    private static final class IncorrectInitializationException extends AndroidRuntimeException {
        IncorrectInitializationException(String msg) {
            super(msg);
        }
    }
    public static Context getApplicationContext() {
        synchronized (sTheLock) {
            if (sAppContext == null) {
                throw new IncorrectInitializationException(
                        "Create an MyToolText object before calling MyToolText.getApplicationContext()");
            }
            return sAppContext;
        }
    }
    public MyToolText(Context context){
        synchronized (sTheLock) {
            sAppContext = context.getApplicationContext();
        }
    }
}

