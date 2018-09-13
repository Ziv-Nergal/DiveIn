package com.example.user.divein;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText mUserNameET;
    private EditText mPasswordET;
    private EditText mConfirmPassET;
    private EditText mEmailET;
    private EditText mFirstNameET;
    private EditText mLastNameET;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mUserNameET = (EditText) findViewById(R.id.reg_user_name_edit_text);
        mPasswordET = (EditText) findViewById(R.id.reg_password_edit_text);
        mConfirmPassET = (EditText) findViewById(R.id.reg_conf_pass_edit_text);
        mEmailET = (EditText) findViewById(R.id.reg_email_edit_text);
        mFirstNameET = (EditText) findViewById(R.id.reg_first_name_edit_text);
        mLastNameET = (EditText) findViewById(R.id.reg_last_name_edit_text);
    }

    public void RegisterNewUserBtnCLickListener(View view) {

        final String userNameStr = mUserNameET.getText().toString().trim();
        final String passwordStr = mPasswordET.getText().toString().trim();
        final String confirmPassStr = mConfirmPassET.getText().toString().trim();
        final String emailStr = mEmailET.getText().toString().trim();
        final String firstNameStr = mFirstNameET.getText().toString().trim();
        final String lastNameStr = mLastNameET.getText().toString().trim();

        String isDataValid = validateRegistrationDetails(userNameStr, passwordStr, confirmPassStr,
                emailStr, firstNameStr, lastNameStr);

        if(isDataValid.equals("OK")){
            mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.register_progress_bar);
                            progressBar.setVisibility(View.VISIBLE);

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser currentUser = mAuth.getCurrentUser();

                                if(currentUser != null) {
                                    String uid = currentUser.getUid();
                                    mUsersDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                    currentUser.updateEmail(emailStr);
                                    currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userNameStr).build());

                                    Map userDetailtMap = new HashMap();
                                    userDetailtMap.put("first_name", firstNameStr);
                                    userDetailtMap.put("last_name", lastNameStr);

                                    mUsersDataBase.updateChildren(userDetailtMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(RegisterActivity.this, "Error registering!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                progressBar.setVisibility(View.INVISIBLE);

                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                intent.putExtra("from register", true);

                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

        else{
            Toast.makeText(this, isDataValid, Toast.LENGTH_SHORT).show();
        }
    }

    private String validateRegistrationDetails(String userNameStr, String passwordStr, String confirmPassStr,
                                               String emailStr, String firstNameStr,
                                               String lastNameStr) {
        String isDataValid = "OK";

        if(userNameStr.isEmpty() || passwordStr.isEmpty() || confirmPassStr.isEmpty() || emailStr.isEmpty()
                || firstNameStr.isEmpty() || lastNameStr.isEmpty()) {
            isDataValid = "Please Fill All Details!";
        }
        else if(!passwordStr.equals(confirmPassStr)) {
            isDataValid =  "Passwords Do Not Match!";
        }

        return isDataValid;
    }
}
