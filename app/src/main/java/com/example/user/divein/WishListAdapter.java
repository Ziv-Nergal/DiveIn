package com.example.user.divein;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.WishListViewHolder> {

    private Context mContext;

    private ArrayList<WishList> mWishList;

    private DatabaseReference mWishListDatabase;
    private FirebaseUser mCurrentUser;

    public static class WishListViewHolder extends RecyclerView.ViewHolder{

        View mView;

        WishListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView titleTV = (TextView) mView.findViewById(R.id.wish_list_cell_title);
            titleTV.setText(title);
        }

        public void setAddress(String address) {
            TextView addressTV = mView.findViewById(R.id.wish_list_cell_address);
            addressTV.setText(address);
        }
    }

    WishListAdapter(ArrayList<WishList> mWishList, FirebaseUser mCurrentUser, Context context) {
        this.mWishList = mWishList;
        this.mCurrentUser = mCurrentUser;
        this.mContext = context;
    }

    @NonNull
    @Override
    public WishListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wish_list_cell, parent, false);

        return new WishListAdapter.WishListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishListViewHolder holder, final int position) {

        holder.setTitle(mWishList.get(position).getmTitle());
        holder.setAddress(mWishList.get(position).getmAddress());

        ImageButton deleteItem = (ImageButton) holder.mView.findViewById(R.id.wish_list_cell_delete_btn);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Are you sure you want to delete this item?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                mWishListDatabase = FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("Wishlists")
                                        .child(mCurrentUser.getUid());

                                mWishListDatabase.child(mWishList.get(position).getmTitle()).removeValue();

                                mWishList.remove(position);
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWishList.size();
    }
}
