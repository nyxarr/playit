package com.viby.playit.adapters;

import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by nyxar on 18/12/15.
 */
public class LayoutManager extends RecyclerView.LayoutManager {

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return null;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
