package com.key;

import com.data.TRACE;
import com.encryption.DUKPK2009_CBC;

import org.signal.aesgcmprovider.OpenSSLApiLibrary;

import java.util.Random;




public class OpenSSLApi {

    static{
        System.loadLibrary("OpenSSLApi");
    }

    public native String setCPOCSeed(StringBuffer cpocSeed);


    public native String getRandomString();

//    public static String getDRBGRandomNumber() {
//        return "";
//    }

    public static void setSeed(String randomNumberFromDevice) {
    }




    public native String getDRBGRandomNumber1();


    public String getDRBGRandomNumber() {
        return getDRBGRandomNumber1();
    }

    public String getDRBGLocalMasterKey(String seed) {
        char []tempArray = seed.toCharArray();
        int  j = 0;
        for (int i=0;i<tempArray.length;i++){
            j = j+ (int) tempArray[i];

        }
        OpenSSLApiLibrary openSSLApi = new OpenSSLApiLibrary();
        String random = DUKPK2009_CBC.parseByte2HexStr(String.valueOf(openSSLApi.getDRBGRandomNumber(j)).getBytes());

        return random.substring(0,48);

    }



    public String getDRBGRandomNumber(String seed) {
        char []tempArray = seed.toCharArray();
        int  j = 0;
        for (int i=0;i<tempArray.length;i++){
            j = j+ (int) tempArray[i];

        }
        OpenSSLApiLibrary openSSLApi = new OpenSSLApiLibrary();
        String random = DUKPK2009_CBC.parseByte2HexStr(
                String.valueOf(openSSLApi.getDRBGRandomNumber(j)).getBytes());

        TRACE.i("DRBG SEED :"+seed);
        TRACE.i("DRBG RANDOM STRING :"+random);

        char []randomArray = random.toCharArray();

        for (int i=0;i<randomArray.length;i++){
            j = j + (int) randomArray[1];

        }


        return String.valueOf(j);

    }
}
