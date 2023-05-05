package com.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.autolocktimer.OnAutoLockListener;
import com.connection.ResponseType;
import com.data.TRACE;
import com.mosambee.mpos.cpoc.R;
import com.ui.MAlertDialog;
import com.ui.MAlertDialogListener;
import com.ui.MNumberPad;
import com.ui.MNumberPadListener;

public class LockScreen extends Activity implements OnTouchListener, MNumberPadListener, OnAutoLockScreen, MAlertDialogListener {
    private static final String PASSWORD_CHARACTER = "*";

    private Controller controller;
    private MNumberPad numberPad;
    private OnAutoLockListener autoLockTimer;

    private StringBuilder pin;

    private MAlertDialog alertDialog;
    private boolean isSessionExpired;

    private EditText etPin1;
    private EditText etPin2;
    private EditText etPin3;
    private EditText etPin4;
    private static final int REQUEST_CODE_PERMISSION = 2;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        TRACE.i("Req Code for permission"+ "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length >0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[7] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[8] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[9] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[10] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[11] == PackageManager.PERMISSION_GRANTED) {
                pin = new StringBuilder();

                displayDialog();
                initialize();
                intializePinFields();
            }else{
                Toast.makeText(this, "Permission not granted!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_screen);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        Intent intent = getIntent();
        String str=intent.getStringExtra("caller");
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        controller = (Controller) getApplication();



        controller = (Controller) getApplication();
        if(controller.startProcess()) {
            pin = new StringBuilder();

            displayDialog();
            initialize();
            intializePinFields();
        }else{
            ActivityCompat.requestPermissions(this, controller.mPermission, REQUEST_CODE_PERMISSION);
        }

    }

    private void initialize() {
        numberPad = (MNumberPad) findViewById(R.id.number_pad);
        numberPad.registerMNumberPadListener(this);
        numberPad.setReservedButton(MNumberPad.TYPE_LOGOUT);

        autoLockTimer = controller.getAutoLockTimer(Integer.parseInt(getString(R.string.session_time_out)));
        autoLockTimer.setOnAutoLockListener(this);
        autoLockTimer.startTimer();
    }

    private void intializePinFields() {
        etPin1 = (EditText) findViewById(R.id.et_pin1);
        etPin1.setOnTouchListener(this);

        etPin2=(EditText) findViewById(R.id.et_pin2);
        etPin2.setOnTouchListener(this);

        etPin3=(EditText) findViewById(R.id.et_pin3);
        etPin3.setOnTouchListener(this);

        etPin4=(EditText) findViewById(R.id.et_pin4);
        etPin4.setOnTouchListener(this);
        disableKeyboard(etPin1);
    }

    private void displayDialog() {
        if(isSessionExpired) {
            autoLockTimer.stopTimer();
            alertDialog = new MAlertDialog(this, R.style.MosambeeDialogTheme,controller);
            alertDialog.show();
            alertDialog.setMessage(getString(R.string.session_expired),"");
            alertDialog.setButtonText(getString(R.string.login));
            alertDialog.registerMAlertDialogListener(this);
        }
    }
    private void disableKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                                     Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
    private void setPin(String input) {
        if(etPin1.getText().toString().length() < 1) {
            pin.append(input);
            etPin1.setText(PASSWORD_CHARACTER);
            etPin1.setFocusableInTouchMode(false);

            etPin2.setFocusableInTouchMode(true);
            etPin2.requestFocus();
            disableKeyboard(etPin2);
        } else if(etPin2.getText().toString().length() < 1) {
            pin.append(input);
            etPin2.setText(PASSWORD_CHARACTER);
            etPin2.setFocusableInTouchMode(false);

            etPin3.setFocusableInTouchMode(true);
            etPin3.requestFocus();
            disableKeyboard(etPin3);
        } else if(etPin3.getText().toString().length() < 1) {
            pin.append(input);
            etPin3.setText(PASSWORD_CHARACTER);
            etPin3.setFocusableInTouchMode(false);

            etPin4.setFocusableInTouchMode(true);
            etPin4.requestFocus();
            disableKeyboard(etPin4);
        } else if(etPin4.getText().toString().length() < 1) {
            pin.append(input);
            etPin4.setText(PASSWORD_CHARACTER);
            etPin4.setFocusableInTouchMode(false);
            etPin4.clearFocus();

            etPin1.setFocusableInTouchMode(true);
            etPin1.requestFocus();
            disableKeyboard(etPin1);
            firePinFieldSetEvent();
        }
    }

    private void clearPin() {
        if (pin.length() < 1) {
            return;
        }

        if (etPin4.getText().length() == 1) {
            etPin4.setText("");
            pin = pin.deleteCharAt(pin.length() - 1);

            etPin4.setFocusableInTouchMode(false);
        } else if (etPin3.getText().length() == 1) {
            etPin4.setFocusableInTouchMode(false);

            pin = pin.deleteCharAt(pin.length() - 1);
            etPin3.setText("");
            etPin3.setFocusableInTouchMode(true);
            etPin3.requestFocus();
            disableKeyboard(etPin3);
        } else if (etPin2.getText().length() == 1) {
            etPin3.setFocusableInTouchMode(false);

            pin = pin.deleteCharAt(pin.length() - 1);
            etPin2.setText("");
            etPin2.setFocusableInTouchMode(true);
            etPin2.requestFocus();
            disableKeyboard(etPin2);
        } else {
            etPin2.setFocusableInTouchMode(false);
            pin = pin.deleteCharAt(pin.length() - 1);
            etPin1.setText("");
            etPin1.setFocusableInTouchMode(true);
            etPin1.requestFocus();
            disableKeyboard(etPin1);
        }
    }

    private void firePinFieldSetEvent() {

        if (controller.isValidPin(pin.toString())) {
            autoLockTimer.stopTimer();
            //controller.showActivityBeforeLock(this);
            //controller.showActivity("com.ui.MainActivityFragment");
            controller.showActivity("com.ui.MainActivityFragment", Controller.KEY_TRANSACTION_TYPE, Controller.TRANSACTION_TYPE_SALE, ResponseType.SUCCESS);
        } else {
            controller.showDialog(getString(R.string.invalid_pin), this);
        }


            if (controller.isValidPin(pin.toString())) {
                autoLockTimer.stopTimer();
                controller.showActivity("com.ui.MainActivityFragment");
            } else {
                controller.showDialog(getString(R.string.invalid_pin), this);
            }
            clearPinField();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        autoLockTimer.resetTimer();
        if (v.getId() == etPin1.getId()) {
            etPin1.setInputType(InputType.TYPE_NULL); // disable soft input
            etPin1.onTouchEvent(event); // call native handler
            etPin1.setInputType(etPin1.getInputType()); // restore input type

            return true; // consume touch even
        } else if (v == etPin2) {
            etPin2.setInputType(InputType.TYPE_NULL); // disable soft input
            etPin2.onTouchEvent(event); // call native handler
            etPin2.setInputType(etPin2.getInputType()); // restore input type

            return true; // consume touch even
        } else if (v == etPin3) {
            etPin3.setInputType(InputType.TYPE_NULL); // disable soft input
            etPin3.onTouchEvent(event); // call native handler
            etPin3.setInputType(etPin3.getInputType()); // restore input type

            return true; // consume touch even
        } else if (v == etPin4) {
            etPin4.setInputType(InputType.TYPE_NULL); // disable soft input
            etPin4.onTouchEvent(event); // call native handler
            etPin4.setInputType(etPin4.getInputType()); // restore input type

            return true; // consume touch even
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void clearPinField() {
        etPin1.setText("");
        etPin2.setText("");
        etPin3.setText("");
        etPin4.setText("");

        pin = new StringBuilder();
        etPin1.setFocusableInTouchMode(true);
        etPin1.requestFocus();
        disableKeyboard(etPin1);
    }

    @Override
    public void handleInput(String input) {
        setPin(input);
    }

    @Override
    public void handleBackspace() {
        clearPin();
    }

    @Override
    public void handleReservedButton() {
        autoLockTimer.stopTimer();
        controller.showActivity(new Intent(this, Login.class));
    }

    @Override
    public void handleDoneButton() {}

    @Override
    public void handleLongBackspace() {
        etPin1.setText("");
        etPin2.setText("");
        etPin3.setText("");
        etPin4.setText("");

        etPin1.setFocusableInTouchMode(true);
        etPin1.requestFocus();
        disableKeyboard(etPin1);
    }

    @Override
    public void onAutoLock() {
        isSessionExpired = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayDialog();
            }
        });
    }

    @Override
    public void onAlertConfirmation() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        controller.startActivity(intent);
    }


}
