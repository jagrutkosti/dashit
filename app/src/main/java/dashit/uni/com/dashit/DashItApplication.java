package dashit.uni.com.dashit;

import android.app.Application;
import android.content.Context;

/**
 * Created by Jagrut on 17-Apr-16.
 * To create application context and initiate a global ActivityLifeCycleCallBack
 */
public class DashItApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        DashItApplication.context = getApplicationContext();
        registerActivityLifecycleCallbacks(new ActivityLifeCycleHandler());
    }

    public static Context getAppContext() {
        return DashItApplication.context;
    }
}
