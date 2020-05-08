package smap.gr15.appproject.tendr.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;

public class SwipeCardFragment extends Fragment {
    private Profile profile;

    public SwipeCardFragment(Profile profile) {
        this.profile = profile;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_swipe_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //ImageView imageView = (ImageView) getView().findViewById(R.id.);
        // or  (ImageView) view.findViewById(R.id.foo);
        TextView nameAndAge = getView().findViewById(R.id.textView_main_swipe_name_age);
        TextView bio = getView().findViewById(R.id.textView_main_swipe_bio);

        nameAndAge.setText(mergeNameAndAge(profile.getFirstName(), profile.getAge()));
        bio.setText(profile.getBio());
    }

    private String mergeNameAndAge(String name, int age) {
        return name + ": " + age;
    }


}