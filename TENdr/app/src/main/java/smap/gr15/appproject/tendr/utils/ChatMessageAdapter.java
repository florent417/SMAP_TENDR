package smap.gr15.appproject.tendr.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Chat;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private Conversation conversation;
    private Context context;

    public ChatMessageAdapter(Context context, Conversation conversation) {
        this.chatMessages = conversation.getChatMessages();
        this.conversation = conversation;
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
        ChatMessage chatMessage = chatMessages.get(position);

        holder.textViewMessage.setText(chatMessage.getMessage());
        holder.textViewName.setText(chatMessage.getSender());
        holder.textViewTimeStamp.setText(chatMessage.getTimeStamp().toString());


    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName, textViewMessage, textViewTimeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textView_RecyclerView_chat_Name);
            textViewMessage = itemView.findViewById(R.id.textView_RecyclerView_chat_Message);
            textViewTimeStamp = itemView.findViewById(R.id.textView_RecyclerView_chat_timeStamp);

        }
    }
}
