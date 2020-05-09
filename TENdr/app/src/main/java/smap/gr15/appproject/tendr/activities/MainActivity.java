package smap.gr15.appproject.tendr.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.os.IBinder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.fragments.MatchesFragment;
import smap.gr15.appproject.tendr.fragments.SwipeFragment;
import smap.gr15.appproject.tendr.utils.helpers;
import smap.gr15.appproject.tendr.services.MatchService;

// Implementation of swipe fragment based on: https://developer.android.com/training/animation/screen-slide-2
public class MainActivity extends AppCompatActivity {
    private ServiceConnection matchServiceConnection;
    private MatchService matchService;
    private boolean matchServiceBound;
    private final String LOG = "MainActivity";
    private final String SWIPE_FRAGMENT = "SwipeFragment";
    private SwipeFragment swipeFragment;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;

    @BindView(R.id.activity_auth_toolbar)
    Toolbar _toolbar;

    @BindView(R.id.imageButton_settings)
    ImageButton imageButton_settings;

    @BindView(R.id.imageButton_main)
    ImageButton imageButton_main;

    @BindView(R.id.imageButton_profile)
    ImageButton imageButton_profile;

    //@BindView(R.id.toolbar_main_swipe_chat)
    //Toolbar mainActivityFragmentsToolbar;

    @BindView(R.id.button_main_swipe)
    Button swipeFragmentButton;

    @BindView(R.id.button_main_chat)
    Button matchesFragmentButton;

    private MatchesFragment matchesFragment;
    private final String FRAGMENT_MATCHES = "fragment_matches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);

        helpers.setupCustomActionBar(imageButton_settings, imageButton_main, imageButton_profile, this);

        //mainActivityFragmentsToolbar.findViewById(R.id.toolbar_main_swipe_chat);

        setupMatchService();
    }

    private void setupMatchService() {
        startService(new Intent(MainActivity.this, MatchService.class));
        setupConnectionToMatchService();
        bindToMatchService();
    }

    private void setupSwipeFragment() {
        fragmentManager = getSupportFragmentManager();
        swipeFragment = new SwipeFragment(matchService);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_main_swipe, swipeFragment, SWIPE_FRAGMENT)
                .commit();
    }

    /*
    private void setupMatchesFragment(){
        matchesFragment = new MatchesFragment(matchService);

        fragmentTransaction
                .add(R.id.fragment_main_swipe, matchesFragment, FRAGMENT_MATCHES);
                //.commit();
    }

     */

    private void setupFragmentTransaction(){
        fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
    }


    private void setupConnectionToMatchService() {
        matchServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                matchService = ((MatchService.MatchServiceBinder)service).getService();
                setupFragmentTransaction();
                matchesFragment = new MatchesFragment(matchService);
                //matchesFragment = new MatchesFragment(matchService);

                setupSwipeFragment();
                //setupMatchesFragment();
                // create at method that reacts to pressing the buttons on screen, to choose which fragment to use
                //setupMatchesFragment();
                Log.d(LOG, "Main Activity connected to MatchService");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                matchService = null;
                Log.d(LOG, "Main Activity disconnected from MatchService");
            }
        };
    }

    public void yesButtonSwipeClick(View view) {
        Log.d("SwipeFragment", "yesButton clicked main");
        SwipeFragment swipeFragment = (SwipeFragment) getSupportFragmentManager().findFragmentByTag(SWIPE_FRAGMENT);
        swipeFragment.swipeYes();
    }

    public void noButtonSwipeClick(View view) {
        Log.d("SwipeFragment", "noButton clicked main");
        SwipeFragment swipeFragment = (SwipeFragment) getSupportFragmentManager().findFragmentByTag(SWIPE_FRAGMENT);
        swipeFragment.swipeNo();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void bindToMatchService() {
        if (!matchServiceBound) {
            bindService(new Intent(MainActivity.this,
                    MatchService.class), matchServiceConnection, Context.BIND_AUTO_CREATE);
            matchServiceBound = true;
        }
    }

    /*
    @OnClick(R.id.button_main_chat)
    public void onMatchesClicked(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace();
    }

     */

    @OnClick(R.id.button_main_chat)
    public void onChatClicked(){

        matchesFragmentButton.setTextColor(getResources().getColor(R.color.colorButtons));
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_main_swipe, matchesFragment, FRAGMENT_MATCHES);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        //MatchesFragment swipeFragment = (MatchesFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MATCHES);

    }

    @OnClick(R.id.button_main_swipe)
    public void onSwipeClicked(){
        swipeFragmentButton.setTextColor(getResources().getColor(R.color.colorButtons));

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_main_swipe, swipeFragment, SWIPE_FRAGMENT);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        //SwipeFragment swipeFragment = (SwipeFragment) getSupportFragmentManager().findFragmentByTag(SWIPE_FRAGMENT);
    }

    @Override
    public void onBackPressed(){
        if (getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
        }
        else {
            super.onBackPressed();
        }
    }
}
