package com.example.hypermile;

import android.os.Bundle;

import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends Fragment implements AuthRequester {
    View view;
    EditText usernameField;
    EditText passwordField;
    TextView loginLink;
    Button signupButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        signupButton = (Button) view.findViewById(R.id.signupBtn);
        usernameField = (EditText) view.findViewById(R.id.username);
        passwordField = (EditText) view.findViewById(R.id.password);
        loginLink = (TextView) view.findViewById(R.id.loginLink);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();
                authenticationActivity.loadFragment(new LoginFragment());
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();

                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    showErrorMessage(authenticationActivity.registerUser(username,password, SignupFragment.this));
                }
                else {
                    showErrorMessage("Username and password cannot be empty");
                }
            }
        });

        return view;
    }

    private void showErrorMessage(String message) {
        if (message != null && !message.isEmpty()) {
            LinearLayout errorMessage = view.findViewById(R.id.errorMessage);
            TextView messageContent = view.findViewById(R.id.errorMessageContent);
            errorMessage.setVisibility(View.VISIBLE);
            messageContent.setText(message);
        }
    }

    @Override
    public void authError(String errorMessage) {
        showErrorMessage(errorMessage);
    }
}