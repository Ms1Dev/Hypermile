package com.example.hypermile;

/**
 * Callback for login and registration error messages
 */
public interface AuthRequester {
    public void authError(String errorMessage);
}
