package smap.gr15.appproject.tendr.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import smap.gr15.appproject.tendr.models.Profile;

public class ProfileService extends Service {
    private final IBinder binder = new LocalBinder();

    //region Binder Implementaton
    // Ref: https://developer.android.com/guide/components/bound-services
    // Comments also copied from reference
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    // use another name
    public class LocalBinder extends Binder {
        public ProfileService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ProfileService.this;
        }
    }
    //endregion

    public Profile getUserProfile(){
        return null;
    }
}
