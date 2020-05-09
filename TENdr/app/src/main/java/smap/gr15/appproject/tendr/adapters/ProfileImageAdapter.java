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
    private static final String TAG = "ProfileImageAdapter";
    private Context context;
    private List<String> imgUrls = new ArrayList<>();
    private final int MAX_AMOUNT_PICS = 4;

    private Drawable defaultImagePic = null;
    // Change drawable on FloatingActionButton depending on the image on the imageView
    private Drawable addDrawable = null;
    private Drawable deleteDrawable = null;

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

        ImageView imageView = (ImageView) convertView.findViewById(R.id.ProfileImageImageView);

        String imgUrl = null;
        try{
            imgUrl = imgUrls.get(position);
        } catch (IndexOutOfBoundsException e){
            Log.d(TAG, e.getMessage());
        }

        // Why is it called 3 times when position is 0?
        Log.d(TAG, "getView: " + position);

        FloatingActionButton floatingActionButton = convertView.findViewById(R.id.ProfileImageFloatingActionButton);

        if (imgUrl == null){
            floatingActionButton.setImageDrawable(addDrawable);
            imageView.setImageDrawable(defaultImagePic);
        }else {
            floatingActionButton.setImageDrawable(deleteDrawable);
        }

        Picasso.get().load(imgUrl).placeholder(defaultImagePic).error(defaultImagePic).into(imageView);

        floatingActionButton.setOnClickListener(v -> {
            // If default pic is set, you can add a picture, if not you can only delete
            if (onGridItemClickListener != null){
                String imageUrl = null;

                if (imgUrls.size() != 0 && imgUrls.size() > position){
                    imageUrl = imgUrls.get(position);
                }

                if (imageUrl == null) {
                    onGridItemClickListener.onGridItemAddClick(position);
                } else{
                    onGridItemClickListener.onGridItemDeleteClick(position);
                    imageView.setImageDrawable(defaultImagePic);
                }
            }
        });

        return convertView;
    }
}