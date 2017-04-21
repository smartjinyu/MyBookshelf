package com.smartjinyu.mybookshelf.base.rv;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.smartjinyu.mybookshelf.adapter.BookMultiClickAdapter;

import java.util.List;

/**
 * 作者：Neil on 2017/4/18 22:16.
 * 邮箱：cn.neillee@gmail.com
 * 多选、长按、单击
 */

public abstract class BaseMultiClickAdapter<T, VH extends BaseViewHolder>
        extends RecyclerView.Adapter<VH>
        implements View.OnClickListener, View.OnLongClickListener {

    protected List<T> mData;
    protected Context mContext;

    // 监听事件
    protected BookMultiClickAdapter.RecyclerViewOnItemClickListener onItemClickListener;
    protected BookMultiClickAdapter.RecyclerViewOnItemLongClickListener onItemLongClickListener;

    // generally constructor with these two variables
    public BaseMultiClickAdapter(List<T> data, Context context) {
        mData = data;
        mContext = context;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        bindBaseVH(holder, position, mData.get(position));
        // setting listener, tag
        holder.itemView.setOnClickListener(this);
        holder.itemView.setOnLongClickListener(this);
        // tag can pass some variables with view
        holder.itemView.setTag(holder);
        holder.setItemPosition(position);
    }

    protected abstract void bindBaseVH(VH holder, int position, T t);

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener == null) return;
        onItemClickListener.onItemClick((BaseViewHolder) v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        return onItemClickListener != null && onItemLongClickListener
                .onItemLongClick((BaseViewHolder) v.getTag());
    }

    public void setOnItemClickListener(BookMultiClickAdapter.RecyclerViewOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(BookMultiClickAdapter.RecyclerViewOnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public interface RecyclerViewOnItemClickListener {
        void onItemClick(BaseViewHolder holder);
    }

    public interface RecyclerViewOnItemLongClickListener {
        boolean onItemLongClick(BaseViewHolder holder);
    }
}
