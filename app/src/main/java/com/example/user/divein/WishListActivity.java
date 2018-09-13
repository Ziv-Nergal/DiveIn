package com.example.user.divein;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WishListActivity extends AppCompatActivity {

    private DatabaseReference mWishListDatabase;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseUser mCurrentUser;

    private ArrayList<WishList> mWishList = new ArrayList<>();

    private RecyclerView mWishListRV;
    private WishListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list);

        loadToolBar();

        if (mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser();
        }

        mWishListDatabase = FirebaseDatabase.getInstance().getReference().child("Wishlists");

        mWishListDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    WishList wishList = new WishList();

                    wishList.setmTitle(snapshot.getKey());
                    wishList.setmAddress(snapshot.getValue().toString());

                    mWishList.add(wishList);
                }

                createWishList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadToolBar() {
        Toolbar toolBar;
        toolBar = (Toolbar) findViewById(R.id.my_wish_list_toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void createWishList(){

        mWishListRV = (RecyclerView) findViewById(R.id.my_wish_list);
        mWishListRV.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mWishListRV.setLayoutManager(layoutManager);

        mAdapter = new WishListAdapter(mWishList, mCurrentUser, WishListActivity.this);
        mWishListRV.setAdapter(mAdapter);
    }
}
