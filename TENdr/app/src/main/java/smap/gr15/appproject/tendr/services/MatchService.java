package smap.gr15.appproject.tendr.services;
import smap.gr15.appproject.tendr.activities.ChatActivity;
import smap.gr15.appproject.tendr.activities.MainActivity;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.ConversationNotification;
import smap.gr15.appproject.tendr.utils.Globals;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

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

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static smap.gr15.appproject.tendr.utils.Globals.CONVERSATION_CHAT_COLLECTION;
import static smap.gr15.appproject.tendr.utils.Globals.CONVERSATION_REFERENCE;
import static smap.gr15.appproject.tendr.utils.Globals.FIREBASE_Profiles_PATH;
import static smap.gr15.appproject.tendr.utils.Globals.FRAGMENT;
import static smap.gr15.appproject.tendr.utils.Globals.FRAGMENT_MATCH;
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
    private ProfileList wantedMatches = new ProfileList();
    private ProfileList unwantedMatches = new ProfileList();
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
    private static ListenerRegistration registrationlist;
    private static ListenerRegistration registrationNewMatch;
    private List<String> numberOfMatches = new ArrayList<>();
    private boolean firstTimeEvent = true;

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

        fetchOwnProfileData(Auth.getUid());
    }

    public List<Profile> getSuccessFullMatches(){
        return successfulMatches;
    }

    private void addSuccessFullMatches(Profile profile){
        successfulMatches.add(profile);
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
        registrationlist.remove();
        registrationNewMatch.remove();
        firstTimeEvent = true;
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

        if (swipeableProfiles.size() < 2 && ownProfile != null) {
                updateSwipeQueueIfNeeded();
        }

        return swipeableProfiles;
    }

    public Profile getOwnProfile() {
        return ownProfile;
    }

    public void swipeNo(String noThanksUserId) {
        addProfileToUnwantedMatches(noThanksUserId);
        updateSwipeQueueIfNeeded();
    }

    public void swipeYes(String yesPleaseUserId) {
        Log.d("swipeyescalled", "yes");
        addProfileToWantedMatches(yesPleaseUserId);
        updateSwipeQueueIfNeeded();

        // Check for match! here
        checkForMatch(yesPleaseUserId);
    }

    private void checkForMatch(String wantedMatchUserId) {

        Log.d("checFormatches", String.valueOf(wantedMatchUserId));

        db.collection(WANTED_MATCHES_DB)
                .whereEqualTo("userId", wantedMatchUserId)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("12345", "before");
                        if (task.isSuccessful()) {
                            Log.d("12345", String.valueOf(task.getResult().size()));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG, "Fetching wanted matches list of wanted match: " + document.getId() + " => " + document.getData());
                                ProfileList wantedMatchesOfWantedMatch = document.toObject(ProfileList.class);
                                if(wantedMatchesOfWantedMatch.list != null)
                                {
                                    for (String userIdOfWantedMatchWantedList : wantedMatchesOfWantedMatch.list){
                                        Log.d("wanted", String.valueOf(wantedMatchesOfWantedMatch.list.size()));
                                        Log.d("isthisnull??", String.valueOf(userIdOfWantedMatchWantedList));
                                        if (userIdOfWantedMatchWantedList.equals(Auth.getUid()) && !ownProfile.getMatches().contains(wantedMatchUserId)) {
                                            createMatchIfWithinLimit(wantedMatchUserId);

                                        }
                                    }
                                }

                            }
                        } else {
                            Log.d(LOG, "Error getting wanted matches list documents of wanted match: ", task.getException());
                        }
                    }
                });
    }

    private void createMatchIfWithinLimit(String userIdOfMatch) {
        if (ownProfile.getMatches().size() < 10) {
            db.collection(PROFILES_DB).document(userIdOfMatch)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(LOG, "DocumentSnapshot data: " + document.getData());
                            Profile matchProfile = document.toObject(Profile.class);
                            if (matchProfile != null && matchProfile.getMatches().size() < 10) {
                                // updating other persons matches
                                if (matchProfile.getMatches() == null) {
                                    matchProfile.setMatches(new ArrayList<String>());
                                }
                                List<String> otherPersonsMatches = matchProfile.getMatches();
                                otherPersonsMatches.add(Auth.getUid());
                                matchProfile.setMatches(otherPersonsMatches);
                                updateProfileInDB(matchProfile);

                                // adding other person to own matchlist
                                List<String> ownMatches = ownProfile.getMatches();
                                ownMatches.add(matchProfile.getUserId());
                                ownProfile.setMatches(ownMatches);
                                updateProfileInDB(ownProfile);

                                //Create conversation
                                createNewConversation(Auth.getUid(), matchProfile.getUserId());
                            }

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

    private void checkAmountAndUpdateMatchList(String userId) {
        // check for only 10, otherwise don't update
    }

    private void updateProfileInDB(Profile profile) {
        db.collection(PROFILES_DB).document(profile.getUserId())
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG, "profiles DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG, "Error writing document profiles", e);
                    }
                });
    }


    public void createProfileInDB(Profile profile) {
        // create Profile
        updateWantedListInDB(new ProfileList(profile.getUserId()));
        updateUnwantedListInDB(new ProfileList(profile.getUserId()));
        //  other collections we find we will need
    }

    private void updateSwipeQueueIfNeeded() {
        if (swipeableProfiles.size() <= 10) {
            fetchProfilesForSwiping(ownProfile);
        }
    }

    private void addProfileToUnwantedMatches(String userId) {
        unwantedMatches.list.add(userId);
        if (unwantedMatches.list.size() >= UNWANTED_MATCHES_LIMIT)
        {
            unwantedMatches.list.remove(unwantedMatches.list.size());
        }
        updateUnwantedListInDB(unwantedMatches);
    }

    private void addProfileToWantedMatches(String userId) {
        if(wantedMatches.list.contains(userId))
        {

        }
        else{
            wantedMatches.list.add(userId);
            if (wantedMatches.list.size() >= WANTED_MATCHES_LIMIT)
            {
                wantedMatches.list.remove(wantedMatches.list.size());
            }

            updateWantedListInDB(wantedMatches);
        }
    }

    private void fetchWantedMatches(String userId) {
        db.collection(WANTED_MATCHES_DB)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG, "Fetching wanted matches list item: " + document.getId() + " => " + document.getData());
                                wantedMatches = document.toObject(ProfileList.class);
                            }
                            if (wantedMatches == null || wantedMatches.userId == null) {
                                wantedMatches.userId = Auth.getUid();
                                updateWantedListInDB(new ProfileList(Auth.getUid())); // Should probably remove in release
                            }
                            wantedMatchesFetched = true;
                        } else {
                            Log.d(LOG, "Error getting wanted matches list documents: ", task.getException());
                            updateWantedListInDB(new ProfileList(Auth.getUid())); // Should probably remove in release
                            wantedMatchesFetched = true;
                        }
                    }
                });
    }

    private void fetchUnwantedMatches(String userId) {
        db.collection(UNWANTED_MATCHES_DB)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG, document.getId() + " => " + document.getData());
                                unwantedMatches = document.toObject(ProfileList.class);
                            }
                            if (unwantedMatches == null) {
                                updateUnwantedListInDB(new ProfileList(Auth.getUid())); // Should probably remove in release
                            }
                            unwantedMatchesFetched = true;
                        } else {
                            Log.d(LOG, "Error getting documents: ", task.getException());
                            updateUnwantedListInDB(new ProfileList(Auth.getUid())); // Should probably remove in release
                            unwantedMatchesFetched = true;
                        }

                    }
                });
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
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setColor(0xdf4723)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .build();
    }

    private Notification setupNotificationsCombat(String title, String text, String matchUid)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ConversationKey", matchUid);


        // Whoever reads this, i spend 2 hours fixing FLAG_UPDATE_CURRENT, because of caching - Now i will go out in the sun and get an ice cream.
        // In order to fix this, i had to make over 10 logs and my head was about to explode
        // Then i went to the fridge to get some nice to eat, and guess what, it was fucking empty. Then finally, stackoverflow came to my rescue. Thank you Sagar from StackOverflow
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);

        Log.d("IM IN SETCOMBAT", "COMBAT");
        return new NotificationCompat.Builder(this,
                NOTIFICATIONS_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setColor(0xdf4723)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .build();
    }

    //Used on New Match
    private Notification setupNotificationsCombat(){

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(FRAGMENT, FRAGMENT_MATCH);

        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);

        String title = "You have a new Match";
        String text = "Don't let your match wait too long!";

        return new NotificationCompat.Builder(this,
                NOTIFICATIONS_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setColor(0xdf4723)
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
            setupMatchNotificationListener();
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
        registrationlist = documentReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w("Error", "listen:error", e);
                    return;
                }

                Log.d("key", key);

                ChatMessage doc = queryDocumentSnapshots.getDocumentChanges().get(0).getDocument().toObject(ChatMessage.class);

                String id = getMatchUserUid(key);

                Log.d("idd", id);


                // Ehh I read on stackoverflow that this was the only way to not get the first event on initial call: https://stackoverflow.com/questions/47601038/disable-the-first-query-snapshot-when-adding-a-snapshotlistener
                // It is not pretty though :()
                // || id.equals(ownProfile.getUserId())
                Log.d("docc", doc.getSender());
                if(doc.getTimeStamp().compareTo(new Date(System.currentTimeMillis() - 30000L)) < 0 || id.equals(ownProfile.getUserId()) || isChatActivityTopActivity())
                {
                    Log.d("reutrning", "return");
                    return;
                }

                Log.d("reutrnings", "returns");

                Notification notification;

                if(id.equals(""))
                {
                    notification = setupNotificationsCombat(doc.getSender(), doc.getMessage());
                }
                else{
                    Log.d("matchuid", id);
                    notification = setupNotificationsCombat(doc.getSender(), doc.getMessage(), id);
                }


                notificationManagerCompat.notify(NOTIFICATIONS_ID_INTEGER, notification);
            }
        });


    }

    private String getMatchUserUid(String key)
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

    //https://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android/13994622
    private boolean isChatActivityTopActivity()
    {
        boolean isTop = false;
        String chatActivity = ".activities.ChatActivity";

        ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

        Log.d("topactivity", cn.getShortClassName());

        if(cn.getShortClassName().equals(chatActivity))
            isTop = true;


        return isTop;

    }

    private void setupMatchNotificationListener()
    {
        DocumentReference doc = db.collection(FIREBASE_Profiles_PATH).document(Auth.getUid());

        registrationNewMatch = doc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                Profile profile = new Profile();
                profile = documentSnapshot.toObject(Profile.class);

                if(firstTimeEvent)
                {
                    numberOfMatches = profile.getMatches();
                    firstTimeEvent = false;
                    return;
                }

                Log.d("iminhere1", "here");

                //You cannot remove matches in current version
                if(numberOfMatches.size() != profile.getMatches().size() && profile.getMatches().size() > numberOfMatches.size())
                {
                    // TODO: 09-05-2020 maybe we can use this later
                    List<String> tempMatches = profile.getMatches();
                    tempMatches.removeAll(numberOfMatches);
                    String currentMatch = tempMatches.get(0);

                    db.collection(PROFILES_DB).document(currentMatch).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                addSuccessFullMatches(task.getResult().toObject(Profile.class));

                                Log.d("iminhere2", "here");

                                Notification notification;
                                notification = setupNotificationsCombat();
                                notificationManagerCompat.notify(NOTIFICATIONS_ID_INTEGER, notification);
                            }

                        }
                    });





                }
            }
        });
    }


    //A Comment

    private void updateUnwantedListInDB(ProfileList unwantedList) {
        String userId = Auth.getUid();

        db.collection(UNWANTED_MATCHES_DB).document(userId)
                .set(unwantedList, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG, "UnwantedList DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG, "Error writing document UnwantedList", e);
                    }
                });

    }


    private void updateWantedListInDB(ProfileList wantedList) {
        String userId = Auth.getUid();

        Log.d(LOG, "ProfileList size: " + wantedList.list);
        db.collection(WANTED_MATCHES_DB).document(userId)
                .set(wantedList, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG, "WantedList DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG, "Error writing document WantedList", e);
                    }
                });

    }

    private void createNewConversation(String myUserId, String matchUserId){

        Conversation conversation = new Conversation();
        conversation.setFirstUserId(matchUserId);
        conversation.setSecondUserId(myUserId);
        conversation.setCombinedUserUid(compareUsers(matchUserId,myUserId));


        db.collection(Globals.FIREBASE_CONVERSATIONS_PATH).document().set(conversation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    db.collection(Globals.FIREBASE_CONVERSATIONS_PATH).whereEqualTo("combinedUserUid", conversation.getCombinedUserUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                String key = task.getResult().getDocuments().get(0).getId();

                                Log.d("key", key);

                                db.collection(Globals.FIREBASE_CONVERSATIONS_PATH).document(key).collection("chatMessages").add(new ChatMessage("Welcome to TENdr - Start out by saying something funny","TENdr", new Date())).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful())
                                        {
                                            Log.d("complete", "complete");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private String compareUsers(String user1, String user2)
    {
        int sizeOfUser = user1.compareTo(user2);
        String combinedId = "";

        if(sizeOfUser > 0)
        {
            combinedId = user1 + user2;
        }
        else
        {
            combinedId = user2 + user1;
        }

        return combinedId;
    }

}
