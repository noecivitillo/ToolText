package demo.tooltextdemo;

import android.app.Application;

import com.tool.MyToolText;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new MyToolText(getApplicationContext());
    }
}
