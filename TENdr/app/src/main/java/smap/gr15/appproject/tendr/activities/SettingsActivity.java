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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.services.ProfileService;
import smap.gr15.appproject.tendr.utils.Globals;
import smap.gr15.appproject.tendr.utils.helpers;

import static smap.gr15.appproject.tendr.utils.Globals.FIREBASE_Profiles_PATH;

public class SettingsActivity extends AppCompatActivity {

    private ServiceConnection profileServiceConnection;
    private ProfileService profileService;
    private boolean profileServiceBound;
    private static final String TAG = "ProfileActivity";
    private FirebaseAuth Auth;
    private static String TAG_ERROR_NO_PREFERENCES = "You must select at least one gender preference";
    private static String TAG_GENEREAL_ERROR = "Error!";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private MediaPlayer mediaPlayer;

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

    @BindView(R.id.LocationCountryOfResidence)
    EditText LocationCountryOfResidence;

    @BindView(R.id.CityCityOfResidence)
    EditText CityCityOfResidence;

    @BindView(R.id.OccupationOccupation)
    EditText OccupationOccupation;

    @BindView(R.id.applyButtonSettings)
    Button applyButtonSettings;

    @BindView(R.id.buttonSignOut)
    Button buttonSignOut;

    public static Profile profile = new Profile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);

        setupOnClickListeners();

        setupFirebase();

        setupProfileServiceConnection();

        setupMediaPlayerForFunnySong();

        helpers.setupCustomActionBar(imageButton_settings, imageButton_main, imageButton_profile, this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getUserProfile(Auth.getUid());


    }

    @Override
    protected void onStop() {
        profile = null;
        profileService = null;
        unbindService(profileServiceConnection);
        profileServiceBound = false;
        super.onStop();
    }

    @Override
    protected void onRestart() {

        setupProfileServiceConnection();

        super.onRestart();
    }

    private void setupMediaPlayerForFunnySong()
    {
        mediaPlayer = MediaPlayer.create(this, R.raw.yoooo);
    }

    private void setupOnClickListeners()
    {
        applyButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyChangesProfile();
            }
        });

        checkedTextViewFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedTextViewFemale.setChecked(!checkedTextViewFemale.isChecked());
                checkedTextViewFemale.setError(null);
            }
        });

        checkedTextViewMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedTextViewMale.setChecked(!checkedTextViewMale.isChecked());
                checkedTextViewFemale.setError(null);
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.signOut();
                Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                startActivity(intent);
                // TODO: use finishAffinity instead. Removes all prior activities in the stack
                finishAffinity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "onresume called");
        setupMediaPlayerForFunnySong();
    }

    private void getUserProfile(String userid)
    {
        firestore.collection(FIREBASE_Profiles_PATH).document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    Log.d("Successfully", "new user:" + Auth.getUid());

                    profile = task.getResult().toObject(Profile.class);

                    setupUserSpecificUI();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(profileService != null)
        {
            profile = null;
            profileService = null;
            unbindService(profileServiceConnection);
            profileServiceBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
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
                getUserProfile(Auth.getUid());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                profileService = null;
                //profileServiceBound = false;
            }
        };
    }

    private void setupFirebase()
    {
        Auth = FirebaseAuth.getInstance();
    }


    private void bindToProfileService() {
        if (!profileServiceBound) {
            bindService(new Intent(SettingsActivity.this,
                    ProfileService.class), profileServiceConnection, Context.BIND_AUTO_CREATE);
            profileServiceBound = true;
        }
    }

    private void setupUserSpecificUI()
    {
        Log.d("profilesize", String.valueOf(profile.getGenderPreference().size()));
        //Gender preferences
        for (String s : profile.getGenderPreference())
        {
            if(s.equals("Female"))
            {
                Log.d("here1", "here");
                checkedTextViewFemale.setChecked(true);
            }
            if(s.equals("Male"))
            {
                Log.d("here2", "here");
                checkedTextViewMale.setChecked(true);
            }
        }

        //Location
        LocationCountryOfResidence.setText(profile.getCountry());

        //City
        CityCityOfResidence.setText(profile.getCity());

        //OccupationOccupation
        OccupationOccupation.setText(profile.getOccupation());

    }

    private void applyChangesProfile()
    {
        final String Location = LocationCountryOfResidence.getText().toString().trim();
        final String City = CityCityOfResidence.getText().toString().trim();
        final String Occupation = OccupationOccupation.getText().toString().trim();
        ArrayList<String> genderPrefences = new ArrayList<>();
        if(checkedTextViewMale.isChecked())
        {
            genderPrefences.add("Male");
        }
        if(checkedTextViewFemale.isChecked())
        {
            genderPrefences.add("Female");
        }
        if(genderPrefences.isEmpty())
        {
            checkedTextViewFemale.setError(TAG_ERROR_NO_PREFERENCES);
            return;
        }
        if(Location.isEmpty())
        {
            LocationCountryOfResidence.setError(TAG_GENEREAL_ERROR);
            LocationCountryOfResidence.requestFocus();
            return;
        }
        if(City.isEmpty())
        {
            CityCityOfResidence.setError(TAG_GENEREAL_ERROR);
            CityCityOfResidence.requestFocus();
            return;
        }
        if(Occupation.isEmpty())
        {
            OccupationOccupation.setError(TAG_GENEREAL_ERROR);
            OccupationOccupation.requestFocus();
            return;
        }
        if(genderPrefences.equals(profile.getGenderPreference()) && Location.equals(profile.getCountry()) && City.equals(profile.getCity()) && Occupation.equals(profile.getOccupation()))
        {
            Toast.makeText(getApplicationContext(), "You haven't made any changes", Toast.LENGTH_SHORT).show();
            return;
        }

        profile.setCountry(Location);
        profile.setCity(City);
        profile.setOccupation(Occupation);
        profile.setGenderPreference(genderPrefences);


        firestore.collection(FIREBASE_Profiles_PATH).document(Auth.getUid()).set(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_LONG).show();
                    mediaPlayer.start();
                }
            }
        });

    }








}
