package smap.gr15.appproject.tendr.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.services.MatchService;

public class MainActivity extends AppCompatActivity {
    private ServiceConnection matchServiceConnection;
    private MatchService matchService;
    private boolean matchServiceBound;
    private final String LOG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ////// DEBUG SERVICE
        setupMatchService();
        //////
    }



    ////////// DEBUG SERVICE METHODS
    private void setupMatchService() {
        startService(new Intent(MainActivity.this, MatchService.class));
        setupConnectionToMatchService();
        bindToMatchService();
    }

    private void setupConnectionToMatchService() {
        matchServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                matchService = ((MatchService.MatchServiceBinder)service).getService();
    ///         getAndDisplayAllWords();
                Log.d(LOG, "Main Activity connected to MatchService");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                matchService = null;
                Log.d(LOG, "Main Activity disconnected from MatchService");
            }
        };
    }

    private void bindToMatchService() {
        if (!matchServiceBound) {
            bindService(new Intent(MainActivity.this,
                    MatchService.class), matchServiceConnection, Context.BIND_AUTO_CREATE);
            matchServiceBound = true;
        }
    }

    //////////
}
