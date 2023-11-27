package com.example.hypermile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AuthenticationActivity extends AppCompatActivity {

//    Button goToSignup;
    TextView loginLink;
    TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
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
}