package org.signal.aesgcmprovider;

public class OpenSSLApiLibrary {

    public OpenSSLApiLibrary(){

    }

    static {
        System.loadLibrary("aesgcm");
    }

    public native String getDRBGRandomNumber(byte[] seed);
}
