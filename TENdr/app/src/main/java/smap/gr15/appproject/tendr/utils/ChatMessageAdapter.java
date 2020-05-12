package smap.gr15.appproject.tendr.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.activities.ChatActivity;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Profile;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private Context context;
    private Profile myProfile = new Profile();
    private Profile matchProfile = new Profile();

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        notifyDataSetChanged();
    }

    public void setChatMessages(ChatMessage chatMessage)
    {
        this.chatMessages.add(chatMessage);
        notifyDataSetChanged();
    }

    public ChatMessageAdapter(Context context, List<ChatMessage> chatMessage) {
        this.chatMessages = chatMessage;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View conversationItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item,parent,false);
        return new ViewHolder(conversationItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageAdapter.ViewHolder holder, int position) {
        final ChatMessage chatMessage = chatMessages.get(position);
        holder.textViewMessage.setText(chatMessage.getMessage());
        holder.textViewName.setText(chatMessage.getSender());
        holder.textViewTimeStamp.setText(chatMessage.getTimeStamp().toString());

        loadProfilesAndSetPic(holder.image, chatMessage.getSender());

    }

    private void loadProfilesAndSetPic(ImageView imageView, String sender)
    {

        this.myProfile = ChatActivity.getMyProfile();
        this.matchProfile = ChatActivity.getMatchProfile();


        if(myProfile.getFirstName().equals(sender))
        {
            String picture = null;
            if(myProfile.getPictures() != null && myProfile.getPictures().size() > 0)
            {
                picture = myProfile.getPictures().isEmpty() ? "https://clipartart.com/images/2-year-old-boy-clipart-4.jpg": myProfile.getPictures().get(0);
            }

            Picasso.get()
                    .load(picture)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(imageView);
        }
        else if(sender.equals("TENdr"))
        {
            String picture = null;
            Picasso.get()
                    .load(picture)
                    .placeholder(android.R.drawable.alert_dark_frame)
                    .into(imageView);
        }
        else{
            String picture = null;
            if(matchProfile.getPictures() != null && matchProfile.getPictures().size() > 0)
            {
                picture = matchProfile.getPictures().isEmpty() ? "https://clipartart.com/images/2-year-old-boy-clipart-4.jpg": matchProfile.getPictures().get(0);
            }

            Picasso.get()
                    .load(picture)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(imageView);
        }
    }



    @Override
    public int getItemCount() {
        return chatMessages.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private ImageView image;
        private TextView textViewName, textViewMessage, textViewTimeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.textView_RecyclerView_chat_image);
            textViewName = itemView.findViewById(R.id.textView_RecyclerView_chat_Name);
            textViewMessage = itemView.findViewById(R.id.textView_RecyclerView_chat_Message);
            textViewTimeStamp = itemView.findViewById(R.id.textView_RecyclerView_chat_timeStamp);
            linearLayout = itemView.findViewById(R.id.linearLayout);

        }
    }
}
