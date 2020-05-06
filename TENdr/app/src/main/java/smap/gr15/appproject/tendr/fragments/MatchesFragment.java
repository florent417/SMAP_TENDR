package smap.gr15.appproject.tendr.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class MatchesFragment extends Fragment implements MainActivity.ConnectedToServices {
    private static final String TAG = "MatchesFragment";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String testUID1 = "9PH4nGqkaQNmhrIAygcxddO4ljl2";
    private String testUID2 = "T0Wg4ZuO7Cg4X2aBnVAHFqizlAf1";
    private String ref = "jN9BhcJ4LZpnoDzXNVsJ";
    // Ref: https://stackoverflow.com/questions/56659321/what-does-uf8ff-mean-in-java
    private String JAVA_UNICODE_ESCAPE_CHAR = "\uf8ff";
    private MatchService matchService = null;
    // RecyclerView
    private RecyclerView recyclerView;
    private MatchAdapter matchAdapter;
    private RecyclerView.LayoutManager layoutManager;
    List<Conversation> convos = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public MatchesFragment() {
        // Required empty public constructor
    }

    public MatchesFragment(MatchService matchService){
        this.matchService = matchService;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MatchesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MatchesFragment newInstance(String param1, String param2) {
        MatchesFragment fragment = new MatchesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getConversations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_matches, container, false);
    }

    private void getConversations(){
        CollectionReference findMatches = db.collection(Globals.FIREBASE_CONVERSATIONS_PATH);
        Query getConvosQuery = findMatches
                .whereGreaterThanOrEqualTo("combinedUserUid", testUID2)
                .whereLessThanOrEqualTo("combinedUserUid", testUID2 + JAVA_UNICODE_ESCAPE_CHAR);

        getConvosQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        convos = task.getResult().toObjects(Conversation.class);
                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                        for (int i = 0; i < convos.size(); i++){
                            Conversation tempConvo = convos.get(i);
                            String docRef = documentSnapshots.get(i).getId();
                            getMessages(i, docRef);
                        }
                        /*
                        for(DocumentSnapshot documentSnapshot : task.getResult()){

                            Conversation tempConvo = documentSnapshot.toObject(Conversation.class);
                            Conversation tempConvo2 = convos.get(0);
                            Log.d(TAG, "Convos index: " + convos.indexOf(tempConvo));
                            Log.d(TAG, "Convo somehting1: " + tempConvo.getCombinedUserUid());
                            Log.d(TAG, "Convo somehting2: " + convos.get(0).getCombinedUserUid());
                            String docRef = documentSnapshot.getId();
                            getMessages(tempConvo, docRef);
                        }

                         */

                    }
                });
    }

    private void getMessages(int convoIndex, String conversationDocRef){
        db.collection(Globals.FIREBASE_CONVERSATIONS_PATH).document(conversationDocRef).collection(Globals.FIREBASE_CHAT_MSG_PATH)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, "Inside get chats");
                        List<ChatMessage> chatMessages = task.getResult().toObjects(ChatMessage.class);
                        Conversation conversation = convos.get(convoIndex);
                        conversation.setChatMessages(chatMessages);
                        convos.set(convoIndex, conversation);
                        setupRecyclerView(convos);
                        //Log.d(TAG, "Got convo: " + convos.get(0).getCombinedUserUid());
                        Log.d(TAG, Integer.toString(convos.size()));
                    }
                });
    }

    private void setupRecyclerView(List<Conversation> conversations){
        recyclerView = getView().findViewById(R.id.RecyclerView_Matches_OverView);
        // Maybe another context
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        matchAdapter = new MatchAdapter(this, conversations);
        recyclerView.setAdapter(matchAdapter);
        matchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectedToMatchService(MatchService matchService) {
        this.matchService = matchService;
        getConversations();
    }
}
