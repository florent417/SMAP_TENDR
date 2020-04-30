package smap.gr15.appproject.tendr.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.Globals;

public class ProfileService extends Service {
    private final IBinder binder = new ProfileServiceBinder();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Maybe not needed
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    //region Binder Implementaton
    // Ref: https://developer.android.com/guide/components/bound-services
    // Comments also copied from reference
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    // use another name
    public class ProfileServiceBinder extends Binder {
        public ProfileService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ProfileService.this;
        }
    }
    //endregion

    public void getUserProfile(String userId, UserProfileOperationsListener listener){
        DocumentReference docRef = db.collection(Globals.FIREBASE_Profiles_PATH).document(userId);
        Task<DocumentSnapshot> documentSnapshotTask = docRef.get();

        documentSnapshotTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Profile userProfile = documentSnapshot.toObject(Profile.class);
                    listener.onGetProfileSuccess(userProfile);
                }
            }
        });
    }

    public void deletePhoto(String imageUrl, UserProfileOperationsListener listener){
        StorageReference deletePictureStorageRef = storage.getReferenceFromUrl(imageUrl);

        deletePictureStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener.onDeletePhotoSuccess(imageUrl);
            }
        });

        // Todo: add message listener if soemthing failed
    }

    public interface UserProfileOperationsListener{
        void onGetProfileSuccess(Profile userProfile);
        void onDeletePhotoSuccess(String imageUrl);
    }
}
