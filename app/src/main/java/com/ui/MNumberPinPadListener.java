package com.ui;

public interface MNumberPinPadListener {
    public void handleInput(String input);

    public void handleBackspace();

    public void handleReservedButton();

    public void handleDoneButton();

    public void handleLongBackspace();


    public void handleCancelButton();

    public void handleEnterButton();

}
