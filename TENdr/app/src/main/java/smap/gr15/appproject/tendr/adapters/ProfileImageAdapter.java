package smap.gr15.appproject.tendr.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import smap.gr15.appproject.tendr.R;

public class ProfileImageAdapter extends BaseAdapter {
    private static final String TAG = "ProfileImageAdapter";
    private OnGridItemClickListener onGridItemClickListener;
    public interface OnGridItemClickListener {
        void onGridItemClick(int position);
    }
    public void setOnGridItemClickListener(OnGridItemClickListener gridItemClickListener){
        onGridItemClickListener = gridItemClickListener;
    }

    private Context context;
    private List<String> imgUrls = new ArrayList<>();

    public void setImgUrls(List<String> imgUrls){
        this.imgUrls = imgUrls;
        notifyDataSetChanged();
    }

    public ProfileImageAdapter(Context context, ArrayList<String> imgUrls){
        this.context = context;
        this.imgUrls = imgUrls;
    }
    @Override
    public int getCount() {
        return imgUrls.size();
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
        imageView = (ImageView) convertView.findViewById(R.id.imageViewGrid);

        String imgUrl = null;
        try{
            imgUrl = imgUrls.get(position);
        } catch (IndexOutOfBoundsException e){
            Log.d(TAG, e.getMessage());
        }

        if(imgUrl != null)
            Picasso.get().load(imgUrls.get(position)).into(imageView);

        FloatingActionButton floatingActionButton = convertView.findViewById(R.id.floatingActionButton4);
        floatingActionButton.setOnClickListener(v -> {
            if (onGridItemClickListener != null){
                onGridItemClickListener.onGridItemClick(position);
            }
        });

        return convertView;
    }
}
