package com.example.user.divein;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MyDivesActivity extends AppCompatActivity {

    private DatabaseReference mDivesDatabase;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseUser mCurrentUser;

    private ArrayList<Dive> mDivesList = new ArrayList<>();

    private RecyclerView mDivesRV;
    private DiveAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dives);

        loadToolBar();

        if (mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser();
        }

        mDivesDatabase = FirebaseDatabase.getInstance().getReference().child("DiveLogs");

        mDivesDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Dive dive = new Dive();

                    dive.setSite_name(snapshot.child("site_name").getValue().toString());
                    dive.setDescription(snapshot.child("description").getValue().toString());
                    dive.setBottom_type(snapshot.child("bottom_type").getValue().toString());
                    dive.setWater_type(snapshot.child("water_type").getValue().toString());
                    dive.setSalinity_type(snapshot.child("salinity_type").getValue().toString());
                    dive.setMax_depth(snapshot.child("max_depth").getValue().toString());

                    dive.setId(snapshot.getKey());

                    mDivesList.add(dive);
                }

                createDiveList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadToolBar() {
        Toolbar toolBar;
        toolBar = (Toolbar) findViewById(R.id.my_dives_toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void createDiveList(){

        mDivesRV = (RecyclerView) findViewById(R.id.my_dives_list);
        mDivesRV.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mDivesRV.setLayoutManager(layoutManager);

        mAdapter = new DiveAdapter(mDivesList, mCurrentUser, MyDivesActivity.this);
        mDivesRV.setAdapter(mAdapter);

        mDivesRV.addItemDecoration(new SimpleDividerItemDecoration(this));
    }
}
