package br.com.daniel.marques;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;



import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {


    private FirebaseAuth firebaseAuth;
    SharedPreferences sharedPreferences;
    public static String EMAIL="email";
    public static String PASSWORD="pass";

    private EditText mEmailView;
    private EditText mPasswordView;
    FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getPreferences(MODE_PRIVATE);
        firebaseAuth=FirebaseAuth.getInstance();
        if(sharedPreferences.contains(EMAIL)){

        }

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =firebaseAuth.getCurrentUser();
                if(user!=null){
                    finish();
                    Intent i= new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);

                }
            }
        };


        setContentView(R.layout.activity_login);
        // Set up the login form.

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }


        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void attemptRegister() {
        if(!isEmailValid(mEmailView.getText().toString())){
            return;
        }

        if(!isPasswordValid(mPasswordView.getText().toString())){
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(mEmailView.getText().toString(),mPasswordView.getText().toString());

    }


    private void attemptLogin() {
        if(!isEmailValid(mEmailView.getText().toString())){

            return;
        }


        if(!isPasswordValid(mPasswordView.getText().toString())){

            return;
        }

        firebaseAuth.signInWithEmailAndPassword(mEmailView.getText().toString(),mPasswordView.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),R.string.erro_login,Toast.LENGTH_LONG).show();

                }
            }
        });




    }

    private boolean isEmailValid(String email) {
        return email.matches(".+@.+\\..+");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }








}

