package smap.gr15.appproject.tendr.utils;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.LinkedList;

import smap.gr15.appproject.tendr.fragments.SwipeCardFragment;
import smap.gr15.appproject.tendr.models.Profile;

public class SwipePagerAdapter extends FragmentStateAdapter {
    private LinkedList<Profile> swipeableProfiles;

    /*public SwipePagerAdapter(Fragment fragment) {
        super(fragment);
    }*/

    public SwipePagerAdapter(Fragment fragment, LinkedList<Profile> swipeableProfiles) {
        super(fragment);
        this.swipeableProfiles = swipeableProfiles;
    }

    // this one needs to return the swipe cards with their filled data. Use the position to pass
    // a profile from at list i guess.
    @Override
    public Fragment createFragment(int position) {
        return new SwipeCardFragment(swipeableProfiles.getFirst());
    }

    @Override
    public int getItemCount() {
        if (swipeableProfiles != null) {
            return swipeableProfiles.size();
        } else {
            return 0;
        }
    }
}
