package smap.gr15.appproject.tendr.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import smap.gr15.appproject.tendr.models.Profile;
import smap.gr15.appproject.tendr.utils.Globals;

public class ProfileService extends Service {
    private static final String TAG = "ProfileService";
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

    // TODO: Change userid to check on runtime
    public void editUserProfile(String userId, Profile userProfile){
        db.collection(Globals.FIREBASE_Profiles_PATH).document(userId)
                .set(userProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "User profile edited");
            }
        });
    }

    public void uploadPhoto(Uri imagePath, UserProfileOperationsListener listener){
        StorageReference picStorageRef = storage.getReference().child(Globals.FIREBASE_STORAGE_PICTURES_PATH).child(UUID.randomUUID().toString());

        UploadTask uploadPictureTask = picStorageRef.putFile(imagePath);

        Task<Uri> pictureUrlTask = uploadPictureTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }

                return picStorageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                String imageDownloadUrl = null;
                try {
                    imageDownloadUrl = task.getResult().toString();
                } catch (Exception e){
                    Log.d(TAG, e.toString());
                }
                // TODO: What to do if imageurl is null?
                listener.onUploadPhotoSuccess(imageDownloadUrl);
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
        void onUploadPhotoSuccess(String imageUrl);
        void onDeletePhotoSuccess(String imageUrl);
        // Todo: add message listener if soemthing failed
        // Todo: add message for successful operations
        // TODO: is it necessary to have a callback for edituserprofile??
    }
}
