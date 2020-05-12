package smap.gr15.appproject.tendr.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.activities.ChatActivity;
import smap.gr15.appproject.tendr.activities.MainActivity;
import smap.gr15.appproject.tendr.adapters.MatchAdapter;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.models.ProfileList;
import smap.gr15.appproject.tendr.services.MatchService;
import smap.gr15.appproject.tendr.utils.Globals;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MatchesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatchesFragment extends Fragment {
    private static final String TAG = "MatchesFragment";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Ref: https://stackoverflow.com/questions/56659321/what-does-uf8ff-mean-in-java
    private String JAVA_UNICODE_ESCAPE_CHAR = "\uf8ff";
    private MatchService matchService = null;
    // RecyclerView
    private RecyclerView recyclerView;
    private MatchAdapter matchAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Conversation> convos = new ArrayList<>();
    private List<Profile> profiles = new ArrayList<>();
    private final int GET_MATCHES_WAIT_TIME_MS = 1000;
    private final static int CHAT_MESSAGE_REQUEST = 7;
    private List<String> combinedUserId = new ArrayList<>();

    private View view = null;

    private FirebaseAuth Auth = FirebaseAuth.getInstance();


    public MatchesFragment() {
        // Required empty public constructor
    }

    public MatchesFragment (MatchService matchService){
        this.matchService = matchService;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private void updateMatchAdapter(){
        matchAdapter.updateData(convos, profiles);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);
        // Inflate the layout for this fragment
        this.view = view;
        setupRecyclerView();
        getMatches();
        getConversations();


        return view;
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

    private void getConversations(){

        db.collection(Globals.FIREBASE_Profiles_PATH).document(Auth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    Profile myProfile = task.getResult().toObject(Profile.class);

                    List<String> mymatches = new ArrayList<>();

                    if(!myProfile.getMatches().isEmpty() || myProfile.getMatches() == null)
                    {
                        mymatches = myProfile.getMatches();
                    }
                    else{
                        mymatches.add("sometestdata");
                    }

                    for (String match : mymatches)
                    {
                        if(!combinedUserId.contains(compareUsers(Auth.getUid(), match)))
                        {
                            combinedUserId.add(compareUsers(Auth.getUid(), match));
                        }
                    }

                    CollectionReference findMatches = db.collection(Globals.FIREBASE_CONVERSATIONS_PATH);
                    Query getConvosQuery = findMatches
                            .whereIn("combinedUserUid", combinedUserId);

                    getConvosQuery.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<Conversation> conversationsFromQuery = task.getResult().toObjects(Conversation.class);
                                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                                    for (int i = 0; i < conversationsFromQuery.size(); i++){
                                        Log.d(TAG, "inside for loop convo query:");
                                        String docRef = documentSnapshots.get(i).getId();
                                        Log.d(TAG, "Docreference: " + docRef);
                                        getLastMessage(conversationsFromQuery.get(i), docRef, conversationsFromQuery);
                                    }
                                }
                            });

                }
            }
        });
    }

    private void getLastMessage(Conversation conversation, String conversationDocRef, List<Conversation> tempConvos){
        CollectionReference lastMsgRef = db
                .collection(Globals.FIREBASE_CONVERSATIONS_PATH)
                .document(conversationDocRef)
                .collection(Globals.FIREBASE_CHAT_MSG_PATH);

        Query lastMsgQuery = lastMsgRef
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(1);

        lastMsgQuery
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (convos.size() >= tempConvos.size()){
                                convos.clear();
                            }
                            List<ChatMessage> chatMessages = task.getResult().toObjects(ChatMessage.class);
                            conversation.setChatMessages(chatMessages);
                            convos.add(conversation);

                            //convos.set(conversationIndex, conversation);
                            updateMatchAdapter();
                        } else{
                            Log.d(TAG, "Mesages exception: " + task.getException().toString());
                        }

                    }
                });
    }

    private void setupRecyclerView(){
        recyclerView = view.findViewById(R.id.RecyclerView_Matches_OverView);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        matchAdapter = new MatchAdapter(view.getContext(), convos, profiles);
        matchAdapter.setOnMatchClickListener(onMatchClickListener);
        recyclerView.setAdapter(matchAdapter);
    }

    private void getMatches(){
        if(!matchService.serviceIsInit()){
            Log.d(TAG, "getMatches: convos size: " + convos.size());
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getMatches();
                }
            }, GET_MATCHES_WAIT_TIME_MS);
        } else{
            profiles = matchService.getSuccessFullMatches();
            //TODO: Think about adding a viewmodel instead
        }
    }

    private MatchAdapter.OnMatchClickListener onMatchClickListener = new MatchAdapter.OnMatchClickListener() {
        @Override
        public void onMatchClick(String matchProfileUId) {
            Log.d(TAG, "onMatchClick: ");
            Intent intent = new Intent(view.getContext(), ChatActivity.class);

            intent.putExtra(Globals.CONVERSATION_KEY, matchProfileUId);

            startActivityForResult(intent, CHAT_MESSAGE_REQUEST);
            // TODO: Find out what to do here, finish?
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHAT_MESSAGE_REQUEST && resultCode == Activity.RESULT_OK){
            getConversations();
        }
    }
}
