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
import com.karbyshev.droptobasket.activities.IOnDroppedItemClickListener;
import com.karbyshev.droptobasket.model.DroppedItem;

import java.util.ArrayList;
import java.util.List;

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

        holder.mRelativeLayout.setBackgroundColor(Color.GRAY);
        holder.mTextView.setText(item.getProductName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

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
                    if (mListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){

                            mListener.OnItemClick(position, itemList);
                        }
                    }
                }
            });
        }
    }

    public void addDropped(List<DroppedItem> items){
        itemList = items;
        notifyDataSetChanged();
    }
}
