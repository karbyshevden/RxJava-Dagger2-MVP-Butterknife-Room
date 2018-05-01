package com.karbyshev.droptobasket.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.karbyshev.droptobasket.R;
import com.karbyshev.droptobasket.ui.IOnDroppedItemClickListener;
import com.karbyshev.droptobasket.model.DroppedItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import android.view.ViewGroup.LayoutParams;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DroppedAdapter extends RecyclerView.Adapter<DroppedAdapter.ViewHolder> {
    List<DroppedItem> itemList = new ArrayList<>();
    private IOnDroppedItemClickListener mListener;

    public void setOnDroppedItemClickListener(IOnDroppedItemClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DroppedItem item = itemList.get(position);

        LayoutParams params = (LayoutParams) holder.mTextView.getLayoutParams();
        params.height = LayoutParams.MATCH_PARENT;
        holder.mTextView.setLayoutParams(params);
        holder.mTextView.setText(item.getProductName());
        if (item.getImage().equals("")) {
            holder.mImageView.setImageResource(R.drawable.ic_local_grocery_store_black_24dp);
        } else {
            Picasso.get().load(item.getImage()).fit().centerCrop().into(holder.mImageView);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.itemImageView)
        ImageView mImageView;
        @BindView(R.id.itemTextView)
        TextView mTextView;
        @BindView(R.id.itemRelativeLayout)
        RelativeLayout mRelativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            mListener.OnItemClick(position, itemList);
                        }
                    }
                }
            });
        }
    }

    public void addDropped(List<DroppedItem> items) {
        itemList = items;
        notifyDataSetChanged();
    }
}
