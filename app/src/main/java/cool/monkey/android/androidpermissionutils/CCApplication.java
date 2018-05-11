package cool.monkey.android.androidpermissionutils;

import android.app.Application;

public class CCApplication extends Application {

    private static CCApplication INSTANCE;

    public static CCApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        ToastHelper.init(getApplicationContext());
    }
}
