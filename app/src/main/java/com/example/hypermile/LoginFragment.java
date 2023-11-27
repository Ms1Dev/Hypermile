package com.example.hypermile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class LoginFragment extends android.app.Fragment {
    View view;
    TextView signupLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        signupLink = (TextView) view.findViewById(R.id.signUpLink);
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();
                authenticationActivity.loadFragment(new SignupFragment());
            }
        });

        return view;
    }
}