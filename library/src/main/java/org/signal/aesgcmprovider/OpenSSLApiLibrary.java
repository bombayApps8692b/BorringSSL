package org.signal.aesgcmprovider;

public class OpenSSLApiLibrary {

    static {
        System.loadLibrary("aesgcm");
    }

    public native String getDRBGRandomNumber(int seed);

}
