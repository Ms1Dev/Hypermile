package com.example.hypermile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class AuthenticationActivity extends AppCompatActivity {

//    Button goToSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        loadFragment(new LoginFragment());
    }
    private void loadFragment(Fragment fragment) {
// create a FragmentManager
        FragmentManager fragmentManager = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.authLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }
}