package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CheckedTextView;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.services.ProfileService;
import smap.gr15.appproject.tendr.utils.Globals;
import smap.gr15.appproject.tendr.utils.helpers;

public class SettingsActivity extends AppCompatActivity {

    private ServiceConnection profileServiceConnection;
    private ProfileService profileService;
    private boolean profileServiceBound;
    private static final String TAG = "ProfileActivity";
    private FirebaseAuth Auth;

    @BindView(R.id.activity_auth_toolbar)
    Toolbar _toolbar;

    @BindView(R.id.imageButton_settings)
    ImageButton imageButton_settings;

    @BindView(R.id.imageButton_main)
    ImageButton imageButton_main;

    @BindView(R.id.imageButton_profile)
    ImageButton imageButton_profile;

    @BindView(R.id.ShowMeFemale)
    CheckedTextView checkedTextViewFemale;

    @BindView(R.id.ShowMeMale)
    CheckedTextView checkedTextViewMale;

    public static Profile profile = new Profile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);

        setupFirebase();

        setupProfileServiceConnection();

        helpers.setupCustomActionBar(imageButton_settings, imageButton_main, imageButton_profile, this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    protected void onStop() {
        profileService = null;
        unbindService(profileServiceConnection);
        profileServiceBound = false;
        super.onStop();
    }

    private void setupProfileServiceConnection(){
        startService(new Intent(SettingsActivity.this, ProfileService.class));
        setupConnectionToProfileService();
        bindToProfileService();
    }

    private void setupConnectionToProfileService(){
        profileServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                profileService = ((ProfileService.ProfileServiceBinder)service).getService();
                Log.d(TAG, "profile activity connected to profile service");
                profileServiceBound = true;
                profileService.getUserProfile(Auth.getUid(), userProfileOperationsListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                profileService = null;
                profileServiceBound = false;
            }
        };
    }

    private void setupFirebase()
    {
        Auth = FirebaseAuth.getInstance();
    }

    private ProfileService.UserProfileOperationsListener userProfileOperationsListener = new ProfileService.UserProfileOperationsListener() {
        @Override
        public void onGetProfileSuccess(Profile userProfile) {
            profile = userProfile;
        }

        @Override
        public void onUploadPhotoSuccess(String imageUrl) {

        }

        @Override
        public void onDeletePhotoSuccess(String imageUrl) {

        }
    };

    private void bindToProfileService() {
        if (!profileServiceBound) {
            bindService(new Intent(SettingsActivity.this,
                    ProfileService.class), profileServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void setupUserSpecificUI()
    {

    }








}
