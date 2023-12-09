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
    protected void loadFragment(Fragment fragment) {
// create a FragmentManager
        FragmentManager fragmentManager = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.authLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    public String registerUser(String email, String password) {
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
                        errorMessage = translateException(task.getException());
                    }
                }
            });
        return errorMessage;
    }

    public String loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startMainActivity();
                    }
                    else {
                        errorMessage = translateException(task.getException());
                    }
                }
            });
        return errorMessage;
    }

    public void startMainActivity() {
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
            startActivity(intent);

        }
    }


    private String translateException(Exception exception) {
        try {
            throw exception;
        }
        catch (FirebaseAuthEmailException ex) {
            return "Invalid email";
        }
        catch (FirebaseAuthWeakPasswordException ex) {
            return "Password must be at least 6 characters.";
        }
        catch (FirebaseAuthUserCollisionException ex) {
            return "Username already exists.";
        }
        catch (FirebaseAuthInvalidCredentialsException ex) {
            return "Invalid login credentials.";
        }
        catch (Exception ex) {
            return ex.getMessage();
        }
    }
}