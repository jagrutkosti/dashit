package dashit.uni.com.dashit;

import android.app.Application;
import android.content.Context;

/**
 * Created by Jagrut on 17-Apr-16.
 */
public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        registerActivityLifecycleCallbacks(new MyLifeCycleHandler());
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
