package common;

import android.util.Log;

/**
 * Created by njoy on 2015-04-02.
 */
public class LogManager {
    public static void DEBUG(String msg){
        if(SystemProperty.IS_DEBUG) {
            Log.d(SystemProperty.DEBUG, msg);
        }
    }

    public static void ERROR(String msg){
        if(SystemProperty.IS_DEBUG)
            Log.e(SystemProperty.DEBUG, msg);
    }
}
