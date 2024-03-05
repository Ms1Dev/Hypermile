package com.example.hypermile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Shows a simple login form to the user to enter credentials.
 * When the details are submitted they are passed to methods in the AuthenticationActivity
 */
public class LoginFragment extends android.app.Fragment implements AuthRequester {
    private View view;
    private EditText usernameField;
    private EditText passwordField;
    private Button loginButton;
    private TextView signupLink;


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
                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();

                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    showErrorMessage(authenticationActivity.loginUser(username,password,LoginFragment.this));
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