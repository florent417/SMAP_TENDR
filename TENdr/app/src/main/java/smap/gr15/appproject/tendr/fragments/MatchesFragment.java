package smap.gr15.appproject.tendr.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.models.ProfileList;
import smap.gr15.appproject.tendr.services.MatchService;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MatchesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatchesFragment extends Fragment {
    private final String LOG = "MatchService LOG";
    private int MATCH_LIMIT = 10;
    private final String PROFILES_DB = "profiles";
    private LinkedList<Profile> swipeableProfiles = new LinkedList<Profile>();
    private ProfileList wantedMatches;
    private ProfileList unwantedMatches;
    private List<Profile> successfulMatches = new ArrayList<Profile>();
    private Profile ownProfile;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MatchService matchService;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_matches, container, false);
    }

    private void fetchSuccessfulMatches(List<String> matchIds) {
        if (!matchIds.isEmpty()) {
            db.collection(PROFILES_DB)
                    .whereIn("email", matchIds)
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
                            } else {
                                Log.d(LOG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void fetchConversations(List) {
        db.collection(PROFILES_DB)
                .whereIn("email", matchIds)
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
                        } else {
                            Log.d(LOG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
}
