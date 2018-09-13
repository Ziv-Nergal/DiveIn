package com.example.user.divein;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LogNewDiveActivity extends AppCompatActivity {

    private TextView mSiteTitleTV;

    private Spinner mWaterSpinner;
    private Spinner mBottomSpinner;
    private Spinner mSalinitySpinner;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DatabaseReference mDiveLogsDatabase;

    private boolean mWaterTypeSelected = false;
    private boolean mSalinityTypeSelected = false;
    private boolean mBottomTypeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_new_dive);

        loadToolBar();

        mDiveLogsDatabase = FirebaseDatabase.getInstance().getReference().child("DiveLogs");

        mSiteTitleTV = (TextView) findViewById(R.id.log_dive_title);
        mSiteTitleTV.setText(getIntent().getStringExtra("site name"));

        mWaterSpinner = (Spinner) findViewById(R.id.water_spinner);
        mBottomSpinner = (Spinner) findViewById(R.id.bottom_spinner);
        mSalinitySpinner = (Spinner) findViewById(R.id.salinity_spinner);

        setSpinners();
    }

    private void setSpinners() {

        ArrayAdapter<CharSequence> waterAdapter = ArrayAdapter
                .createFromResource(LogNewDiveActivity.this,
                        R.array.water_type_array,
                        android.R.layout.simple_spinner_item);

        waterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWaterSpinner.setAdapter(waterAdapter);

        mWaterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i != 0){
                    mWaterTypeSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ArrayAdapter<CharSequence> bottomAdapter = ArrayAdapter
                .createFromResource(LogNewDiveActivity.this,
                        R.array.bottom_type_array,
                        android.R.layout.simple_spinner_item);

        bottomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBottomSpinner.setAdapter(bottomAdapter);

        mBottomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i != 0){
                    mBottomTypeSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ArrayAdapter<CharSequence> salinityAdapter = ArrayAdapter
                .createFromResource(LogNewDiveActivity.this,
                        R.array.salinity_type_array,
                        android.R.layout.simple_spinner_item);

        salinityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSalinitySpinner.setAdapter(salinityAdapter);

        mSalinitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i != 0){
                    mSalinityTypeSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void loadToolBar() {
        Toolbar toolBar;
        toolBar = (Toolbar) findViewById(R.id.log_dive_toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void saveDiveBtnClickListener(View view) {

        if(validateDetails()){

            EditText descriptionET = (EditText) findViewById(R.id.log_dive_description_edit_text);
            EditText maxDepthET = (EditText) findViewById(R.id.log_dive_depth_edit_text);

            String descriptionStr = descriptionET.getText().toString();
            String maxDepthStr = maxDepthET.getText().toString();
            String bottomTypeStr = mBottomSpinner.getSelectedItem().toString();
            String salinityTypeStr = mSalinitySpinner.getSelectedItem().toString();
            String waterTypeStr = mWaterSpinner.getSelectedItem().toString();

            DatabaseReference diveLog = mDiveLogsDatabase.child(mAuth.getCurrentUser().getUid()).push();
            String pushId = diveLog.getKey();

            Map diveLogMap = new HashMap();
            diveLogMap.put("site_name", mSiteTitleTV.getText().toString());
            diveLogMap.put("description", descriptionStr);
            diveLogMap.put("max_depth", maxDepthStr);
            diveLogMap.put("bottom_type", bottomTypeStr);
            diveLogMap.put("salinity_type", salinityTypeStr);
            diveLogMap.put("water_type", waterTypeStr);

            diveLog.updateChildren(diveLogMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Toast.makeText(LogNewDiveActivity.this,
                                "There was an error!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(LogNewDiveActivity.this,
                                "Dive logged successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }else{
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateDetails() {

        boolean isGood = true;

        EditText descriptionET = (EditText) findViewById(R.id.log_dive_description_edit_text);
        EditText maxDepthET = (EditText) findViewById(R.id.log_dive_depth_edit_text);

        if(descriptionET.getText().toString().equals("")
                || maxDepthET.getText().toString().equals("")
                || !mWaterTypeSelected
                || !mSalinityTypeSelected
                || !mBottomTypeSelected){
            isGood = false;
        }

        return isGood;
    }
}
