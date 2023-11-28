package com.example.hypermile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class LoginFragment extends android.app.Fragment {
    View view;
    EditText usernameField;
    EditText passwordField;
    Button loginButton;
    TextView signupLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        loginButton = (Button) view.findViewById(R.id.loginBtn);
        usernameField = (EditText) view.findViewById(R.id.username);
        passwordField = (EditText) view.findViewById(R.id.password);

        signupLink = (TextView) view.findViewById(R.id.signUpLink);
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();
                authenticationActivity.loadFragment(new SignupFragment());
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();
                authenticationActivity.loginUser(username,password);
            }
        });

        return view;
    }
}