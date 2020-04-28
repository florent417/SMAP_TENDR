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

import java.util.List;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Chat;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {



    private List<Chat> chatList;
    private Context context;

    public ChatMessageAdapter(Context context, List<Chat> chatList)
    {
        this.chatList = chatList;
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
        Chat chatItem = chatList.get(position);

        //holder.
        /*holder.textViewName.setText(messages.get(position).getProfile().getFirstName());
        holder.textViewMessage.setText(messages.get(position).getMessage());
        Picasso.with(context)
                .load(messages.get(position).getProfile().getPictures().get(0))
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(holder.imageView);

         */

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public String getImagesUriFromFirebase(String Uri)
    {
        //Maybe we need this

        return Uri;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textViewName, textViewMessage, textViewTimeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_RecyclerView_chat);
            textViewName = itemView.findViewById(R.id.textView_RecyclerView_chat_Name);
            textViewMessage = itemView.findViewById(R.id.textView_RecyclerView_chat_Message);
            textViewTimeStamp = itemView.findViewById(R.id.textView_RecyclerView_chat_timeStamp);

        }
    }
}
