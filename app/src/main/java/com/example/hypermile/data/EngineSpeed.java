package com.example.hypermile.data;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class EngineSpeed implements DataPoint{
    byte data[] = new byte[256];

    @Override
    public byte[] requestCode() {
        return "010C\r".getBytes();
    }

    @Override
    public void passResponse(InputStream inputStream) {
        try {
            inputStream.read(data);
            inputStream.reset();
            inputStream.close();

            String res = new String(data);

            Log.d("out", "passResponse: " + res);
        }
        catch (IOException e) {

        }
    }

}
