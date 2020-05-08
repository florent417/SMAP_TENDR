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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.models.ProfileList;

public class MatchService extends Service {
    private final String LOG = "MatchService LOG";
    private int MATCH_LIMIT = 10;
    private final String PROFILES_DB = "profiles";
    private final String UNWANTED_MATCHES_DB = "unwantedMatches";
    private final String WANTED_MATCHES_DB = "wantedMatches";
    private int PROFILES_TO_FETCH_FOR_SWIPING_AT_ONCE = 20;
    private int UNWANTED_MATCHES_LIMIT = 100;
    private int WANTED_MATCHES_LIMIT = 100;
    private boolean successfulMatchesFetched = false;
    private boolean wantedMatchesFetched = false;
    private boolean unwantedMatchesFetched = false;
    private boolean swipeableProfilesFetched = false;
    private LinkedList<Profile> swipeableProfiles = new LinkedList<Profile>();
    private ProfileList wantedMatches;
    private ProfileList unwantedMatches;
    private ArrayList<Profile> successfulMatches = new ArrayList<Profile>();
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

        fetchOwnProfileData(FirebaseAuth.getInstance().getUid());
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

    public boolean serviceIsInit() {
        return successfulMatchesFetched && wantedMatchesFetched && unwantedMatchesFetched && swipeableProfilesFetched;
    }

    // Should perhaps also have a broadcast method. Activities can get all matches at startup, and
    // should instantly get updated if a new match happens
    public ArrayList<Profile> getMatches() {
        return successfulMatches;
    }

    public LinkedList<Profile> getSwipeableProfiles() {
        // return profiles, then empty and get new profiles

        if (swipeableProfiles.size() == 0 && ownProfile != null) {
                updateSwipeQueueIfNeeded();
        }

        return swipeableProfiles;
    }

    public void swipeNo(Profile noThanksProfile) {
        swipeableProfiles.remove(noThanksProfile);

        addProfileToUnwantedMatches(noThanksProfile);

        updateSwipeQueueIfNeeded();
    }

    public void swipeYes(Profile yesPleaseProfile) {
        swipeableProfiles.remove(yesPleaseProfile);

        addProfileToWantedMatches(yesPleaseProfile);

        updateSwipeQueueIfNeeded();
    }

    public void createProfileInDB(Profile profile) {
        // create Profile
        // create wantedMatches
        // create unwantedMatches
        //  other collections we find we will need
    }

    private void checkProfileIsInitInDB() {

    }

    private void updateSwipeQueueIfNeeded() {
        if (swipeableProfiles.size() <= 10) {
            fetchProfilesForSwiping(ownProfile);
        }
    }

    private void addProfileToUnwantedMatches(Profile profile) {
        unwantedMatches.list.add(profile.getUserId());
        if (unwantedMatches.list.size() >= UNWANTED_MATCHES_LIMIT)
        {
            unwantedMatches.list.remove(unwantedMatches.list.size());

            // Update database with data. Remove just the one instead of sending whole list of 100
        }
        // Update database with data of just the 1 new person
    }

    private void addProfileToWantedMatches(Profile profile) {
        wantedMatches.list.add(profile.getUserId());
        if (wantedMatches.list.size() >= WANTED_MATCHES_LIMIT)
        {
            wantedMatches.list.remove(wantedMatches.list.size());
            // Update database with data. Remove just the one instead of sending whole list of 100
        }

        // Update database with data of just the 1 new person
    }

    private void fetchWantedMatches(String userId) {
        db.collection(WANTED_MATCHES_DB).document(userId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(LOG, "DocumentSnapshot data: " + document.getData());
                        wantedMatches = document.toObject(ProfileList.class);
                        wantedMatchesFetched = true;
                    } else {
                        Log.d(LOG, "No such document");
                        wantedMatchesFetched = true;
                    }
                } else {
                    Log.d(LOG, "get failed with ", task.getException());
                    wantedMatchesFetched = true;
                }
            }
        });
    }

    private void fetchUnwantedMatches(String userId) {
        // fetch code


        unwantedMatchesFetched = true;
    }

    private void fetchOwnProfileData(String profileKey) {
        db.collection(PROFILES_DB).document(profileKey)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(LOG, "DocumentSnapshot data: " + document.getData());
                        ownProfile = document.toObject(Profile.class);
                        fetchSuccessfulMatches(ownProfile.getMatches());
                        fetchWantedMatches(ownProfile.getUserId());
                        fetchUnwantedMatches(ownProfile.getUserId());
                        fetchProfilesForSwiping(ownProfile);
                    } else {
                        Log.d(LOG, "No such document");
                    }
                } else {
                    Log.d(LOG, "get failed with ", task.getException());
                }
            }
        });
    }

    // It is not the size of the dataset we're querying, that is the primary factor for how long a
    // query takes, rather it's the size of the data the query returns. Bandwith is the limiting factor.
    // Source: https://medium.com/firebase-developers/why-is-my-cloud-firestore-query-slow-e081fb8e55dd
    private void fetchSuccessfulMatches(List<String> matchIds) {
        successfulMatchesFetched = true;
        if (!matchIds.isEmpty()) {
            db.collection(PROFILES_DB)
                    .whereIn("userId", matchIds)
                    .limit(MATCH_LIMIT)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                successfulMatches = new ArrayList<Profile>();
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    Log.d(LOG, document.getId() + " => " + document.getData());
                                    Profile matchedProfile = document.toObject(Profile.class);
                                    successfulMatches.add(matchedProfile);
                                }
                                successfulMatchesFetched = true;
                            } else {
                                Log.d(LOG, "Error getting documents: ", task.getException());
                                successfulMatchesFetched = true;
                            }
                        }
                    });
        }
    }

    // Currently has no smart algorithme to prefer people of same city, only matches base on same country
    private void fetchProfilesForSwiping(Profile ownProfiles) {
        db.collection(PROFILES_DB)
                .whereEqualTo("country", ownProfiles.getCountry())
                .whereArrayContains("genderPreference", ownProfiles.getGender())
                .limit(PROFILES_TO_FETCH_FOR_SWIPING_AT_ONCE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(LOG, document.getId() + " => " + document.getData());
                                Profile swipeableProfile = document.toObject(Profile.class);
                                swipeableProfiles.add(swipeableProfile);
                            }
                            swipeableProfilesFetched = true;
                        } else {
                            Log.d(LOG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

}
