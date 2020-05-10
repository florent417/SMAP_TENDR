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
import com.google.firebase.auth.FirebaseAuth;
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
    private static final String TAG_DOWNLOAD_URL_FAILED = "Something went wrong, could not download photo";
    private static final String TAG_UPLOAD_IMAGE_FAILED = "Something went wrong, could not upload photo";
    private static final String TAG_GET_USER_FAILED = "Could not get user data, check internet connection";
    private static final String TAG_PHOTO_DELETION_FAILED = "Unable to delete photo. Check internet connection";
    private static final String TAG_PROFILE_DATA_SAVED = "Profile updated!";
    private static final String TAG_PROFILE_DATA_SAVE_FAILED = "Unable to update profile. Check internet connection";
    private String currentLoggedInUserId = null;

    private final IBinder binder = new ProfileServiceBinder();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Maybe not needed
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public ProfileService(){
        currentLoggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

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

    public void getUserProfile(UserProfileOperationsListener listener){
        DocumentReference docRef = db.collection(Globals.FIREBASE_Profiles_PATH).document(currentLoggedInUserId);
        Task<DocumentSnapshot> documentSnapshotTask = docRef.get();

        documentSnapshotTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Profile userProfile = documentSnapshot.toObject(Profile.class);
                    listener.onGetProfileSuccess(userProfile);
                } else {
                    Log.d(TAG, "Failed to get profile: " + task.getException());
                    listener.onOperationFailedMessage(TAG_GET_USER_FAILED);
                }
            }
        });
    }

    public void editUserProfile(Profile userProfile, UserProfileOperationsListener listener){
        db.collection(Globals.FIREBASE_Profiles_PATH)
                .document(currentLoggedInUserId).set(userProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            listener.onProfileDataSaved(TAG_PROFILE_DATA_SAVED);
                        } else {
                            listener.onOperationFailedMessage(TAG_PROFILE_DATA_SAVE_FAILED);
                        }
                    }
                });
    }

    // reference : https://firebase.google.com/docs/storage/android/upload-files#get_a_download_url
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
                if(task.isSuccessful()){
                    String imageDownloadUrl = null;
                    try {
                        imageDownloadUrl = task.getResult().toString();
                    } catch (RuntimeException e){
                        Log.d(TAG, e.toString());
                    }
                    // TODO: What to do if imageurl is null?
                    listener.onUploadPhotoSuccess(imageDownloadUrl);
                }
                else {
                    Log.d(TAG, "Download img url failed: " + task.getException());
                    listener.onOperationFailedMessage(TAG_UPLOAD_IMAGE_FAILED);
                }
            }
        });
    }

    public void deletePhoto(String imageUrl, UserProfileOperationsListener listener){
        StorageReference deletePictureStorageRef = storage.getReferenceFromUrl(imageUrl);

        deletePictureStorageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    listener.onDeletePhotoSuccess(imageUrl);
                } else {
                    Log.d(TAG, "Deletion of photo failed: " + task.getException());
                    listener.onOperationFailedMessage(TAG_PHOTO_DELETION_FAILED);
                }
            }
        });
    }

    public interface UserProfileOperationsListener{
        void onGetProfileSuccess(Profile userProfile);
        void onUploadPhotoSuccess(String imageUrl);
        void onDeletePhotoSuccess(String imageUrl);
        void onProfileDataSaved(String message);
        void onOperationFailedMessage(String messageToShow);
        // add message for all successful operations?
        // is it necessary to have a callback for editUserProfile?
    }
}
