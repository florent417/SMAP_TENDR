package smap.gr15.appproject.tendr.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.services.MatchService;
import smap.gr15.appproject.tendr.utils.SwipeCardAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static smap.gr15.appproject.tendr.utils.Globals.FIREBASE_Profiles_PATH;

public class SwipeFragment extends Fragment {
    // Implement swipe fragment using: https://stackoverflow.com/questions/34620840/how-to-add-swipe-functionality-on-android-cardview
    // and: https://stackoverflow.com/questions/27293960/swipe-to-dismiss-for-recyclerview/30601554#30601554
    private final int FETCH_PROFILE_WAIT_TIME_MS = 1000;
    private final String LOG = "SwipeFragment";
    private LinkedList<Profile> profilesToSwipe;
    private Profile currentProfileToSwipe;
    private Profile ownProfile;
    private MatchService matchService;
    private RecyclerView swipeRecyclerView;
    private RecyclerView.Adapter swipeAdapter;
    private RecyclerView.LayoutManager swipeLayoutManager;
    private TextView outOfSinglesMessage;
    private FirebaseAuth Auth;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public SwipeFragment(MatchService matchService) {
        this.matchService = matchService;


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_swipe, container, false);

        Auth = FirebaseAuth.getInstance();

        setupView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserProfile(Auth.getUid());
    }

    private void getUserProfile(String userid)
    {
        firestore.collection(FIREBASE_Profiles_PATH).document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    Log.d("Successfully", "new user:" + Auth.getUid());

                    ownProfile = task.getResult().toObject(Profile.class);

                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerView();
        fetchSwipeableProfiles();

    }

    @Override
    public void onStop() {
        Log.d("myprofile", "iminhere");
        ownProfile = null;
        super.onStop();

    }

    private void setupView(View view) {
        outOfSinglesMessage = view.findViewById(R.id.textView_main_swipe_no_swipes);
    }

    private void setupRecyclerView() {
        swipeRecyclerView = getView().findViewById(R.id.main_swipe_card);
        swipeRecyclerView.setHasFixedSize(true);

        swipeLayoutManager = new SwipeLinearLayoutManager(getContext());
        swipeRecyclerView.setLayoutManager(swipeLayoutManager);

        ItemTouchHelper.SimpleCallback simpleItemTouchHelper =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                Log.d(LOG, "onSwiped direction: " + direction);
                if (direction == ItemTouchHelper.LEFT) {
                    swipeNo();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    swipeYes();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(swipeRecyclerView);
    }

    public void swipeYes() {
        Log.d("ownprofileisnull", String.valueOf(ownProfile == null));
        if (ownProfile.getMatches() != null && ownProfile.getMatches().size() < 10) {
            String tempUserId = currentProfileToSwipe.getUserId();

            removeUserFromSwipeQueue();
            //Log.d(LOG, "SwipeRight on " + currentProfileToSwipe.getUserId());

            matchService.swipeYes(tempUserId);
        } else {
            activateTooManyMatchesPopUp(getView());
            refreshSameAdapter();
        }
    }

    public void swipeNo() {
        String tempUserId = currentProfileToSwipe.getUserId();

        removeUserFromSwipeQueue();
        Log.d(LOG, "SwipeLeft on " + currentProfileToSwipe);

        matchService.swipeNo(tempUserId);
    }

    // based on: https://stackoverflow.com/questions/5944987/how-to-create-a-popup-window-popupwindow-in-android
    public void activateTooManyMatchesPopUp(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.fragment_swipe_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, -250);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void removeUserFromSwipeQueue() {
        if (profilesToSwipe.size() < 2) {
            fetchSwipeableProfiles();
        }
             updateAdapter();
    }

    private void updateAdapter() {
        if (profilesToSwipe.size() != 0) {
            currentProfileToSwipe = profilesToSwipe.pop();
        } else {
            Log.d(LOG, "updateAdapter called: null prof created");
            currentProfileToSwipe = createEmptyProfileForAdapter();
            outOfSinglesMessage.setText(R.string.out_of_singles);
        }

        swipeAdapter = new SwipeCardAdapter(getContext(), currentProfileToSwipe);
        swipeRecyclerView.setAdapter(swipeAdapter);
    }

    private void refreshSameAdapter() {
        swipeAdapter = new SwipeCardAdapter(getContext(), currentProfileToSwipe);
        swipeRecyclerView.setAdapter(swipeAdapter);
    }

    private Profile createEmptyProfileForAdapter() {
        return null;
    }

    private void fetchSwipeableProfiles() {
        if (!matchService.serviceIsInit()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG, "fetchSwipeableProfiles: if statement");

                    fetchSwipeableProfiles();
                }
            }, FETCH_PROFILE_WAIT_TIME_MS);
        } else {
            Log.d(LOG, "fetchSwipeableProfiles: else statement");
            profilesToSwipe = matchService.getSwipeableProfiles();

            ownProfile = matchService.getOwnProfile();

            outOfSinglesMessage.setText("");
            if (profilesToSwipe.size() == 0) {
                outOfSinglesMessage.setText(R.string.out_of_singles);
            }
            updateAdapter();
        }
    }

    public class SwipeLinearLayoutManager extends LinearLayoutManager {
        public SwipeLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }
}
