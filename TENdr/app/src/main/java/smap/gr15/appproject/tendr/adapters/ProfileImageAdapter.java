package smap.gr15.appproject.tendr.adapters;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.style.LineHeightSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import smap.gr15.appproject.tendr.R;

public class ProfileImageAdapter extends BaseAdapter {
    private Context context;
    private List<String> imgUrls = new ArrayList<>();
    private final int MAX_AMOUNT_PICS = 4;
    private static final String TAG = "ProfileImageAdapter";

    private Drawable defaultImagePic = null;
    private Drawable addDrawable = null;
    private Drawable deleteDrawable = null;
    private boolean imgIsDefault = true;

    private OnGridItemClickListener onGridItemClickListener;
    public interface OnGridItemClickListener {
        void onGridItemAddClick(int position);
        void onGridItemDeleteClick(int position);
    }
    public void setOnGridItemClickListener(OnGridItemClickListener gridItemClickListener){
        onGridItemClickListener = gridItemClickListener;
    }

    public ProfileImageAdapter(Context context, List<String> imgUrls){
        this.context = context;
        this.imgUrls = imgUrls;
        defaultImagePic = ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_menu_gallery,context.getTheme());
        addDrawable = ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_input_add, context.getTheme());
        deleteDrawable = ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_delete, context.getTheme());
    }

    public void setImgUrls(List<String> imgUrls){
        this.imgUrls = imgUrls;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return MAX_AMOUNT_PICS;
    }

    @Override
    public Object getItem(int position) {
        return imgUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_image, null);
        }

        ImageView imageView;
        imageView = (ImageView) convertView.findViewById(R.id.ProfileImageImageView);

        String imgUrl = null;
        try{
            imgUrl = imgUrls.get(position);
        } catch (IndexOutOfBoundsException e){
            Log.d(TAG, e.getMessage());
        }

        Log.d(TAG, "getView: " + imgUrl);

        FloatingActionButton floatingActionButton = convertView.findViewById(R.id.ProfileImageFloatingActionButton);
        if (imgUrl != null){
            Picasso.get().load(imgUrl).placeholder(defaultImagePic).into(imageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            // TODO: check if img urls are at maximum size = 4

                            // Set FloatingActionButton functionality and layout based on a picture is already added or not
                            Drawable imgDrawable = imageView.getDrawable();

                            imgIsDefault = defaultImagePic == imgDrawable;
                            Log.d(TAG, "onSuccess: " + imgIsDefault);
                            if (!imgIsDefault){
                                // other option ic_menu_close_clear_cancel
                                floatingActionButton.setImageDrawable(deleteDrawable);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            imgIsDefault = true;
                            // other option ic_menu_close_clear_cancel
                            floatingActionButton.setImageDrawable(deleteDrawable);
                        }
                    });
        }


        floatingActionButton.setOnClickListener(v -> {
            // If default pic is set, you can add a picture, if not you can only delete
            if (onGridItemClickListener != null){
                if (imgIsDefault) {
                    floatingActionButton.setImageDrawable(deleteDrawable);
                    imgIsDefault = false;
                    onGridItemClickListener.onGridItemAddClick(position);
                } else{
                    floatingActionButton.setImageDrawable(addDrawable);
                    imageView.setImageDrawable(defaultImagePic);
                    imgIsDefault = true;
                    onGridItemClickListener.onGridItemDeleteClick(position);
                    // TODO: b4 or after after functionality?
                }
            }
        });


        return convertView;
    }
}
