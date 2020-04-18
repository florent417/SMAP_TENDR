package smap.gr15.appproject.tendr.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import smap.gr15.appproject.tendr.R;

// INSPIRED FROM BRIANS VIDEO ABOUT AUTHENTICATION & https://www.youtube.com/watch?v=7Yc3Pt37coM
public class AuthenticationActivity extends AppCompatActivity {

    private static int RC_SIGN_IN = 1;
    private static final String TAG_ERROR = "Firebase Error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Button signInButton = findViewById(R.id.LoginAuthenticationButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build());

                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
            }
        });

        //checkForLoggedInUser();
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
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty())
        {
            redirectToMainActivity();
        }
    }
}
