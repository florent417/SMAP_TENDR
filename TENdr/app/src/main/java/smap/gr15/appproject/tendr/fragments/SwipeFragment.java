package smap.gr15.appproject.tendr.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.LinkedList;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.services.MatchService;
import smap.gr15.appproject.tendr.utils.SwipeCardAdapter;
import smap.gr15.appproject.tendr.utils.SwipePagerAdapter;

public class SwipeFragment extends Fragment {
    // REMOVE ?? Implement swipe fragment using: https://developer.android.com/training/animation/screen-slide-2#viewpager
    // Implement swipe fragment using: https://stackoverflow.com/questions/34620840/how-to-add-swipe-functionality-on-android-cardview
    // and: https://stackoverflow.com/questions/27293960/swipe-to-dismiss-for-recyclerview/30601554#30601554
    private static final int NUM_SWIPE_CARDS = 10; // Set to size of profilesToSwipe
    private final int FETCH_PROFILE_WAIT_TIME_MS = 1000;
    private final String LOG = "SwipeFragment";
    private int lastSwipeCardPosition = 1;
    private LinkedList<Profile> profilesToSwipe;
    private Profile currentProfileToSwipe;
    private ArrayList<Profile> swipedProfiles = new ArrayList<Profile>();
    private MatchService matchService;
    private FragmentStateAdapter swipeFragmentStateAdapter;
    private ViewPager2 viewPager;
    private RecyclerView swipeRecyclerView;
    private RecyclerView.Adapter swipeAdapter;
    private RecyclerView.LayoutManager swipeLayoutManager;
    private Button yesButton;
    private Button noButton;
    private TextView outOfSinglesMessage;

    public SwipeFragment(MatchService matchService) {
        this.matchService = matchService;

        fetchSwipeableProfiles();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_main_swipe, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupView();
        setupRecyclerView();
    }

    private void setupView() {
        yesButton = getView().findViewById(R.id.button_main_fragment_yes);
        noButton = getView().findViewById(R.id.button_main_fragment_no);
        outOfSinglesMessage = getView().findViewById(R.id.textView_main_swipe_no_swipes);
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
                    swipeLeft();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    swipeRight();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(swipeRecyclerView);
    }


    /*private void setViewPager() {
        viewPager = getView().findViewById(R.id.main_swipe_card);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(LOG, "position: " + position + " positionOffset: " + positionOffset);
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                if (position > lastSwipeCardPosition) {
                    swipeLeft(lastSwipeCardPosition);
                } else if (position < lastSwipeCardPosition) {
                    swipeRight(lastSwipeCardPosition);
                }
                lastSwipeCardPosition = position; // update

                swipedProfiles.add(profilesToSwipe.get(position));

                super.onPageSelected(position);
                String Br = "BR";
            }
        });
    }*/

    private void swipeRight() {
        String tempUserId = currentProfileToSwipe.getUserId();

        removeUserFromSwipeQueue();
        Log.d(LOG, "SwipeRight on " + currentProfileToSwipe);



        // call matchService.swipeRight(tempUserId)
    }

    private void swipeLeft() {
        String tempUserId = currentProfileToSwipe.getUserId();

        removeUserFromSwipeQueue();
        Log.d(LOG, "SwipeLeft on " + currentProfileToSwipe);

        // call matchService.swipeRight(tempUserId)
    }

    private void removeUserFromSwipeQueue() {
        if (profilesToSwipe.size() < 2) {
            fetchSwipeableProfiles();
        }
             updateAdapter();
    }

    private void updateAdapter() {
        if (profilesToSwipe.size() != 0) {///FIXXXX
            currentProfileToSwipe = profilesToSwipe.pop();
        } else {
            // instead add empty card
            currentProfileToSwipe = createEmptyProfileForAdapter();
        }

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
                    fetchSwipeableProfiles();
                }
            }, FETCH_PROFILE_WAIT_TIME_MS);
        } else {
            profilesToSwipe = matchService.getSwipeableProfiles();
            if (profilesToSwipe.size() == 0) {
                TextView outOfSingles = getView().findViewById(R.id.textView_main_swipe_no_swipes);
                outOfSingles.setText(R.string.out_of_singles);
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
