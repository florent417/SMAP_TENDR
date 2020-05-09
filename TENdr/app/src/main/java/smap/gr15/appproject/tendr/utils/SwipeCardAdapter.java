package smap.gr15.appproject.tendr.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import smap.gr15.appproject.tendr.R;
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
                profilePicture = itemView.findViewById(R.id.imageView_main_swipe);
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
                fetchProfilePicture(holder);
            } else {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }

        private void fetchProfilePicture(SwipeCardViewHolder holder) {
            //Profile currentProfile = matchedProfiles.get(position);
            String firstProfilePictureUrl=null;
            if (profile.getPictures() != null && profile.getPictures().size() > 0) {
                firstProfilePictureUrl = profile.getPictures().get(0);
            }

            Picasso.get()
                    .load(firstProfilePictureUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(holder.profilePicture);
            // profilePicture =
        }

        @Override
        public int getItemCount() {
            return 1;// never forget
        }


    }