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
    private static String CONVERSATION_REFERENCE = "conversations";
    private FirebaseAuth Auth;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReferenceFromUrl("gs://tendr-app-project.appspot.com/pictures").child("date-russian-girl-site-review.png");
    public RecyclerView.Adapter chatMessageAdapter;
    private static String ConversationOppositeUserID;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference conversationRef;

    Conversation conversation = new Conversation();

    @BindView(R.id.activity_auth_toolbar)
    Toolbar _toolbar;

    @BindView(R.id.imageButton_settings)
    ImageButton imageButton_settings;

    @BindView(R.id.imageButton_main)
    ImageButton imageButton_main;

    @BindView(R.id.imageButton_profile)
    ImageButton imageButton_profile;

    @BindView(R.id.RecyclerView_chatActivity)
    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setSupportActionBar(_toolbar);
        helpers.setupCustomActionBar(imageButton_settings, imageButton_main, imageButton_profile, this);

        setupRecyclerView();

        ConversationOppositeUserID = getIntent().getStringExtra("ConversationKey");
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        searchConversationDocumentRef(ConversationOppositeUserID);

    }

    private void getUserConversation(String ref)
    {
        conversationRef = firestore.collection(CONVERSATION_REFERENCE).document(ref);

        // Event for when receiving new chat messages
        setupConversationSnapshotListener();
    }

    //This method should return key to the current conversation the user has clicked
    private void searchConversationDocumentRef(String conversationOppositeUserID)
    {

        CollectionReference findConversation = firestore.collection(CONVERSATION_REFERENCE);
        Query query = findConversation
                .whereEqualTo(firstUser, conversationOppositeUserID)
                .whereEqualTo(firstUser, Auth.getUid())
                .whereEqualTo(secondUser, conversationOppositeUserID)
                .whereEqualTo(secondUser, Auth.getUid());

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                            String ref = doc.getReference().toString();

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
                chatMessageAdapter.notifyDataSetChanged();
            }
        });

    }

    private void setupRecyclerView(){
        chatMessageAdapter = new ChatMessageAdapter(this, conversation);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatMessageAdapter);
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
