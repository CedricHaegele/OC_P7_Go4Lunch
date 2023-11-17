package com.example.oc_p7_go4lunch.utils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemClickSupport {
    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private static OnItemLongClickListener mOnItemLongClickListener;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }
    };
    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                return mOnItemLongClickListener.onItemLongClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
            return false;
        }
    };

    private ItemClickSupport(RecyclerView recyclerView, int itemID, OnItemLongClickListener mOnItemLongClickListener) {
        mRecyclerView = recyclerView;
        ItemClickSupport.mOnItemLongClickListener = mOnItemLongClickListener;
        mRecyclerView.setTag(itemID, this);
        RecyclerView.OnChildAttachStateChangeListener mAttachListener = new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (mOnItemClickListener != null) {
                    view.setOnClickListener(mOnClickListener);
                }
                if (ItemClickSupport.mOnItemLongClickListener != null) {
                    view.setOnLongClickListener(mOnLongClickListener);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {

            }
        };
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static ItemClickSupport addTo(RecyclerView view, int itemID) {
        ItemClickSupport support = (ItemClickSupport) view.getTag(itemID);
        if (support == null) {
            support = new ItemClickSupport(view, itemID, mOnItemLongClickListener);
        }
        return support;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {

        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

    public interface OnItemLongClickListener {

        boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
    }
}