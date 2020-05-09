package smap.gr15.appproject.tendr.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;

import smap.gr15.appproject.tendr.R;

public class SwipeCard extends CardView {
    public SwipeCard(Context context) {
        super(context);
        initialize(context);
    }

    public SwipeCard(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize(context);
    }

    private void initialize(Context context){
        LayoutInflater.from(context).inflate(R.layout.fragment_swipe_card, this);
    }
}
