package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.services.ProfileService;
import smap.gr15.appproject.tendr.utils.helpers;
import smap.gr15.appproject.tendr.adapters.ProfileImageAdapter;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.Globals;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private String existingDoc = "9JMORa2zRPWqeMEgt3IAsaXLhHC2";
    private String pathToPics = "pictures/";
    private String pathToUserPics = pathToPics + existingDoc + "/";

    // Connection to ProfileService
    private ServiceConnection profileServiceConnection;
    private ProfileService profileService;
    private boolean profileServiceBound;

    private Profile currentLoggedInProfile = null;
    private static int PICK_IMAGE_REQUEST =  2;
    private ProgressDialog progressDialog;

    @BindView(R.id.BioProfileMultilineText)
    EditText bioEditText;
    @BindView(R.id.OccupationProfileEditText)
    EditText occupationEditText;
    @BindView(R.id.CityProfileEditText)
    EditText cityEditTxt;
    @BindView(R.id.GenderProfileEditText)
    EditText genderEditTxt;
    @BindView(R.id.SaveProfileButton)
    Button saveBtn;

    @BindView(R.id.ImagesProfileGrid)
    GridView gridView;
    List<String> imgUrls = new ArrayList<>();

    private ProfileImageAdapter adapter;

    @BindView(R.id.activity_auth_toolbar)
    Toolbar _toolbar;

    @BindView(R.id.imageButton_settings)
    ImageButton imageButton_settings;

    @BindView(R.id.imageButton_main)
    ImageButton imageButton_main;

    @BindView(R.id.imageButton_profile)
    ImageButton imageButton_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        progressDialog  = new ProgressDialog(ProfileActivity.this);

        setupProfileServiceConnection();

        //getProfileAndPopulate();

        setSupportActionBar(_toolbar);

        helpers.setupCustomActionBar(imageButton_settings, imageButton_main, imageButton_profile, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupProfileServiceConnection(){
        startService(new Intent(ProfileActivity.this, ProfileService.class));
        setupConnectionToProfileService();
        bindToProfileService();
    }

    private void bindToProfileService() {
        if (!profileServiceBound) {
            bindService(new Intent(ProfileActivity.this,
                    ProfileService.class), profileServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void setupConnectionToProfileService(){
        profileServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                profileService = ((ProfileService.ProfileServiceBinder)service).getService();
                Log.d(TAG, "profile activity connected to profile service");
                profileServiceBound = true;
                profileService.getUserProfile(existingDoc, userProfileOperationsListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                profileService = null;
                profileServiceBound = false;
            }
        };
    }

    private ProfileService.UserProfileOperationsListener userProfileOperationsListener = new ProfileService.UserProfileOperationsListener(){
        @Override
        public void onGetProfileSuccess(Profile userProfile) {
            bioEditText.setText(userProfile.getBio());
            occupationEditText.setText(userProfile.getOccupation());
            cityEditTxt.setText(userProfile.getCity());
            genderEditTxt.setText(userProfile.getGender());

            if (userProfile.getPictures() != null)
                imgUrls = userProfile.getPictures();

            adapter = new ProfileImageAdapter(ProfileActivity.this, imgUrls);
            gridView.setAdapter(adapter);
            adapter.setOnGridItemClickListener(onGridItemClickListener);
        }

        @Override
        public void onDeletePhotoSuccess(String imageUrl){
            imgUrls.remove(imageUrl);
            adapter.setImgUrls(imgUrls);
            progressDialog.dismiss();
            Toast.makeText(ProfileActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
        }
    };

    @OnClick(R.id.SaveProfileButton)
    void saveBtn(View view){
        currentLoggedInProfile.setBio(bioEditText.getText().toString());
        currentLoggedInProfile.setOccupation(occupationEditText.getText().toString());
        currentLoggedInProfile.setCity(cityEditTxt.getText().toString());
        currentLoggedInProfile.setGender(genderEditTxt.getText().toString());
        currentLoggedInProfile.setPictures(imgUrls);
        firestore.collection(Globals.FIREBASE_Profiles_PATH).document(existingDoc)
                .set(currentLoggedInProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, "Great Succes!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private ProfileImageAdapter.OnGridItemClickListener onGridItemClickListener = new ProfileImageAdapter.OnGridItemClickListener() {
        @Override
        public void onGridItemAddClick(int position) {
            /*
                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(galleryIntent, "Complete action using"), RC_PHOTO_PICKER);
            */
            // Move comment out, since multiple functions use the code in the link
            // Ref : https://www.geeksforgeeks.org/android-how-to-upload-an-image-on-firebase-storage/
            // Multiple options, seems like this is better than the one above
            // Defining Implicit Intent to mobile gallery
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Image from here..."), PICK_IMAGE_REQUEST);
        }

        @Override
        public void onGridItemDeleteClick(int position) {
            progressDialog.setTitle("Deleting image...");
            progressDialog.show();

            String imageUrl = imgUrls.get(position);

            profileService.deletePhoto(imageUrl, userProfileOperationsListener);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // To revert the add/delete buttons
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_CANCELED){
            adapter.setImgUrls(imgUrls);
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // Get the Uri of data
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog  = new ProgressDialog(ProfileActivity.this);
            progressDialog.setTitle("Uploading image...");
            progressDialog.show();

            Uri filePath = data.getData();
            StorageReference picStorageRef = storage.getReference().child(pathToUserPics + UUID.randomUUID().toString());

            picStorageRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ProfileActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    picStorageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            imgUrls.add(task.getResult().toString());
                            adapter.setImgUrls(imgUrls);
                            progressDialog.dismiss();
                        }
                    });
                }
            });
        }
    }
}
