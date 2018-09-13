package com.example.user.divein;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.user.divein.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailET;
    private EditText mPasswordET;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailET = (EditText) findViewById(R.id.reg_user_name_edit_text);
        mPasswordET = (EditText) findViewById(R.id.reg_password_edit_text);

        mAuth = FirebaseAuth.getInstance();
    }

    public void LoginBtnClickListener(View view) {

        String email = mEmailET.getText().toString();
        String password = mPasswordET.getText().toString();

        if(validateLoginDetails(email, password)){

            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.log_in_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else{
                        Toast.makeText(LoginActivity.this, "fuck", Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private boolean validateLoginDetails(String userName, String password) {

        boolean isValid = true;

        if(userName.isEmpty() || password.isEmpty()){
            isValid = false;
        }

        return isValid;
    }

    public void RegisterBtnClickListener(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}
