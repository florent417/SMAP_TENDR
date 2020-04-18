package smap.gr15.appproject.tendr.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

import smap.gr15.appproject.tendr.models.Match;
import smap.gr15.appproject.tendr.models.Profile;

public class MatchService extends Service {
    private String LOG = "MatchService LOG";
    private int MATCH_LIMIT = 10;
    private LinkedList<Match> swipeableProfiles;
    private List<Match> wantedMatches;
    private LinkedList<Match> unwantedMatches;
    private Match[] matches;
    private Profile ownProfile;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public class MatchServiceBinder extends Binder {
        public MatchService getService() { return MatchService.this; }
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
        getOwnProfileData("alexander8@hotmail.com");
        // Now we have profile data. Use this to search for a number matches with matching
        // gender, compatible genderPreference, and same country.

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

    private void initSwipeQueue() {

    }

    private void getOwnProfileData(String profileKey) {
        db.collection("profiles").document(profileKey)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Profile profile = document.toObject(Profile.class);

                    if (document.exists()) {
                        Log.d(LOG, "DocumentSnapshot data: " + document.getData());
                        ownProfile = profile;
                    } else {
                        Log.d(LOG, "No such document");
                    }
                } else {
                    Log.d(LOG, "get failed with ", task.getException());
                }
            }
        });
    }

}
