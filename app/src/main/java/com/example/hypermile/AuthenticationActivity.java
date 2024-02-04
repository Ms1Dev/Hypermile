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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Handles logging in and registering users
 */
public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    TextView loginLink;
    TextView signupLink;

    String errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        firebaseAuth = FirebaseAuth.getInstance();
        loadFragment(new LoginFragment());
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
        }
    }

    protected void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.authLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }


    public String registerUser(String email, String password, AuthRequester authRequester) {
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
                        authRequester.authError(translateException(task.getException()));
                    }
                }
            });
        return errorMessage;
    }

    public String loginUser(String email, String password, AuthRequester authRequester) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startMainActivity();
                    }
                    else {
                        authRequester.authError(translateException(task.getException()));
                    }
                }
            });
        return errorMessage;
    }

    public void startMainActivity() {
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Creates a meaningful error message to present to the user if there is a problem logging in
     * @param exception
     * @return
     */
    private String translateException(Exception exception) {
        try {
            throw exception;
        }
        catch (FirebaseAuthException ex) {
            return ex.getMessage();
        }
        catch (Exception ex) {
            if (Objects.requireNonNull(ex.getMessage()).contains("INVALID_LOGIN_CREDENTIALS")) {
                return "Invalid username or password.";
            }
            else {
                return ex.getMessage();
            }
        }
    }
}