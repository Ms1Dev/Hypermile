package com.example.hypermile.api;

import android.app.Service;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.hypermile.bluetooth.Connection;

import java.security.Provider;

// source: https://google.github.io/volley/requestqueue.html

public class ApiRequest {
    private static Context context;

    public static void setContext(Context context) {
        ApiRequest.context = context.getApplicationContext();
    }
    private static RequestQueue requestQueue;

    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

}
