package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.Document;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.ChatMessage;
import smap.gr15.appproject.tendr.models.Conversation;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.ChatMessageAdapter;
import smap.gr15.appproject.tendr.utils.helpers;

import static smap.gr15.appproject.tendr.utils.Globals.CONVERSATION_KEY;
import static smap.gr15.appproject.tendr.utils.Globals.FIREBASE_Profiles_PATH;

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
    private String referenceToListCollection;
    private static Profile myProfile;
    private static Profile matchProfile;
    DocumentReference conversationRef;
    List<ChatMessage> chatMessages = new ArrayList<>();
    Conversation conversation = new Conversation();
    ListenerRegistration registration;

    @BindView(R.id.RecyclerView_chatActivity)
    RecyclerView recyclerView;

    @BindView(R.id.imageViewBack_chatActivity)
    ImageView backButton;

    @BindView(R.id.textViewName_chatActivity)
    TextView matchName;

    @BindView(R.id.imageViewPicture_chatActivity)
    ImageView imageViewMatch;

    @BindView(R.id.editTextChatAcitivty)
    EditText editTextChatAcitivty;

    @BindView(R.id.sendButtonChatActivity)
    Button sendButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setupRecyclerView();

        setupOnClickListeners();

        setupFirebase();

        // This should be changed in production!
        //ConversationOppositeUserID = getIntent().getStringExtra("ConversationKey");
        ConversationOppositeUserID = getIntent().getStringExtra(CONVERSATION_KEY);
        Log.d("conkey", ConversationOppositeUserID);

        getProfileOnStartup(Auth.getUid());
        getProfileOnStartup(ConversationOppositeUserID);


    }


    @Override
    protected void onStart() {
        super.onStart();

        searchConversationDocumentRef(ConversationOppositeUserID);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(registration != null)
        {
            registration.remove();
            registration = null;
        }
    }

    private void setupOnClickListeners()
    {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChatMessage();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        sendButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    closeKeyboard();
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();

        super.onBackPressed();

    }

    private void getProfileOnStartup(String Uid)
    {
        DocumentReference docRef = firestore.collection(FIREBASE_Profiles_PATH).document(Uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists())
                    {
                        Profile profile = documentSnapshot.toObject(Profile.class);

                        if(profile.getUserId().equals(Auth.getUid()))
                        {
                            myProfile = profile;
                        }
                        else{
                            matchProfile = profile;
                            matchName.setText(matchProfile.getFirstName());
                            setMatchProfilePicture(imageViewMatch);
                        }
                    }
                }
            }
        });
    }

    private void setMatchProfilePicture(ImageView imageView)
    {
        String picture = matchProfile.getPictures() == null ? "https://pbs.twimg.com/profile_images/749113295299239940/JmxNTCw1.jpg": matchProfile.getPictures().get(0);

        Picasso.get().load(picture).into(imageView);
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
        firestore.collection(CONVERSATION_REFERENCE).document(ref).collection(CONVERSATION_CHAT_COLLECTION).orderBy("timeStamp").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                chatMessages = task.getResult().toObjects(ChatMessage.class);
                adapter.setChatMessages(chatMessages);
                //When conversation go to button
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
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

                            //Set ref to Collection, used when sending to others.
                            referenceToListCollection = ref;

                            getUserConversation(ref);

                            setupConversationSnapshotListener();
                        }
                    }
                });
    }

    private void setupConversationSnapshotListener()
    {
        CollectionReference documentReference = firestore.collection(CONVERSATION_REFERENCE).document(referenceToListCollection).collection(CONVERSATION_CHAT_COLLECTION);
        registration = documentReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    getConversation(referenceToListCollection);
            }
        });


    }

    private void sendChatMessage()
    {
        String message = editTextChatAcitivty.getText().toString();
        //Check to see message
        if(message.isEmpty())
            return;

        ChatMessage chatMessage = new ChatMessage(message, myProfile.getFirstName(), new Date());

        firestore.collection(CONVERSATION_REFERENCE).document(referenceToListCollection).collection(CONVERSATION_CHAT_COLLECTION).add(chatMessage);

        closeKeyboard();

    }

    //This is taken from this youtube video: https://www.youtube.com/watch?v=CW5Xekqfx3I
    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        editTextChatAcitivty.setText("");
        editTextChatAcitivty.clearFocus();
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

    public static Profile getMyProfile(){
        return myProfile;
    }

    public static Profile getMatchProfile(){
        return matchProfile;
    }


}
