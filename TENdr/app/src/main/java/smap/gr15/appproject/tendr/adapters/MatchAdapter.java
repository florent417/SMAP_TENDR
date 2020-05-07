package smap.gr15.appproject.tendr.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;

// The idea on how to implement adapter with recycler view is influenced by this playlist on YT
// https://www.youtube.com/watch?v=5T144CbTwjc&list=PLk7v1Z2rk4hjHrGKo9GqOtLs1e2bglHHA&index=2&fbclid=IwAR16HBg3NMwz2uDT9gbiUgP6QquDEVK5S1UEx3nz49kTvtU_Wisl9XpowUc*/
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {
    private static final String TAG = "MatchAdapter";
    // TODO: add profiles for pictures as well
    private List<Conversation> conversations = new ArrayList<>();
    private List<Profile> matchedProfiles = new ArrayList<>();
    private Context context;

    public MatchAdapter(Context context, List<Conversation> conversations, List<Profile> matchedProfiles){
        this.context = context;
        this.conversations = conversations;
        this.matchedProfiles = matchedProfiles;
        Log.d(TAG, "MatchAdapter: " + conversations.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View matchView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.match, parent, false);
        return new ViewHolder(matchView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: add for profile pictures as well

        Profile currentProfile = matchedProfiles.get(position);
        String firstProfilePictureUrl=null;
        if (currentProfile.getPictures() != null && currentProfile.getPictures().size() > 0) {
            firstProfilePictureUrl = currentProfile.getPictures().get(0);
        }

        Picasso.get()
                .load(firstProfilePictureUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(holder.profilePictureImageView);

        Conversation currentConversation = conversations.get(position);
        List<ChatMessage> conversationChatMessages = currentConversation.getChatMessages();
        ChatMessage lastChatMsg = conversationChatMessages.get(conversationChatMessages.size()-1);
        holder.lastMsgEditText.setText(lastChatMsg.getMessage());



        holder.nameTextView.setText(currentProfile.getFirstName());
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView profilePictureImageView;
        private TextView nameTextView;
        private EditText lastMsgEditText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            lastMsgEditText = itemView.findViewById(R.id.LastMsg_EditText_match);
            profilePictureImageView = itemView.findViewById(R.id.ProfileImage_ImageView_match);
            nameTextView = itemView.findViewById(R.id.Name_TextView_match);
        }
    }
}
