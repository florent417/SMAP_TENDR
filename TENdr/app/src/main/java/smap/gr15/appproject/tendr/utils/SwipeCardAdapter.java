package smap.gr15.appproject.tendr.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.fragments.SwipeCardFragment;
import smap.gr15.appproject.tendr.models.Profile;

public class SwipeCardAdapter extends RecyclerView.Adapter<SwipeCardAdapter.SwipeCardViewHolder> {
        private Context context;
        private Profile profile;

        public static class SwipeCardViewHolder extends RecyclerView.ViewHolder {
            private TextView nameAndAge;
            private TextView bio;
            private ImageView profilePicture;

            public SwipeCardViewHolder(View itemView) {
                super(itemView);
                nameAndAge = itemView.findViewById(R.id.textView_main_swipe_name_age);
                bio = itemView.findViewById(R.id.textView_main_swipe_bio);
                // profilePicture =
            }
        }

        // Only give the adapter the one new card it needs, everytime we use setAdapter
        // The card that is to be shown, must be controlled from SwipeFragment, as it is there that
        // we got isSwiped() which needs to remove the one we just swiped on
        public SwipeCardAdapter(Context context, Profile profile) {
            this.context = context;
            this.profile = profile;
        }

        @Override
        public SwipeCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SwipeCardViewHolder(new SwipeCard(context));
        }

        @Override
        public void onBindViewHolder(SwipeCardViewHolder holder, int position) {
            if (profile != null) {
                holder.nameAndAge.setText(profile.getFirstName() + ", " + profile.getAge());
                holder.bio.setText(profile.getBio());
            } else {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }

        @Override
        public int getItemCount() {
            return 1;// never forget
        }


    }