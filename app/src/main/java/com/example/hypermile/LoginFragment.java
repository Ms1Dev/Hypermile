package com.example.hypermile;

import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

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
                AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();

                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                boolean loginSuccess = false;

                if (!username.isEmpty() && !password.isEmpty()) {
                    showErrorMessage(authenticationActivity.loginUser(username,password));
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
            LinearLayout errorMessage = (LinearLayout) view.findViewById(R.id.errorMessage);
            TextView messageContent = (TextView) view.findViewById(R.id.errorMessageContent);
            errorMessage.setVisibility(View.VISIBLE);
            messageContent.setText(message);
        }
    }
}