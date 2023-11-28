package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    TextView loginLink;
    TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        firebaseAuth = FirebaseAuth.getInstance();
        loadFragment(new LoginFragment());
    }
    protected void loadFragment(Fragment fragment) {
// create a FragmentManager
        FragmentManager fragmentManager = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.authLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    public void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startMainActivity();
                        Log.d("debug","Workeddd");
                    }
                    else {
                        Log.w("debug","createUserWithEmail:failure", task.getException());
                        // failure message
                    }
                }
            });
    }

    public void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startMainActivity();
                    }
                    else {
                        // failure message
                    }
                }
            });
    }

    public void startMainActivity() {
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
            startActivity(intent);

        }
    }
}