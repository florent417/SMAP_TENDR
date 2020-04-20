package smap.gr15.appproject.tendr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import smap.gr15.appproject.tendr.R;

public class ProfileImageAdapter extends BaseAdapter {

    private Context context;
    private List<String> imgUrls = new ArrayList<>();

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
        Picasso.get().load(imgUrls.get(position)).into(imageView);
        return convertView;
    }
}
