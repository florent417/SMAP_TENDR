package smap.gr15.appproject.tendr.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import smap.gr15.appproject.tendr.R;

public class SwipeCardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_swipe_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //ImageView imageView = (ImageView) getView().findViewById(R.id.foo);
        // or  (ImageView) view.findViewById(R.id.foo);
 /*       viewPager = getView().findViewById(R.id.main_swipe_card);
        swipeAdapter = new SwipeFragment.SwipePagerAdapter(this);
        viewPager.setAdapter(swipeAdapter);*/
    }


}