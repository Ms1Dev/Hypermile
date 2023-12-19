package com.example.hypermile.data;

import java.io.InputStream;
import java.io.OutputStream;

public interface DataPoint {
    public byte[] requestCode();
    public void passResponse(InputStream inputStream);
}
