package com.smartjinyu.mybookshelf.base.rv;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    protected boolean isSelected = false;
    protected int mPosition = -1;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public int getItemPosition() {
        return mPosition;
    }

    public void setItemPosition(int position) {
        mPosition = position;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        resp2View(isSelected);
    }

    protected abstract void resp2View(boolean isSelected);
}

