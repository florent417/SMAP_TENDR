package smap.gr15.appproject.tendr.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class MatchService extends Service {
    private String LOG = "MatchService LOG";
    private int MATCH_LIMIT = 10;

    public class MatchServiceBinder extends Binder {
        MatchService getService() { return MatchService.this; }
    }

    private final IBinder binder = new MatchServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG, "MatchService has been created");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create notification channel
        return super.onStartCommand(intent, flags, startId);

        // return START_STICKY
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG, "MatchService has been destroyed");
    }
}
