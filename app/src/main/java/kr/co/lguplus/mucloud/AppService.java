package kr.co.lguplus.mucloud;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import common.LogManager;

/**
 * Created by njoy on 2016. 8. 25..
 */

public class AppService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.i("Service", "onStart()");
        LogManager.DEBUG("Service - " + "onStart()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.i("Service", "onTaskRemoved()");
        LogManager.DEBUG("Service - " + "onTaskRemoved()");
    }
}
