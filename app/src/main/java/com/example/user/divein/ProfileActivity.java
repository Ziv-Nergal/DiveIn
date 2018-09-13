package com.example.user.divein;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.user.divein.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, ValueEventListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mCurrentUser;

    private ImageButton mUserNameEditBtn;
    private ImageButton mFirstNameEditBtn;
    private ImageButton mLastNameEditBtn;
    private ImageButton mDateOfBirthEditBtn;
    private ImageButton mDiveAssociationEditBtn;
    private ImageButton mLicenceTypeEditBtn;
    private ImageButton mLicenceIdEditBtn;

    private TextView mUserNameTV;
    private TextView mEmailTV;
    private TextView mFirstNameTV;
    private TextView mLastNameTV;
    private TextView mBirthdayTV;
    private TextView mDiveAssociationTV;
    private TextView mLicenceTypeTV;
    private TextView mLicenceIdTV;

    private CircleImageView mProfilePic;

    private DatabaseReference mUserDatabase;
    private StorageReference mImageStorage;

    private String mCurrentUserId;

    private ProgressBar mProgressBar;
    private LinearLayout mProfileContentLayout;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProgressBar = (ProgressBar) findViewById(R.id.profile_progress_bar);
        mProfileContentLayout = (LinearLayout) findViewById(R.id.profile_content_layout);
        mProfileContentLayout.setVisibility(View.INVISIBLE);

        if(mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser();
        }

        loadToolBar();
        initializeActivityContent();

        mCurrentUserId = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mUserDatabase.addValueEventListener(this);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                mBirthdayTV.setText(day + "/" + (month + 1) + "/" + year);

                mUserDatabase.child("birthday").setValue(mBirthdayTV.getText().toString());
            }
        };
    }


    private void loadToolBar() {
        Toolbar toolBar;
        toolBar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeActivityContent() {

        mUserNameEditBtn = (ImageButton) findViewById(R.id.profile_user_name_edit_btn);
        mFirstNameEditBtn = (ImageButton) findViewById(R.id.profile_first_name_edit_btn);
        mLastNameEditBtn = (ImageButton) findViewById(R.id.profile_last_name_edit_btn);
        mDateOfBirthEditBtn = (ImageButton) findViewById(R.id.profile_birthday_edit_btn);
        mDiveAssociationEditBtn = (ImageButton) findViewById(R.id.profile_dive_association_edit_btn);
        mLicenceTypeEditBtn = (ImageButton) findViewById(R.id.profile_licence_type_edit_btn);
        mLicenceIdEditBtn = (ImageButton) findViewById(R.id.profile_licence_id_edit_btn);

        mUserNameTV = (TextView) findViewById(R.id.profile_user_name_text_view);
        mEmailTV = (TextView) findViewById(R.id.profile_email_text_view);
        mFirstNameTV = (TextView) findViewById(R.id.profile_first_name_text_view);
        mLastNameTV = (TextView) findViewById(R.id.profile_last_name_text_view);
        mBirthdayTV = (TextView) findViewById(R.id.profile_birthday_text_view);
        mDiveAssociationTV = (TextView) findViewById(R.id.profile_dive_association_text_view);
        mLicenceTypeTV = (TextView) findViewById(R.id.profile_licence_type_text_view);
        mLicenceIdTV = (TextView) findViewById(R.id.profile_licence_id_text_view);

        mProfilePic = (CircleImageView) findViewById(R.id.profile_pic);

        mUserNameEditBtn.setOnClickListener(this);
        mFirstNameEditBtn.setOnClickListener(this);
        mLastNameEditBtn.setOnClickListener(this);
        mLicenceIdEditBtn.setOnClickListener(this);
    }

    public void ChangePicBtnClickListener(View view) {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1)
                .start(ProfileActivity.this);
    }

    public void BirthdayBtnClickListener(View view) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(ProfileActivity.this,
                        android.R.style.Theme_DeviceDefault_Dialog, mDateSetListener, year, month, day);

        datePickerDialog.show();
    }

    public void DiveAssociationBtnClickListener(View view) {

        final String[] values = getResources().getStringArray(R.array.associations_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.dive_association).setItems(values, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDiveAssociationTV.setText(values[i]);
                mUserDatabase.child("dive_association").setValue(values[i]);
            }
        }).show();
    }

    public void LicenceTypeBtnClickListener(View view) {

        final String[] values;

        if(mDiveAssociationTV.getText().toString().equals("PADI")) {
            values = getResources().getStringArray(R.array.padi_licence_type);
        }else{
            values = getResources().getStringArray(R.array.ssi_licence_type);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.licence_type).setItems(values, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mLicenceTypeTV.setText(values[i]);
                mUserDatabase.child("licence_type").setValue(values[i]);
            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                try
                {
                    File fileImagePath = new File(resultUri.getPath());

                    Bitmap thumbNailBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(30)
                            .compressToBitmap(fileImagePath);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    thumbNailBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] thumbByteArray = byteArrayOutputStream.toByteArray();

                    uploadImageToDataBase(thumbByteArray);
                    mProfilePic.setImageURI(resultUri);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadImageToDataBase(byte[] thumbByteArray)
    {
        final StorageReference imagePath = mImageStorage.child("profile_images").child(mCurrentUserId + ".jpg");

        UploadTask uploadTask = imagePath.putBytes(thumbByteArray);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    mImageStorage
                            .child("profile_images")
                            .child(mCurrentUserId + ".jpg")
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mCurrentUser.updateProfile(new UserProfileChangeRequest
                                            .Builder()
                                            .setPhotoUri(Uri.parse(uri.toString()))
                                            .build());


                                }
                            });
                }
            }
        });
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        mUserNameTV.setText(mCurrentUser.getDisplayName());
        mEmailTV.setText(mCurrentUser.getEmail());

        mFirstNameTV.setText(dataSnapshot.child("first_name").getValue(String.class));
        mLastNameTV.setText(dataSnapshot.child("last_name").getValue(String.class));

        if(dataSnapshot.hasChild("birthday")){
            mBirthdayTV.setText(dataSnapshot.child("birthday").getValue(String.class));
        }
        if(dataSnapshot.hasChild("dive_association")){
            mDiveAssociationTV.setText(dataSnapshot.child("dive_association").getValue(String.class));
        }
        if(dataSnapshot.hasChild("licence_type")){
            mLicenceTypeTV.setText(dataSnapshot.child("licence_type").getValue(String.class));
        }
        if(dataSnapshot.hasChild("licence_id")){
            mLicenceIdTV.setText(dataSnapshot.child("licence_id").getValue(String.class));
        }

        if(mCurrentUser.getPhotoUrl() != null)
        {
            Picasso.get().load(mCurrentUser.getPhotoUrl())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.progress_animation)
                    .into(mProfilePic, new Callback()
                    {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError(Exception e)
                        {
                            Picasso.get().load(mCurrentUser.getPhotoUrl()).placeholder(R.drawable.progress_animation).into(mProfilePic);
                        }
                    });
        }

        if(mDiveAssociationTV.getText().toString().equals(mDiveAssociationEditBtn.getTag())){
            mLicenceTypeEditBtn.setVisibility(View.INVISIBLE);
        }else{
            mLicenceTypeEditBtn.setVisibility(View.VISIBLE);
        }

        if(mLicenceTypeTV.getText().toString().equals(mLicenceTypeEditBtn.getTag())){
            mLicenceIdEditBtn.setVisibility(View.INVISIBLE);
        }else{
            mLicenceIdEditBtn.setVisibility(View.VISIBLE);
        }

        mProgressBar.setVisibility(View.INVISIBLE);
        mProfileContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}

    @Override
    public void onClick(final View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_dialog, null);

        final EditText newInputEditText = (EditText) dialogView.findViewById(R.id.new_input);
        newInputEditText.setHint("Set New " + view.getTag().toString() + "...");

        builder.setView(dialogView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String newValue = newInputEditText.getText().toString();

                if(!newValue.isEmpty()) {

                    switch (view.getTag().toString()) {

                        case "User Name":
                            mProgressBar.setVisibility(View.VISIBLE);

                            mCurrentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newValue).build()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mUserNameTV.setText(mCurrentUser.getDisplayName());
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                            break;

                        case "First Name":
                            mUserDatabase.child("first_name").setValue(newValue);
                            break;

                        case "Last Name":
                            mUserDatabase.child("last_name").setValue(newValue);
                            break;

                        case "Licence ID":
                            mUserDatabase.child("licence_id").setValue(newValue);
                            break;
                    }
                }

                dialogInterface.dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }
}
