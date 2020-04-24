package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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
import smap.gr15.appproject.tendr.utils.Globals;
import smap.gr15.appproject.tendr.utils.helpers;

public class SettingsActivity extends AppCompatActivity {

    private static final String BROADCAST_USER_PROFILE_FETCHED = "BROADCAST_USER_PROFILE_FETCHED";
    private static final String EXTRA_USER_PROFILE_FETCHED = "EXTRA_USER_PROFILE_FETCHED";

    @BindView(R.id.activity_auth_toolbar)
    Toolbar _toolbar;

    @BindView(R.id.imageButton_settings)
    ImageButton imageButton_settings;

    @BindView(R.id.imageButton_main)
    ImageButton imageButton_main;

    @BindView(R.id.imageButton_profile)
    ImageButton imageButton_profile;

    public static Profile profile = new Profile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(_toolbar);

        helpers.setupCustomActionBar(imageButton_settings, imageButton_main, imageButton_profile, this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        BuildUserProfile(currentUser.getUid());
    }



    public void BuildUserProfile(String userId)
    {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection(Globals.FIREBASE_Profiles_PATH).document(userId);
        Task<DocumentSnapshot> documentSnapshotTask = docRef.get();

        documentSnapshotTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();

                if(documentSnapshot.exists())
                {
                    profile = documentSnapshot.toObject(Profile.class);
                    Log.d("PROF", profile.getCity());
                }
            }
        });
    }

}
