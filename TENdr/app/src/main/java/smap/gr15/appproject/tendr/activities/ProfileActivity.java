package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.adapters.ProfileImageAdapter;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.Globals;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String existingDoc = "9JMORa2zRPWqeMEgt3IAsaXLhHC2";
    Profile testProf = null;
    private static int PICK_IMAGE_REQUEST =  2;

    @BindView(R.id.FirstNameProfileEditText)
    EditText firstNameEditTxt;
    @BindView(R.id.AgeProfileEditText)
    EditText ageEditTxt;
    @BindView(R.id.CityProfileEditText)
    EditText cityEditTxt;
    @BindView(R.id.saveBtn)
    Button saveBtn;

    @BindView(R.id.gridViewTest)
    GridView gridView;
    ArrayList<String> imgUrls = new ArrayList<>();

    ProfileImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        testGetPicGrid();

        //testGetPic();
        //testGetProfile();
        //testAddProfile();
    }

    @OnClick(R.id.saveBtn)
    void saveBtn(View view){
        testProf.setFirstName(firstNameEditTxt.getText().toString());
        testProf.setAge(Integer.valueOf(ageEditTxt.getText().toString()));
        testProf.setCity(cityEditTxt.getText().toString());
        testProf.setPictures(imgUrls);
        firestore.collection(Globals.FIREBASE_Profiles_PATH).document(existingDoc)
                .set(testProf).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, "Great Succes!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void testGetPicGrid(){
        String pathToTestPic = "pictures/date-russian-girl-site-review.png";
        StorageReference russianDateRef = storage.getReference(pathToTestPic);
        russianDateRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                String testPath = task.getResult().toString();
                imgUrls.add(testPath);
                // Needs to be activity context and not application context
                adapter = new ProfileImageAdapter(ProfileActivity.this, imgUrls);
                gridView.setAdapter(adapter);
                adapter.setOnGridItemClickListener(onGridItemClickListener);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Got path to russian babes: " + testPath);
            }
        });

    }

    //region Needed for later
    private void testGetProfile(){

        DocumentReference docRef = firestore.collection(Globals.FIREBASE_Profiles_PATH).document(existingDoc);
        Task<DocumentSnapshot> documentSnapshotTask = docRef.get();

        documentSnapshotTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();

                if(documentSnapshot.exists()){
                    testProf = documentSnapshot.toObject(Profile.class);
                    firstNameEditTxt.setText(testProf.getFirstName());
                    ageEditTxt.setText(String.valueOf(testProf.getAge()));
                    cityEditTxt.setText(testProf.getCity());
                }
            }
        });
    }
    //endregion

    private ProfileImageAdapter.OnGridItemClickListener onGridItemClickListener = position -> {
        /*
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galleryIntent, "Complete action using"), RC_PHOTO_PICKER);

         */
        // Ref : https://www.geeksforgeeks.org/android-how-to-upload-an-image-on-firebase-storage/
        // Multiple options, seems like this is better than the one above
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image from here..."), PICK_IMAGE_REQUEST);
    };

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // Get the Uri of data
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            Uri filePath;
            filePath = data.getData();
            Log.d(TAG, filePath.toString());
            String pathPics = "pictures/" + existingDoc + "/";
            StorageReference picStorageRef = storage.getReference().child(pathPics + "testPic");

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

//region Previously used code
/*
private void testAddProfile(){
        List<String> genderprefs = new ArrayList<>();

        genderprefs.add("Man");

        Profile testProf = new Profile("firstName", 22, "occupation", "city", "country", "gender", "email", "password");
        firestore.collection(Globals.FIREBASE_Profiles_PATH).add(testProf).addOnSuccessListener(
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Added " + documentReference.getId());
                    }
                }
        ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
        );
    }

    private void testGetPic(){
        String pathToTestPic = "pictures/date-russian-girl-site-review.png";
        StorageReference russianDateRef = storage.getReference(pathToTestPic);
        russianDateRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                String testPath = task.getResult().toString();
                Log.d(TAG, "Got path to russian babes: " + testPath);
                Picasso.get().load(testPath).into(imgView);
            }
        });

    }
 */
//endregion
