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
import com.karbyshev.droptobasket.ui.IOnItemClickListener;
import com.karbyshev.droptobasket.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private List<Item> itemList = new ArrayList<>();
    private IOnItemClickListener mListener;
    private Random mRandom = new Random();

    public MainAdapter() {
        setHasStableIds(true);
    }

    public void setOnItemClickListener(IOnItemClickListener mListener) {
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
        Item item = itemList.get(position);

        //How do not change color every time when the adapter has changed
        holder.mRelativeLayout.setBackgroundColor(getRandomColor());
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

    public void addProduct(List<Item> list){
        itemList = list;
        notifyDataSetChanged();
    }

    public void clearProduct(){
        itemList.clear();
        notifyDataSetChanged();
    }

    public List<Item> getItemList() {
        return itemList;
    }

    private int getRandomColor(){

        int color = Color.argb(255,
                mRandom.nextInt(256),
                mRandom.nextInt(256),
                mRandom.nextInt(256));

        return color;
    }
}
