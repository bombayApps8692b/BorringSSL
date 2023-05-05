package com.paycore;

public interface KernelListener {

    void onCardDetected();

    void onGoOnline();

    void onResult();
}
