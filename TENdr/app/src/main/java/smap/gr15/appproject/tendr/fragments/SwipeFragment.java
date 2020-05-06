package smap.gr15.appproject.tendr.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.LinkedList;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.services.MatchService;
import smap.gr15.appproject.tendr.utils.SwipePagerAdapter;

public class SwipeFragment extends Fragment {
    // Implement swipe fragment using: https://developer.android.com/training/animation/screen-slide-2#viewpager
    private static final int NUM_SWIPE_CARDS = 10; // Set to size of profilesToSwipe
    private final int FETCH_PROFILE_WAIT_TIME_MS = 1000;
    private LinkedList<Profile> profilesToSwipe;
    private MatchService matchService;
    private FragmentStateAdapter swipeAdapter;
    private ViewPager2 viewPager;

    public SwipeFragment(MatchService matchService) {
        this.matchService = matchService;

        // SwipeableProfiles er 0, data MatchService ikke er færdig med at hente dem endnu
        // gør MatchServicens init kald async/await https://stackoverflow.com/a/47021042
        //this.profilesToSwipe = matchService.getSwipeableProfiles();
        fetchSwipeableProfiles();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_main_swipe, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        viewPager = getView().findViewById(R.id.main_swipe_card);
        swipeAdapter = new SwipePagerAdapter(this, profilesToSwipe);
        viewPager.setAdapter(swipeAdapter);
    }

    /*@Override In example this makes sense because it is in an activity, but we are in a fragment
                which shouldn't have responsibility for back pressed.
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }*/

    private void fetchSwipeableProfiles() {
        profilesToSwipe = matchService.getSwipeableProfiles();
        if (!matchService.serviceIsInit()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchSwipeableProfiles();
                }
            }, FETCH_PROFILE_WAIT_TIME_MS);
        }
    }
}
