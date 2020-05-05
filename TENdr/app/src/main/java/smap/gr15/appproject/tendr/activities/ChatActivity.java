package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.Document;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.utils.ChatMessageAdapter;
import smap.gr15.appproject.tendr.utils.helpers;

// Inspired from this youtube Video https://www.youtube.com/watch?v=n8QWeqeUeA0
public class ChatActivity extends AppCompatActivity {

    private static String firstUser = "firstUserId";
    private static String secondUser = "secondUserId";
    private static String comparedUser = "combinedUserUid";
    private static String CONVERSATION_REFERENCE = "conversations";
    private static String CONVERSATION_CHAT_COLLECTION = "chatMessages";
    private FirebaseAuth Auth;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReferenceFromUrl("gs://tendr-app-project.appspot.com/pictures").child("date-russian-girl-site-review.png");
    public ChatMessageAdapter adapter;
    private static String ConversationOppositeUserID;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference conversationRef;
    List<ChatMessage> chatMessages = new ArrayList<>();
    Conversation conversation = new Conversation();

    @BindView(R.id.RecyclerView_chatActivity)
    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setupRecyclerView();

        setupFirebase();


        //ConversationOppositeUserID = getIntent().getStringExtra("ConversationKey");
        ConversationOppositeUserID = "9PH4nGqkaQNmhrIAygcxddO4ljl2";
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("USERUID", Auth.getUid());
        Log.d("compared", compareUsers());
        searchConversationDocumentRef(ConversationOppositeUserID);

    }

    private void setupFirebase()
    {
        Auth = FirebaseAuth.getInstance();
    }

    private void getUserConversation(String ref)
    {
        DocumentReference docRef = firestore.collection(CONVERSATION_REFERENCE).document(ref);


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    conversation = task.getResult().toObject(Conversation.class);

                    //Now Get Chat List
                    getConversation(ref);


                }
            }
        });

        //Log.d("confRefss", conversationRef);

        // Event for when receiving new chat messages
        //setupConversationSnapshotListener();
    }

    public void getConversation(String ref)
    {
        firestore.collection(CONVERSATION_REFERENCE).document(ref).collection(CONVERSATION_CHAT_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                chatMessages = task.getResult().toObjects(ChatMessage.class);

                Log.d("sizeofmessages", String.valueOf(chatMessages.size()));

                for (ChatMessage chat : chatMessages)
                {

                    Log.d("chatmessages", chat.getMessage());
                }

                adapter.setChatMessages(chatMessages);

                adapter.notifyDataSetChanged();

                Log.d("itemcount", String.valueOf(adapter.getItemCount()));

            }
        });
    }

    private String compareUsers()
    {
        int sizeOfUser = Auth.getUid().compareTo(ConversationOppositeUserID);

        if(sizeOfUser > 0)
        {
            conversation.setCombinedUserUid(Auth.getUid() + ConversationOppositeUserID);
        }
        else
        {
            conversation.setCombinedUserUid(ConversationOppositeUserID + Auth.getUid());
        }

        return conversation.getCombinedUserUid();
    }

    //This method should return key to the current conversation the user has clicked
    private void searchConversationDocumentRef(String conversationOppositeUserID)
    {
        CollectionReference findConversation = firestore.collection(CONVERSATION_REFERENCE);
        Query query = findConversation
                .whereEqualTo(comparedUser, compareUsers());

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                            String ref = doc.getId();

                            getUserConversation(ref);
                        }
                    }
                });
    }

    private void setupConversationSnapshotListener()
    {
        conversationRef.addSnapshotListener((documentSnapshot, e) -> {
            if(e != null)
            {
                Log.d("Error", e.getLocalizedMessage());
            }

            if(documentSnapshot != null && documentSnapshot.exists())
            {
                ChatMessage newChaMessage = (ChatMessage) documentSnapshot.getData();
                conversation.addChatMessage(newChaMessage);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void setupRecyclerView(){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(this, chatMessages);
        recyclerView.setAdapter(adapter);
    }



    // Currently not in use
    private void getImageUrl(String child)
    {
        Task geturl = storageReference.getDownloadUrl();

        geturl.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Object documentSnapshot = task.getResult();

                    String url = documentSnapshot.toString();

                    Log.d("urlabc", url);
                }
            }
        });
    }


}
