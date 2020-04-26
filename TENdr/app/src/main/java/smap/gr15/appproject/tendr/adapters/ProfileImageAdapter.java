package smap.gr15.appproject.tendr.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
        defaultImagePic = ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_menu_gallery, context.getTheme());
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

        if(imgUrl != null)
            Picasso.get().load(imgUrls.get(position)).into(imageView);

        // TODO: check if img urls are at maximum size = 4

        // Set FloatingActionButton functionality and layout based on a picture is already added or not
        FloatingActionButton floatingActionButton = convertView.findViewById(R.id.ProfileImageFloatingActionButton);

        boolean imageIsDefault = defaultImagePic == imageView.getDrawable();

        if (!imageIsDefault){
            // other option ic_menu_close_clear_cancel
            floatingActionButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_delete, context.getTheme()));
        }

        floatingActionButton.setOnClickListener(v -> {
            // If default pic is set, you can add a picture, if not you can only delete
            if (onGridItemClickListener != null){
                if (imageIsDefault) {
                    onGridItemClickListener.onGridItemAddClick(position);
                } else{
                    onGridItemClickListener.onGridItemDeleteClick(position);
                    // TODO: b4 or after after functionality?
                    floatingActionButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_input_add, context.getTheme()));
                    imageView.setImageDrawable(defaultImagePic);
                }
            }
        });

        return convertView;
    }
}
