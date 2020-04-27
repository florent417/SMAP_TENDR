package smap.gr15.appproject.tendr.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import smap.gr15.appproject.tendr.R;

public class SwipeFragment extends Fragment {
    // Implement swipe fragment using: https://developer.android.com/training/animation/screen-slide-2#viewpager

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_swipe_card, container, false);
    }




}
