package com.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.activity.Controller;
import com.connection.ConnectionManager;
import com.data.TRACE;
import com.mosambee.mpos.cpoc.R;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionDialog extends Dialog implements MNumberPadListener, MAlertDialogListener, View.OnClickListener, View.OnTouchListener {


    private final int transactionType;
    private String amt = "0.00";
    public Activity c;
    public Dialog d;
    Button okButton, cancelButton;
    private String walletProvider;
    Controller controller;
    /*String[] transType={getContext().getString(R.string.preauth),getContext().getString( R.string.sale_complete),
            getContext().getString(R.string.type_void), getContext().getString(R.string.CBWP), getContext().getString(R.string.PWCB)};*/
    private MNumberPad numberPad;
    private EditText etCashback;

    //private final String DEFAULT_AMOUNT = "0.00";
    private String selectedTransType;
    private TextView txt_alertTitle;


    public TransactionDialog(int id, Activity a, Controller snc, String amount) {
        super(a);
        this.c = a;
        controller = snc;
        transactionType = id;
        amt = amount;
    }

    public TransactionDialog(int id, FragmentActivity a, Controller snc) {
        super(a);
        this.c = a;
        controller = snc;
        transactionType = id;
    }

    private void disableKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }


    public void showNumPad() {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);

        numberPad.setVisibility(View.VISIBLE);

        numberPad.invalidate();
    }

    private void hideNumPad() {
        numberPad.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.transactiondialog);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setCancelable(false);
        numberPad = (MNumberPad) findViewById(R.id.number_pad);
        numberPad.registerMNumberPadListener(this);
        numberPad.setReservedButton(MNumberPad.TYPE_DOUBLE_ZERO);
        txt_alertTitle = (TextView) findViewById(R.id.txt_alertTitle);


        if (transactionType == 13) {
            txt_alertTitle.setText(c.getString(R.string.dialog_title_cashback));
            showNumPad();
        } else if (transactionType == 7) {
            txt_alertTitle.setText(c.getString(R.string.dialog_title_sale_tip));
            showNumPad();
        }else if (transactionType == 27) {
            txt_alertTitle.setText(c.getString(R.string.dialog_title_sale_tip));
            showNumPad();
        } else {
            txt_alertTitle.setText(c.getString(R.string.dialog_title_reverse_amount));
            hideNumPad();
        }

        etCashback = (EditText) findViewById(R.id.cashback_field);
        etCashback.setOnTouchListener(this);
        etCashback.setText(amt);
        etCashback.setSelection(etCashback.getText().length());
        disableKeyboard(etCashback);
        etCashback.requestFocus();
        cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(this);

        okButton = (Button) findViewById(R.id.button_ok);
        okButton.setOnClickListener(this);

        Typeface typeface1 = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        cancelButton.setTypeface(typeface1);
        okButton.setTypeface(typeface1);

        if (Build.VERSION.SDK_INT >= 21) {
            okButton.setBackgroundResource(R.drawable.button_dialog);
            cancelButton.setBackgroundResource(R.drawable.button_dialog);
        } else {
            okButton.setBackgroundResource(R.drawable.button_dialog_under21);
            cancelButton.setBackgroundResource(R.drawable.button_dialog_under21);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == okButton) {
            //controller.setCashBackAmountOnButton(controller.getCashBackAmountOnButton());
            if (transactionType == 13) {
                if (Double.parseDouble(etCashback.getText().toString()) > 0.00) {
                    if (Double.parseDouble(controller.getAmount()) > Double.parseDouble(etCashback.getText().toString())) {
                        controller.setCashBackAmountOnButton(etCashback.getText().toString(), transactionType);
                        dismiss();
                        etCashback.setError(null);
                    } else {
                        etCashback.setError("Cashback amount should be less than base amount.");
                        etCashback.requestFocus();
                    }
                } else {
                    etCashback.setError("Enter cashback amount.");
                    etCashback.requestFocus();
                }
            } else if (transactionType == 7) {
                String tipAmount = etCashback.getText().toString();
                if (controller.isValidAmount(tipAmount) && Double.valueOf(controller.getAmount()) * .2 >= Double.valueOf(tipAmount)) {
                    if (Double.valueOf(tipAmount) > 0.00) {
                        controller.setCashBackAmountOnButton(etCashback.getText().toString(), transactionType);
                        controller.setCashBackAmount(etCashback.getText().toString());
                        dismiss();
                        etCashback.setError(null);
                    } else {
                        etCashback.setError(c.getString(R.string.zero_amount));
                        etCashback.requestFocus();
                    }
                } else {
                    etCashback.setError(controller.getString(R.string.invalid_TIPamount));
                    etCashback.requestFocus();
                }
            }else if( transactionType == 27){
                String tipAmount = etCashback.getText().toString();
                if (controller.isValidAmount(tipAmount) && Double.valueOf(controller.getAmount()) * .2 >= Double.valueOf(tipAmount)) {
                    if (Double.valueOf(tipAmount) > 0.00) {
                        controller.setCashBackAmountOnButton(etCashback.getText().toString(), transactionType);
                        controller.setTipAmount(etCashback.getText().toString());
                        dismiss();
                        etCashback.setError(null);
                    } else {
                        etCashback.setError(c.getString(R.string.zero_amount));
                        etCashback.requestFocus();
                    }
                } else {
                    etCashback.setError(controller.getString(R.string.invalid_TIPamount));
                    etCashback.requestFocus();
                }
            } else if (transactionType == 25) {
                Double amount = Double.parseDouble(etCashback.getText().toString());
                if (amount > 0.00 && amount <= Double.parseDouble(amt)) {
                    etCashback.setError(null);
                    controller.setAmount(etCashback.getText().toString());
                    controller.post(ConnectionManager.PostType.BHARAT_QR_REFUND, getContext().getString(R.string.wait));
                    dismiss();
                } else {
                    etCashback.setError("Invalid amount.");
                }
            }
        } else if (v == cancelButton) {

            if (transactionType == 13) {
                controller.setCashBackAmountOnButton("0.00", transactionType);
                controller.showCustomToast("You have not yet entered cashback amount.");
            } else if (transactionType == 7) {
                controller.setCashBackAmountOnButton("0.00", transactionType);
                controller.showCustomToast("You have not yet entered TIP amount.");
            }if (transactionType == 27) {
                controller.setCashBackAmountOnButton("0.00", transactionType);
                controller.showCustomToast("You have not yet entered TIP amount.");
            }

            dismiss();
        }
    }


    @Override
    public void handleInput(String input) {
        if (etCashback.isFocused()) {
            String currentAmount = etCashback.getText().toString();
            if (!currentAmount.trim().equals("") && currentAmount.trim().length() <= controller.getMaxAmount() - 1) {
                //etCashback.setText(String.format("%.2f", (Double.parseDouble(currentAmount) * 10) + (Double.parseDouble(input) * .01)));
                etCashback.setText(controller.setAmountWithDecimal(currentAmount, input));
                etCashback.setError(null);
            } else {
                etCashback.setError(getContext().getString(R.string.max_limit));
            }

            if (Double.parseDouble(controller.getAmount()) > Double.parseDouble(etCashback.getText().toString()))
                etCashback.setError(null);
        }
        etCashback.setSelection(etCashback.getText().length());
    }

    @Override
    public void handleBackspace() {
        if (etCashback.isFocused()) {
            /*String amount = etCashback.getText().toString();
            amount = amount.replaceAll("(\\d*)(\\d)(\\.)(\\d)(\\d)", "$1$3$2$4");

            if (amount.length() == controller.getDecimalPlaces()+1) {
                amount = "0" + amount;
            }
            etCashback.setText(amount);*/

            String amount = etCashback.getText().toString();
            amount = amount.replace(".","");
            amount = amount.substring(0,amount.length()-1);
            //amount = amount.replaceAll("(\\d*)(\\d)(\\.)(\\d)(\\d)", "$1$3$2$4");
            if (amount.length() == controller.getDecimalPlaces()) {
                amount = "0" + amount;
            }
            String temp = "";
            BigInteger d = new BigInteger(amount);
            BigDecimal one = new BigDecimal(d, controller.getDecimalPlaces());
            temp = one.toString();
            TRACE.i("tempCashBack::"+temp);
            etCashback.setText( temp);

            if (!etCashback.getText().toString().trim().equals("") && etCashback.getText().toString().trim().length() <= Integer.parseInt("9")) {
                etCashback.setError(null);
            }
        }
        etCashback.setSelection(etCashback.getText().length());
    }

    @Override
    public void handleReservedButton() {
        String currentAmount = "0.00";
        if (transactionType == 13) {
            if (etCashback.isFocused()) {
                currentAmount = etCashback.getText().toString();
                if (Double.parseDouble(currentAmount) <= 1000) {
                    etCashback.setText(String.format("%.2f", (Double.parseDouble(currentAmount) * 100)));
                    //etCashback.setText(controller.setAmountWithDecimalForReserveButton(currentAmount));
                } else {
                    controller.showDialog("Please enter cashback amount less than or equal to 1000.00 ");
                }
            }
        } else {
            if (etCashback.isFocused()) {
                currentAmount = etCashback.getText().toString();
                if (currentAmount.length() <= controller.getMaxAmount() - controller.getDecimalPlaces()) {
                    //etCashback.setText(String.format("%.2f", (Double.parseDouble(currentAmount) * 100)));
                    etCashback.setText(controller.setAmountWithDecimalForReserveButton(currentAmount));
                    etCashback.setError(null);
                } else {
                    etCashback.setError(getContext().getString(R.string.max_limit));
                    //controller.showDialogMaxAmount(getContext().getString(R.string.max_limit), controller.getActiveActivity(), this);
                }
            }
        }
        etCashback.setSelection(etCashback.getText().length());
    }

    @Override
    public void handleDoneButton() {

    }

    @Override
    public void handleLongBackspace() {
        if (etCashback.hasFocus()) {
            etCashback.setText(controller.getDefaultAmount());
            etCashback.setError(null);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        TRACE.i("Inside onTouch-------------");
        showNumPad();
        etCashback.setError(null);
        disableKeyboard(etCashback);
        return true;

    }

    @Override
    public void onAlertConfirmation() {

    }
}
