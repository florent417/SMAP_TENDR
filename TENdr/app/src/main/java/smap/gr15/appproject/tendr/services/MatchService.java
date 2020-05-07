package smap.gr15.appproject.tendr.services;
import smap.gr15.appproject.tendr.activities.ChatActivity;
import smap.gr15.appproject.tendr.activities.MainActivity;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.ConversationNotification;
import smap.gr15.appproject.tendr.utils.Globals;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.models.ProfileList;

import static smap.gr15.appproject.tendr.utils.Globals.CONVERSATION_CHAT_COLLECTION;
import static smap.gr15.appproject.tendr.utils.Globals.CONVERSATION_REFERENCE;
import static smap.gr15.appproject.tendr.utils.Globals.comparedUser;
import static smap.gr15.appproject.tendr.utils.Globals.firstUser;
import static smap.gr15.appproject.tendr.utils.Globals.secondUser;

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
    public static final String NOTIFICATIONS_ID = "NOTIFICATIONS_ID";
    public static final String NOTIFICATIONS_NAME = "NOTIFICATIONS_NAME";
    public static final Integer NOTIFICATIONS_ID_INTEGER = 1;
    private ExecutorService notificationsExecutor;
    private NotificationManagerCompat notificationManagerCompat;
    private List<ConversationNotification> ConversationNotification = new ArrayList<>();
    private FirebaseAuth Auth;
    ListenerRegistration registration;

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

        Auth = FirebaseAuth.getInstance();

        fetchOwnProfileData(FirebaseAuth.getInstance().getUid());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create notification channel

        // Message notifications
        setupNewMessageNotifications();
        updateNewMessageNotifications();

        return super.onStartCommand(intent, flags, startId);


        // return START_STICKY
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
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

    private void setupNewMessageNotifications()
    {
        notificationManagerCompat = NotificationManagerCompat.from(this);

        setupChannel();

        Notification notification = setupNotificationsCombat("Welcome to TENdr","Here you will receive new messages");

        notificationManagerCompat.notify(NOTIFICATIONS_ID_INTEGER, notification);

        startForeground(NOTIFICATIONS_ID_INTEGER, notification);
    }

    private void setupChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { //needed because channels are not supported on older versions

            Log.d("IM IN SETUP" , "SETUP");

            NotificationChannel mChannel = new NotificationChannel(NOTIFICATIONS_ID,
                    NOTIFICATIONS_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private Notification setupNotificationsCombat(String title, String text)
    {
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Log.d("IM IN SETCOMBAT", "COMBAT");
        return new NotificationCompat.Builder(this,
                NOTIFICATIONS_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .build();
    }

    private Notification setupNotificationsCombat(String title, String text, String matchUid)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ConversationKey", matchUid);

        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, intent, 0);

        Log.d("IM IN SETCOMBAT", "COMBAT");
        return new NotificationCompat.Builder(this,
                NOTIFICATIONS_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .build();
    }

    private void updateNewMessageNotifications()
    {
        if(notificationsExecutor == null)
        {
            notificationsExecutor = Executors.newSingleThreadExecutor();
        }

        notificationsExecutor.submit(UpdateNotificationsThread);
    }

    private Runnable UpdateNotificationsThread = new Runnable(){

        @Override
        public void run() {
            populateConversationList();
        }
    };

    private void populateConversationList(){
        String myUserId = Auth.getUid();

        //Get collections where the user is involved
        db.collection(CONVERSATION_REFERENCE).whereEqualTo(firstUser, myUserId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<ConversationNotification> tempConversation = new ArrayList<>();

                    tempConversation = task.getResult().toObjects(ConversationNotification.class);
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                    for (int i = 0; i < tempConversation.size(); i++)
                    {
                        String key = documentSnapshots.get(i).getId();
                        tempConversation.get(i).setDocKey(key);

                        ConversationNotification.add(tempConversation.get(i));

                        Log.d("sizeOf", String.valueOf(ConversationNotification.size()));
                        Log.d("key", ConversationNotification.get(i).getDocKey());
                        setupConversationSnapshotListener(key);
                    }
                }
            }
        });

        db.collection(CONVERSATION_REFERENCE).whereEqualTo(secondUser, myUserId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<ConversationNotification> tempConversation = new ArrayList<>();
                    tempConversation = task.getResult().toObjects(ConversationNotification.class);
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                    for (int i = 0; i < tempConversation.size(); i++)
                    {
                        String key = documentSnapshots.get(i).getId();
                        tempConversation.get(i).setDocKey(key);
                        ConversationNotification.add(tempConversation.get(i));

                        Log.d("sizeOf", String.valueOf(ConversationNotification.size()));
                        Log.d("key", ConversationNotification.get(i).getDocKey());
                        setupConversationSnapshotListener(key);
                    }

                }
            }
        });


    }

    private void setupConversationSnapshotListener(String key)
    {
        CollectionReference documentReference = db.collection(CONVERSATION_REFERENCE).document(key).collection(CONVERSATION_CHAT_COLLECTION);
        registration = documentReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w("Error", "listen:error", e);
                    return;
                }

                ChatMessage doc = queryDocumentSnapshots.getDocumentChanges().get(0).getDocument().toObject(ChatMessage.class);

                // Ehh I read on stackoverflow that this was the only way to not get the first event on initial call: https://stackoverflow.com/questions/47601038/disable-the-first-query-snapshot-when-adding-a-snapshotlistener
                // It is not pretty though
                if(doc.getTimeStamp().compareTo(new Date(System.currentTimeMillis() - 30000L)) < 0 || doc.getSender().equals(ownProfile.getFirstName()))
                    return;


                Notification notification;

                String id = getUserUid(key);

                if(id.equals(""))
                {
                    notification = setupNotificationsCombat(doc.getSender(), doc.getMessage());
                }
                else{
                    notification = setupNotificationsCombat(doc.getSender(), doc.getMessage(), id);
                }


                notificationManagerCompat.notify(NOTIFICATIONS_ID_INTEGER, notification);
            }
        });


    }

    private String getUserUid(String key)
    {
        //find key
        for( ConversationNotification c : ConversationNotification)
        {
            if(c.getDocKey().equals(key))
            {
                if(!c.getFirstUserId().equals(Auth.getUid()))
                    return c.getFirstUserId();
                else{
                    return c.getSecondUserId();
                }
            }
        }

        return "";
    }


}
