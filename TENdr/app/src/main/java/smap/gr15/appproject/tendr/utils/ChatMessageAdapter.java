package smap.gr15.appproject.tendr.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    private List<Conversation> conversationList;
    private Context context;

    public ChatMessageAdapter(Context context, List<Conversation> conversationList)
    {
        this.conversationList = conversationList;
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
        Conversation conversationItem = conversationList.get(position);

        List<ChatMessage> messages =  conversationItem.getChatMessages();

        holder.textViewName.setText(messages.get(position).getProfile().getFirstName());
        holder.textViewMessage.setText(messages.get(position).getMessage());
        Picasso.with(context)
                .load(messages.get(position).getProfile().getPictures().get(0))
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public String getImagesUriFromFirebase(String Uri)
    {
        //Maybe we need this

        return Uri;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textViewName, textViewMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_RecyclerView_chat);
            textViewName = itemView.findViewById(R.id.textView_RecyclerView_chat_Name);
            textViewMessage = itemView.findViewById(R.id.textView_RecyclerView_chat_Message);

        }
    }
}
