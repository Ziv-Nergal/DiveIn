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

public class DiveAdapter extends RecyclerView.Adapter<DiveAdapter.DiveViewHolder> {

    private ArrayList<Dive> mDivesList;

    private DatabaseReference mDivesDatabase;
    private FirebaseUser mCurrentUser;

    private Context mContext;

    public static class DiveViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DiveViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView titleTV = (TextView) mView.findViewById(R.id.dive_cell_title);
            titleTV.setText(title);
        }

        public void setDescription(String summary) {
            TextView summaryTV = mView.findViewById(R.id.dive_cell_summary);
            summaryTV.setText(summary);
        }

        void setBottomType(String bType) {
            TextView bottomTypeTV = mView.findViewById(R.id.dive_cell_bottom_type);
            bottomTypeTV.setText(bType);
        }

        void setWaterType(String wType) {
            TextView waterTypeTV = mView.findViewById(R.id.dive_cell_water_type);
            waterTypeTV.setText(wType);
        }

        void setSalinityType(String sType) {
            TextView salinityTV = mView.findViewById(R.id.dive_cell_salinity_type);
            salinityTV.setText(sType);
        }

        void setMaxDepth(String maxDepth) {
            TextView maxDepthTV = mView.findViewById(R.id.dive_cell_max_depth);
            String maxDepthStr = maxDepth + " Meters";
            maxDepthTV.setText(maxDepthStr);
        }
    }

    DiveAdapter(ArrayList<Dive> mDivesList, FirebaseUser mCurrentUser, Context context) {
        this.mDivesList = mDivesList;
        this.mCurrentUser = mCurrentUser;
        this.mContext = context;
    }

    @NonNull
    @Override
    public DiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dive_cell, parent, false);

        return new DiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiveViewHolder holder, final int position) {

        holder.setTitle(mDivesList.get(position).site_name);
        holder.setBottomType(mDivesList.get(position).bottom_type);
        holder.setWaterType(mDivesList.get(position).water_type);
        holder.setSalinityType(mDivesList.get(position).salinity_type);
        holder.setMaxDepth(mDivesList.get(position).max_depth);
        holder.setDescription(mDivesList.get(position).description);

        ImageButton deleteBtn = holder.mView.findViewById(R.id.dive_cell_delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Are you sure you want to delete this item?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                mDivesDatabase = FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("DiveLogs")
                                        .child(mCurrentUser.getUid());

                                mDivesDatabase.child(mDivesList.get(position).id).removeValue();

                                mDivesList.remove(position);
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
        return mDivesList.size();
    }
}
