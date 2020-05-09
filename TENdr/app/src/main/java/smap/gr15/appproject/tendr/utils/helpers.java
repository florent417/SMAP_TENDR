package smap.gr15.appproject.tendr.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import smap.gr15.appproject.tendr.R;
import smap.gr15.appproject.tendr.activities.MainActivity;
import smap.gr15.appproject.tendr.activities.ProfileActivity;
import smap.gr15.appproject.tendr.activities.SettingsActivity;
import smap.gr15.appproject.tendr.models.Profile;

import static android.content.Context.ACTIVITY_SERVICE;

public class helpers {

    private static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static Profile profile;

    // Used to decide who to prefer in the start of the app, so if you're a woman, you would as standard prefer man
    public static String setGenderOpposite(String gender)
    {
        switch (gender){
            case "Female":
                return "Male";
            case "Male":
                return "Female";
        }

        //This should never get reached
        return "Female";
    }

    public static void setupCustomActionBar(ImageButton settings, ImageButton main, ImageButton profile, Context context)
    {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass() != SettingsActivity.class)
                {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            }
        });

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass() != MainActivity.class)
                {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.getClass() != ProfileActivity.class)
                {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            }
        });
    }

    public static Profile getProfile(String userUid)
    {
        profile = new Profile();

        DocumentReference documentReference = firestore.collection(Globals.FIREBASE_Profiles_PATH).document(userUid);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if(task.isSuccessful())
                {
                    profile = documentSnapshot.toObject(Profile.class);
                }
            }
        });

        return profile;
    }


}
