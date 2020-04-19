package smap.gr15.appproject.tendr.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.Globals;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String existingDoc = "9JMORa2zRPWqeMEgt3IAsaXLhHC2";
    Profile testProf = null;

    @BindView(R.id.FirstNameProfileEditText)
    EditText firstNameEditTxt;
    @BindView(R.id.AgeProfileEditText)
    EditText ageEditTxt;
    @BindView(R.id.CityProfileEditText)
    EditText cityEditTxt;
    @BindView(R.id.saveBtn)
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        testGetProfile();
        //testAddProfile();
    }

    @OnClick(R.id.saveBtn)
    void saveBtn(View view){
        testProf.setFirstName(firstNameEditTxt.getText().toString());
        testProf.setAge(Integer.valueOf(ageEditTxt.getText().toString()));
        testProf.setCity(cityEditTxt.getText().toString());
        firestore.collection(Globals.FIREBASE_Profiles_PATH).document(existingDoc)
                .set(testProf).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, "Great Succes!", Toast.LENGTH_LONG).show();
            }
        });
    }

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
}
