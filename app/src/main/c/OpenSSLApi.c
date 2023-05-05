#include <jni.h>




JNIEXPORT jstring JNICALL
Java_com_key_OpenSSLApi_getRandomStringOpenssl(JNIEnv *env, jobject instance) {





    return (*env)->  NewStringUTF(env, "911B9B36BC7CE94E");

}


JNIEXPORT jstring JNICALL
Java_com_key_OpenSSLApi_getRandomString1(JNIEnv *env, jobject instance) {

    return (*env)->  NewStringUTF(env, "911B9B36BC7CE94E");

}



JNIEXPORT jstring JNICALL
Java_com_key_OpenSSLApi_getRandomString(JNIEnv *env, jobject instance) {

    return (*env)->  NewStringUTF(env, "911B9B36BC7CE94E");

}


JNIEXPORT jstring JNICALL
Java_com_key_OpenSSLApi_getDRBGRandomNumber1(JNIEnv *env, jobject thiz) {

    return (*env)->  NewStringUTF(env, "2C37B132E5BCB1424CF73742A1371AB14C2CF71A413742B1");

}

JNIEXPORT jstring JNICALL
Java_com_key_OpenSSLApi_setCPOCSeed(JNIEnv *env, jobject thiz, jobject cpoc_seed) {
    return (*env)->  NewStringUTF(env, "2C37B132E5BCB1424CF73742A1371AB14C2CF71A413742B1");

}