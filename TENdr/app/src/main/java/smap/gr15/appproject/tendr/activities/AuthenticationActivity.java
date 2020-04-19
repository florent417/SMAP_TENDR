package smap.gr15.appproject.tendr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.Globals;

// INSPIRED FROM BRIANS VIDEO ABOUT AUTHENTICATION & https://www.youtube.com/watch?v=7Yc3Pt37coM & Firebase docs
public class AuthenticationActivity extends AppCompatActivity {

    private static int RC_SIGN_IN = 1001;
    private static final String TAG_ERROR = "Firebase Error!";
    private static final String TAG_SUCCESS = "Firebase Success!";
    private static final String TAG_GENERAL_ERROR = "ERROR!?";
    private static final String TAG_CREATING_PROFILE_SUCCESS = "Successfully created profile!";
    private static final String TAG_CREATING_PROFILE_FAILURE = "Failed to create profile! :(";
    private FirebaseAuth Auth;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @BindView(R.id.LoginAuthenticationButton)
    Button LoginButton;

    @BindView(R.id.EmailAuthenticationEditText)
    EditText Email;

    @BindView(R.id.PasswordAuthenticationEditText)
    EditText Password;

    @BindView(R.id.AgeAuthenticationEditText)
    EditText Age;

    @BindView(R.id.SexAuthenticationSpinner)
    Spinner Sex;

    @BindView(R.id.FirstnameAuthenticationEditText)
    EditText Firstname;

    @BindView(R.id.OccupationAuthenticationEditText)
    EditText Occupation;

    @BindView(R.id.CityAuthenticationEditText)
    EditText City;

    @BindView(R.id.CountryAuthenticationEditText)
    EditText Country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        ButterKnife.bind(this);

        Auth = FirebaseAuth.getInstance();

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerProfile();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        checkForLoggedInUser();
    }

    private void registerProfile()
    {
        final String EmailFinal = Email.getText().toString().trim();
        final String PasswordFinal = Password.getText().toString().trim();
        final String AgeFinal = Age.getText().toString().trim();
        final String SexFinal = Sex.getSelectedItem().toString().trim();
        final String FirstnameFinal = Firstname.getText().toString().trim();
        final String OccupationFinal = Occupation.getText().toString().trim();
        final String CityFinal = City.getText().toString().trim();
        final String CountryFinal = Country.getText().toString().trim();

        if(EmailFinal.isEmpty())
        {
            Email.setError(TAG_GENERAL_ERROR);
            Email.requestFocus();
            return;
        }

        if(PasswordFinal.isEmpty())
        {
            Password.setError(TAG_GENERAL_ERROR);
            Password.requestFocus();
            return;
        }

        //To avoid yung creeps coming into our app or really really really old people
        if(AgeFinal.isEmpty() || Integer.parseInt(AgeFinal) <= 17 || Integer.parseInt(AgeFinal) >= 110)
        {
            Age.setError(TAG_GENERAL_ERROR);
            Age.requestFocus();
            return;
        }

        if(FirstnameFinal.isEmpty())
        {
            Firstname.setError(TAG_GENERAL_ERROR);
            Firstname.requestFocus();
            return;
        }

        if(OccupationFinal.isEmpty())
        {
            Occupation.setError(TAG_GENERAL_ERROR);
            Occupation.requestFocus();
            return;
        }

        if(CityFinal.isEmpty())
        {
            City.setError(TAG_GENERAL_ERROR);
            City.requestFocus();
            return;
        }

        if(CountryFinal.isEmpty())
        {
            Country.setError(TAG_GENERAL_ERROR);
            Country.requestFocus();
            return;
        }

        Auth.createUserWithEmailAndPassword(EmailFinal, PasswordFinal)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Profile profile = new Profile(
                                    FirstnameFinal,
                                    Integer.parseInt(AgeFinal),
                                    OccupationFinal,
                                    CityFinal,
                                    CountryFinal,
                                    SexFinal,
                                    EmailFinal,
                                    PasswordFinal
                            );

                            
                            firestore.collection(Globals.FIREBASE_Profiles_PATH).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AuthenticationActivity.this, TAG_CREATING_PROFILE_SUCCESS, Toast.LENGTH_LONG).show();

                                    redirectToMainActivity();
                                }
                            });


                        }else {
                            Toast.makeText(AuthenticationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }

    //Redirect to main Activity after successful Login
    private void redirectToMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    //Check for Logged In User
    private void checkForLoggedInUser()
    {
        try {
            if(!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty())
            {
                Log.d("NOTEMPTY", "USER");
                redirectToMainActivity();
            }
        }catch (Exception e)
        {
            Log.d("EMPTY", "USER");
            e.printStackTrace();
        }
    }


}
