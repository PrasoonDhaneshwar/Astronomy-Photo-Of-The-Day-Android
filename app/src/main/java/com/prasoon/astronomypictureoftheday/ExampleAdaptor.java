package com.prasoon.astronomypictureoftheday;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ExampleAdaptor extends RecyclerView.Adapter<ExampleAdaptor.ExampleViewHolder> {
    private Context mContext;
    private ArrayList<ExampleItem> mExampleList;
    private OnItemClickListener mListener;

    // Create an interface to open an image to Detail Activity
    public interface OnItemClickListener {
        void onItemClick(int position); // Call this after adapter has been created, setAdapter()
        void onDeleteItem(int position); // Call this after adapter has been created, setAdapter()
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public ExampleAdaptor(Context context, ArrayList<ExampleItem> exampleList) {
        mContext = context;
        mExampleList = exampleList;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.example_item, parent, false);
        return new ExampleViewHolder(v);
    }

    // Get list of current items here
    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        ExampleItem currentItem = mExampleList.get(position);
        String imageUrl = currentItem.getImageUrl();
        String title = currentItem.getTitle();
        String dateTaken = currentItem.getDate();

        // Set each view here
        holder.mTextViewTitle.setText(title);
        holder.mTextViewDate.setText("Taken on: " + dateTaken);
        Picasso.get().load(imageUrl).fit().centerInside().into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextViewTitle;
        public TextView mTextViewDate;
        public ImageView mDeleteItem;

        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageRecyclerView);
            mTextViewTitle = itemView.findViewById(R.id.textRecyclerViewTitle);
            mTextViewDate = itemView.findViewById(R.id.textRecyclerViewDate);
            mDeleteItem = itemView.findViewById(R.id.deleteItem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        // Get the position in Recycler View List
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });

            mDeleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        // Get the position in Recycler View List
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteItem(position);
                        }
                    }
                }
            });
        }
    }
}
