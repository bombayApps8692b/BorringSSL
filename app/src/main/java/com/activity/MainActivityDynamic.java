package com.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.ActionBarOverlayLayout.LayoutParams;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
//import android.widget.DropDownListView;
import android.widget.DropDownListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.connection.ConnectionManager;
import com.connection.ConnectionManager.PostType;
import com.connection.PostTask;
import com.connection.RequestHandler;
import com.data.TRACE;
import com.mosambee.mpos.cpoc.R;
import com.ui.MActionDialogListener;
import com.ui.MAlertDialogListener;
import com.ui.MEditText;
import com.ui.MEditTextListener;
import com.ui.MListDialog;
import com.ui.MListDialogListener;
import com.ui.MNumberPadListener;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class MainActivityDynamic extends ActiveFragment implements OnTouchListener, ScanDataInterface, MListDialogListener, EMIChangeHead, CardTransactionActivity, ReaderListener, ResponseListener, MEditTextListener, MNumberPadListener, MActionDialogListener, MAlertDialogListener, View.OnFocusChangeListener, View.OnClickListener {
    private View v;
    private LinearLayout myLayout, layoutRadioGroup, layoutpasswordGroup;
    LayoutParams lp;
    private TextView textView;
    private DropDownListView ddList;
    //private Button button;
    private boolean isPopulated;

    private Pattern regex, regexPaste;
    private char separator;
    private JSONArray jsonArray;
    private CheckBox[] checkBoxes;
    private Button[] buttons;
    private TextView[] textViews;
    private EditText[] editTexts, passwordEditTexts;
    private Spinner spinnerDropDown;
    private ArrayAdapter arrayAdapterDropDown;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    int editTextCount, buttonCount, textViewCount, radioGroupCount, spinnerCount, checkBoxCount;
    int etVal = 0, btnVal = 0, txtVal = 0, rgBtnVal = 0, pwdGPVal = 0, spnVal = 0, cbVal = 0, rVal = 0;


    private TextWatcher pswdTextWatcher, amountWatcher;
    private Typeface typeface, typeface1;
    private String[] listRadioGroup, listpasswordGroup, listSpinner, receiptData;

    private String[] listOfCBValues = null;
    private String[] listOfDDValues = null;
    private String[] cbArray = null;
    private List<String> listOfSpinnerValue[];
    private EditText etInvoice;
    //private EditText etMSISDN;
    private JSONObject essentialObject;
    private ImageView imgLogo;
    private RadioGroup[] radioGroups;
    private Spinner[] spinners;

    List<String> spinnerValues[];
    HashMap<String, String> jRequest;
    private CompoundButton.OnCheckedChangeListener checkEvent;
    private AdapterView.OnItemSelectedListener dropDownEvent;
    private String[] listOfRBValues;
    private String[] listOfpasswordGPValues;
    private int selectedStatus;
    private String outStandings = "0.00";
    private int receiptDataCount;
    private int passwordGPCount;
    private int otpLevel = 0;
    private boolean sentOtpFlag = false;
    boolean allEqual = true;
    HashMap<String, String> aMap = new HashMap<>();
    private LayoutParams params;
    private View.OnClickListener cashChequeListener;

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public Void emiChangeLayoutHeading() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.main_dynamic, container, false);
        myLayout = (LinearLayout) v.findViewById(R.id.contentLinearLayout);
        myLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        isPopulated = false;
        TRACE.i("Inside oncrete---------------------");
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imgLogo = (ImageView) v.findViewById(R.id.imgLogo);
        controller = (Controller) getActivity().getApplication();
        amountWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                et_amount.setSelection(et_amount.getText().length());
                if (et_amount.getText().toString().length() <= 10) {
                    if (!s.toString().matches(regex.toString()) || s.toString().matches(regexPaste.toString())) {
                        String coins = s.toString().replaceAll("[^\\d]", "");
                        StringBuilder builder = new StringBuilder(coins);
                        while (builder.length() > 3 && builder.charAt(0) == '0') {
                            builder.deleteCharAt(0);
                        }
                        while (builder.length() < 3)
                            builder.insert(0, '0');
                        builder.insert(builder.length() - 2, separator);
                        et_amount.setText(builder.toString());
                    }
                    et_amount.setSelection(et_amount.getText().length());
                    handleDeviceStatus();
                } else
                    et_amount.setText(et_amount.getText().toString());
            }
        };

        pswdTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (passwordEditTexts != null) {
                    for (int i = 0; i < passwordEditTexts.length; i++) {
                        if (passwordEditTexts[i] != null && passwordEditTexts[i].hasFocus()
                                && passwordEditTexts[i].getText().toString().trim().length() >= passwordEditTexts[i].getMinEms()) {
                            aMap.put(passwordEditTexts[i].getHint().toString(), passwordEditTexts[i].getText().toString());
                            TRACE.i("Inside TextWatcher---------------------" + passwordEditTexts[i].getText().toString());
                        }
                    }
                }
            }
        };

        cashChequeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TRACE.i("view.getId()--------" + view.getId());


                if (et_amount != null) {
                    amount = Double.valueOf(et_amount.getText().toString());
                } else {
                    controller.showDialog("Amount field is missing.");
                    return;
                }


                TRACE.i("----------" + controller.getLevelFlag());

                controller.setAmount(getAmount());
                StringBuilder validFields = new StringBuilder();
                if (controller.getLevelFlag() == 2)
                    jRequest.putAll(controller.getModularRequest());
                for (int i = 0; i < etVal; i++) {
                    TRACE.i(">>>>>>" + editTexts[i].getHint().toString().trim() + ">>>>>>>>>>" + isCallable[i]);
                    if (isCallable[i] == 1 || editTexts[i].getHint().toString().trim().equalsIgnoreCase("amount")) {
                        try {
                            if (!editTexts[i].getText().toString().trim().equals("")
                                    && editTexts[i].getText().toString().trim().length() >= editTexts[i].getMinEms()) {
                                validationFlag = validationFlag && true;
                                TRACE.i(editTexts[i].getHint() + ">>>>>>>" + editTexts[i].getText().toString() + "+++" + validationFlag);

                                if (!editTexts[i].getHint().toString().trim().equalsIgnoreCase("amount") && ((MEditText) editTexts[i]).getRequiredInReceipt() == 1)
                                    jRequest.put(editTexts[i].getHint().toString().trim(), editTexts[i].getText().toString());

                            } else if (editTexts[i].getMinEms() == 0) {
                                validationFlag = validationFlag && true;
                            } else {
                                validFields.append(editTexts[i].getHint().toString().trim() + ",");
                                validationFlag = validationFlag && false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                StringBuilder entireCheckBox = new StringBuilder();
                if (cbArray != null) {
                    for (String s : cbArray
                            ) {
                        if (s != null)
                            entireCheckBox.append(s + ":");

                    }
                    jRequest.put("Charges", entireCheckBox.toString());
                }
                TRACE.i("+++++++" + validationFlag);
                TRACE.i(rVal + "+++++++" + controller.getType().getJsonObject());

                if (controller.getType().getJsonObject() != null) {

                    for (int i = 0; i < rVal; i++) {
                        try {
                            JSONObject json = new JSONObject(controller.getType().getJsonObject().getString("fetchServiceResponse"));
                            jRequest.put(receiptData[i], json.getString(receiptData[i]));
                            TRACE.i(receiptData[i] + ">>>>>>>" + json.getString(receiptData[i]));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }


                TRACE.i(jRequest.size() + "-------------" + controller.getLevelFlag());
                if (amount > 0.00 && validationFlag) {
                    validationFlag = true;
                    if (controller.getLevelFlag() != 1) {
                        try {
                            controller.setLevelFlag(2);
                            controller.setModularRequest(jRequest);
                            controller.setFetchServiceResponse(controller.getType().getJsonObject().getJSONObject("fetchServiceResponse").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            controller.showDialog("Lost the Details. Fetch again and try.");
                            controller.callToFragment("Modular");
                            controller.setLevelFlag(1);
                            return;
                        }
                    } else {
                        controller.setLevelFlag(2);
                        controller.setModularRequest(jRequest);
                    }

                    if (amount > 50000.00) {
                        et_amount.requestFocus();
                        et_amount.setError("Please enter amount less than or equal to 50000.00 ");
                        return;
                    } else {
                        controller.setAmount(getAmount());
                        et_amount.setError(null);
                        String type = ((Button) view).getText().toString();
                        TRACE.i("1-----" + jRequest.size());
                        jRequest = controller.getjRequest();
                        controller.setLevelFlag(2);
                        if (jRequest.size() > 0) {

                            StringBuilder requestStr = new StringBuilder();
                            Set<String> keys = jRequest.keySet();
                            for (String key : keys) {
                                TRACE.i("Keyyy::" + key);
                                if (!key.equalsIgnoreCase("amount")) {
                                    requestStr.append(key + "=" + jRequest.get(key) + ":");
                                }
                            }
                            controller.setDescription(requestStr.toString());
                            TRACE.i("2-----" + controller.getDescription());
                        }
                        if (type.equalsIgnoreCase("cash")) {
                            controller.setPostType(ConnectionManager.PostType.CASH);
                            setType(FragmentType.CASH);
                            controller.appendToJsonResponseObject(Controller.KEY_TRANSACTION_TYPE, Controller.TRANSACTION_TYPE_CASH);
                            controller.setBackStackActivity(BackStackActivity.CASH);
                            controller.setFragmentType(FragmentType.CASH);
                            controller.showFragment("com.activity.CashCheque");
                            return;
                        } else {
                            setType(FragmentType.CHEQUE);
                            controller.appendToJsonResponseObject(Controller.KEY_TRANSACTION_TYPE, Controller.TRANSACTION_TYPE_CHEQUE);
                            controller.setFragmentType(FragmentType.CHEQUE);
                            controller.setBackStackActivity(BackStackActivity.CHEQUE);
                            controller.setPostType(PostType.CHEQUE);
                            controller.showFragment("com.activity.CashCheque");
                            return;

                        }


                    }

                } else {
                    et_amount.requestFocus();
                    et_amount.setError("Enter valid amount");
                    return;

                }

                //controller.post();
            }
        };

        controller.stopProgressDialog();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        TRACE.i("Inside onResume---------------------");
        controller.setHandleScanDataListener(this);
        jRequest = new HashMap<String, String>();
        jRequest.clear();
        if (!isPopulated) {
            isPopulated = true;
            populateFields();
        }
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    StringBuffer str;

    private void callForLevel() {
        TRACE.i("\n=====controller.getLevelFlag()------" + controller.getLevelFlag());

        try {
            if (controller.getLevelFlag() == 2) {
                TRACE.i("Inside 2 level---------------------");

                JSONObject jsonObject = controller.getType().getJsonObject().getJSONObject("fetchServiceResponse");
                str = new StringBuffer();


                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object aObj = jsonObject.get(key);
                    if (aObj instanceof JSONObject) {
                        System.out.println(aObj);
                    } else {
                    /*if("outSatdingAmount".equals(key))
                        str.append("\n" + key + " : INR " +jsonObject.optString(key));
                    else*/

                        if ("outSatdingAmount".equals(key)) {
                            str.append("\n" + "Total Due Amount" + " :  " + jsonObject.optString(key));
                        } else if (!("finalResponse".equals(key) || "status".equals(key)))
                            str.append("\n" + key + " :  " + jsonObject.optString(key));

                    }
                }

                if (jsonObject.has("outSatdingAmount"))
                    outStandings = jsonObject.getString("outSatdingAmount");

                //controller.setAmount(String.format("%.2f",Math.abs(Double.parseDouble(outStandings)) ));

                controller.getTransactionTextView().setText(getString(R.string.sale).toUpperCase());
                controller.setFragmentType(FragmentType.SALE);
                setType(FragmentType.SALE);
                controller.setBackStackActivity(BackStackActivity.SALE);

                for (int i = 0; i < textViewCount; i++) {
                    if (textViews[i] != null) {
                        if ("display".equalsIgnoreCase(textViews[i].getHint().toString()))
                            textViews[i].setText(str.toString());
                    }
                }

                TRACE.i("outstanding::::" + Math.abs(Double.parseDouble(outStandings)));

                for (int i = 0; i < editTextCount; i++) {
                    if (editTexts[i] != null) {
                        if ("amount".equalsIgnoreCase(editTexts[i].getHint().toString())) {
                            if (Double.parseDouble(outStandings) > 0)
                                editTexts[i].setText(String.format("%.2f", Math.abs(Double.parseDouble(outStandings))));
                            else
                                editTexts[i].setText("0.00");

                            editTexts[i].setSelection(editTexts[i].getText().length());
                        }
                    }
                }
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    int[] isCallable, passwordisCallable;

    public static byte[] hexToByteArray(String hexString) {
        TRACE.i("Inside hexToByte---------------------");
        int len = hexString.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }


    JSONObject jsonObject;
    JSONObject levelCount;

    public void populateFields() {
        validationFlag = true;
        TRACE.i("Inside populateFields---------------------");
        controller.getTransactionTextView().setText(getString(R.string.sale).toUpperCase());
        controller.setFragmentType(FragmentType.SALE);
        setType(FragmentType.SALE);
        controller.setBackStackActivity(BackStackActivity.SALE);
        typeface1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        try {

            checkEvent = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (buttonView.getId() == 1) {
                        if (isChecked) {
                            amount = amount + Double.parseDouble(buttonView.getTag().toString());
                            TRACE.i("-------------------***************------------------" + buttonView.getTag() + "   TOTAL ::" + amount);

                        } else {
                            amount = amount - Double.parseDouble(buttonView.getTag().toString());
                            TRACE.i("-------------------***************------------------" + buttonView.getTag() + "   TOTAL ::" + amount);

                        }
                        et_amount.setText(String.format("%.2f", (amount)));
                    }
                }
            };


            dropDownEvent = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    //TRACE.i("--------------------"+spinners[i].getTag());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    //TRACE.i("onNothingSelected--------------------"+spinners[0].getTag());

                }
            };

            jsonObject = new JSONObject(controller.getModularJSONResponse());
            jsonArray = jsonObject.getJSONArray("modularAppElement");
            essentialObject = jsonObject.getJSONObject("modularAppEssentials");

            try {
                if (!essentialObject.getString("primaryColor").equals("") && !essentialObject.getString("secondaryColor").equals("")) {
                    controller.setPrimaryColor(essentialObject.getString("primaryColor"));
                    controller.setSecondaryColor(essentialObject.getString("secondaryColor"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] imagebytes = hexToByteArray(essentialObject.has("logo") ? essentialObject.getString("logo") : "");
            if (imagebytes != null) {
                InputStream inputStream = new ByteArrayInputStream(imagebytes);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgLogo.setImageBitmap(bitmap);
                imgLogo.setVisibility(View.VISIBLE);
            } else {
                imgLogo.setVisibility(View.GONE);
            }

            /*GlideApp.with(getActivity())
                    .load("https://test.mosambee.in/mpos.png")
                    .into(imgLogo);*/

            otpLevel = Integer.parseInt(essentialObject.has("otpLevel") ? essentialObject.getString("otpLevel") : "0");


            if (jsonObject.has("level" + controller.getLevelFlag() + "Count"))
                levelCount = jsonObject.getJSONObject("level" + controller.getLevelFlag() + "Count");


            editTextCount = levelCount.getInt("EditTextCount");
            buttonCount = levelCount.getInt("ButtonCount");
            textViewCount = levelCount.getInt("TextViewCount");
            radioGroupCount = levelCount.getInt("RadioGroupCount");
            spinnerCount = levelCount.getInt("DropDownCount");
            checkBoxCount = levelCount.getInt("CheckBoxCount");
            receiptDataCount = levelCount.getInt("receiptDataCount");
            passwordGPCount = levelCount.getInt("PasswordGroupCount");
            TRACE.i("PasswordGroupCount::::::::::::" + passwordGPCount);
            if (editTextCount > 0) {
                editTexts = new EditText[editTextCount];
                isCallable = new int[editTextCount];
            }
            if (passwordGPCount > 0) {
                passwordEditTexts = new EditText[passwordGPCount];
                passwordisCallable = new int[passwordGPCount];
                listOfpasswordGPValues = new String[passwordGPCount];
            }
            if (buttonCount > 0)
                buttons = new Button[buttonCount];
            if (textViewCount > 0)
                textViews = new TextView[textViewCount];
            if (radioGroupCount > 0) {
                radioGroups = new RadioGroup[radioGroupCount];
                listOfRBValues = new String[radioGroupCount];
            }
            if (spinnerCount > 0) {
                spinners = new Spinner[spinnerCount];
                listOfSpinnerValue = new ArrayList[spinnerCount];
                spinnerValues = new ArrayList[spinnerCount];

                for (int i = 0; i < spinnerCount; i++) {
                    spinnerValues[i] = new ArrayList<String>();
                    listOfSpinnerValue[i] = new ArrayList<String>();
                }
                /*listOfDDValues = new String[spinnerCount];
                listOfDDValues[0]="adhsd";*/
            }
            if (checkBoxCount > 0) {
                checkBoxes = new CheckBox[checkBoxCount];
                listOfCBValues = new String[checkBoxCount];
            }
            if (receiptDataCount > 0) {
                receiptData = new String[receiptDataCount];
            }

            if (jsonArray.length() == 0) {
                buttons = new Button[1];
                buttons[0] = new Button(getActivity());
                buttons[0].setText("TRY AGAIN!");
                buttons[0].setHint("TRY AGAIN!");
                buttons[0].setLayoutParams(lp);
                buttons[0].setGravity(Gravity.CENTER_HORIZONTAL);
                myLayout.addView(buttons[0]);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjTemp = jsonArray.getJSONObject(i);
                    if (jsonObjTemp != null) {
                        createView(myLayout, jsonObjTemp);
                    } else {
                        buttons = new Button[1];
                        buttons[0] = new Button(getActivity());
                        buttons[0].setText("TRY AGAIN!");
                        buttons[0].setHint("TRY AGAIN!");
                        buttons[0].setLayoutParams(lp);
                        buttons[0].setGravity(Gravity.CENTER_HORIZONTAL);
                        myLayout.addView(buttons[0]);
                        break;
                    }
                }


                for (int i = 0; i < buttonCount; i++) {
                    if (buttons[i].getHint().toString().equalsIgnoreCase("CHARGE"))
                        createCashChequeButtons();
                }

                if (controller.getLevelFlag() == otpLevel) {
                    createOTPLayout(myLayout);
                }
            }


            for (int i = 0; i < btnVal; i++) {
                myLayout.addView(buttons[i]);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (buttons != null && buttons.length > 0) {
            for (int i = 0; i < buttonCount; i++) {
                if (buttons[i] != null)
                    buttons[i].setOnClickListener(singleOnclickForChargeButton);
            }
            handleDeviceStatus();
        }

        if (radioGroups != null) {
            TRACE.i("  radioGroups.length+++" + radioGroups.length + "++++" + listOfRBValues.length + listOfRBValues[0]);
            for (int i = 0; i < radioGroups.length; i++) {
                if (radioGroups[i] != null) {
                    final int finalI = i;
                    radioGroups[i].setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {

                            if (radioGroups.length > 2 && checkedId != 2)
                                ((RadioButton) group.getChildAt(2)).setChecked(false);

                            TRACE.i("Group::" + listOfRBValues[finalI] + "checkedId:::" + checkedId + ">>>>>>>>" + group.getChildAt(checkedId).getTag());
                            if (jRequest.containsKey(listOfRBValues[finalI]))
                                jRequest.remove(listOfRBValues[finalI]);
                            jRequest.put(listOfRBValues[finalI], group.getChildAt(checkedId).getTag().toString());
                        }

                    });
                }
            }
        }

        callForLevel();

        controller.stopProgressDialog();
    }

    private void createCashChequeButtons() {

        if (controller.getAllowedTransactionTypeArray().contains(TransactionType.CASH)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            Button button = new Button(getActivity());
            button.setText("CASH");
            button.setLayoutParams(lp);
            button.setClickable(true);
            button.setOnClickListener(cashChequeListener);
            //button.setTag(jsonView.getString("viewName"));
            button.setTextSize(16);
            button.setTypeface(typeface1, Typeface.BOLD);
            button.setBackgroundColor(Color.parseColor(controller.getPrimaryColor()));
            button.setTextColor(getResources().getColor(R.color.black));
            button.setGravity(Gravity.CENTER);
            params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);

            button.setLayoutParams(params);

            myLayout.addView(button);

        }
        if (controller.getAllowedTransactionTypeArray().contains(TransactionType.CHEQUE)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            Button button = new Button(getActivity());
            button.setText("CHEQUE");
            button.setLayoutParams(lp);
            button.setClickable(true);
            button.setOnClickListener(cashChequeListener);
            //button.setTag(jsonView.getString("viewName"));
            button.setTextSize(16);
            button.setTypeface(typeface1, Typeface.BOLD);
            button.setBackgroundColor(Color.parseColor(controller.getPrimaryColor()));
            button.setTextColor(getResources().getColor(R.color.black));
            button.setGravity(Gravity.CENTER);
            params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);

            button.setLayoutParams(params);

            myLayout.addView(button);

        }
    }


    EditText editTextOTP;

    private void createOTPLayout(LinearLayout myLayout) {
        sentOtpFlag = false;
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final LinearLayout myLayout1 = new LinearLayout(getActivity());
        myLayout1.setGravity(Gravity.CENTER);
        myLayout1.setOrientation(LinearLayout.HORIZONTAL);
        editTextOTP = new EditTextAlwaysLast(getActivity());

        editTextOTP.setHint("Enter OTP");
        editTextOTP.setMinEms(6);
        editTextOTP.setMaxEms(6);

        editTextOTP.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        editTextOTP.setTypeface(typeface1, Typeface.NORMAL);
        editTextOTP.setLayoutParams(lp);
        editTextOTP.setGravity(Gravity.CENTER_HORIZONTAL);

        editTextOTP.setInputType(InputType.TYPE_CLASS_NUMBER);

        editTextOTP.setSelection(editTextOTP.getText().toString().length());
        editTextOTP.setTextColor(getResources().getColor(R.color.black));
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editTextOTP, R.color.black);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        final ImageButton imgbutton = new ImageButton(getActivity());
        imgbutton.setBackgroundResource(R.drawable.resend);
        imgbutton.setClickable(true);
        imgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et_moNo = new EditText(getActivity());
                for (int i = 0; i < etVal; i++) {
                    TRACE.i(">>>>>>" + editTexts[i].getHint().toString().trim() + ">>>>>>>>>>" + isCallable[i]);
                    if (isCallable[i] == 1 || editTexts[i].getHint().toString().trim().equalsIgnoreCase("Mobile Number")) {
                        et_moNo = editTexts[i];
                    }

                }
                if (et_moNo.getText().toString().length() == 10) {
                    et_moNo.setError(null);
                    controller.setMobileno(et_moNo.getText().toString());
                    controller.post(PostType.GENERATE_OTP, getString(R.string.wait));
                    editTextOTP.setText("");
                } else {
                    et_moNo.setError("Enter Proper Mobile Number.");
                    editTextOTP.requestFocus();
                }

            }
        });
        //button.setTag(jsonView.getString("viewName"));

        imgbutton.setLayoutParams(lp);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


        myLayout1.setVisibility(View.GONE);
        imgbutton.setLayoutParams(params);
        myLayout1.addView(editTextOTP);
        myLayout1.addView(imgbutton);
        myLayout.addView(myLayout1);


        final Button button = new Button(getActivity());
        button.setText("Send OTP");
        button.setLayoutParams(lp);
        button.setClickable(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText et_moNo = new EditText(getActivity());
                for (int i = 0; i < etVal; i++) {
                    TRACE.i(">>>>>>" + editTexts[i].getHint().toString().trim() + ">>>>>>>>>>" + isCallable[i]);
                    if (isCallable[i] == 1 && editTexts[i].getHint().toString().trim().equalsIgnoreCase("Mobile Number")) {
                        et_moNo = editTexts[i];
                    }

                }
                if (et_moNo.getText().toString().length() == 10) {
                    et_moNo.setError(null);
                    myLayout1.setVisibility(View.VISIBLE);
                    button.setVisibility(View.GONE);
                    controller.setMobileno(et_moNo.getText().toString());
                    controller.post(PostType.GENERATE_OTP, getString(R.string.wait));
                    sentOtpFlag = true;
                } else {
                    et_moNo.setError("Enter Proper Mobile Number.");
                    et_moNo.requestFocus();
                }


                //controller.post();
            }
        });
        //button.setTag(jsonView.getString("viewName"));
        button.setTextSize(16);
        button.setTypeface(typeface1, Typeface.BOLD);
        button.setBackgroundColor(Color.parseColor(controller.getPrimaryColor()));
        button.setTextColor(getResources().getColor(R.color.black));
        button.setGravity(Gravity.CENTER);
        params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 5, 0, 5);

        button.setLayoutParams(params);

        myLayout.addView(button);


    }

    private int timeChargeButton = 500;
    private final String SEPARATOR = "|";
    Double amount = 0.0;
    private boolean validationFlag = true;
    SingleOnClickListener singleOnclickForChargeButton = new SingleOnClickListener(timeChargeButton) {
        @SuppressLint("NewApi")
        @Override
        public void onDebouncedClick(View view) {
            TRACE.i("--------------------------Clicked-----------" + getType().getId());
            LockableActivity.resetLockTimer();
            validationFlag = true;
            allEqual = true;
            if (checkBoxes != null) {
                int ii = 0;
                cbArray = new String[checkBoxes.length];
                for (int i = 0; i < checkBoxes.length; i++) {
                    if (checkBoxes[i].isChecked() && checkBoxes[i].getMinEms() == 1) {
                        cbArray[ii] = checkBoxes[i].getText().toString().split("\n")[0].trim();
                        TRACE.i(checkBoxes[i].getText().toString() + "\n-------------------***************------------------" + checkBoxes[i].getText().toString().split("\n")[0].trim() + "   TOTAL ::" + amount);

                        TRACE.i("ii>" + cbArray[ii]);
                        ii = ii + 1;
                    }
                }
            }

            if (controller.getLevelFlag() == otpLevel) {
                if (editTextOTP.getText().toString().trim().equals(controller.getOTPValue())) {
                    editTextOTP.setError(null);
                    validationFlag = true;
                } else {
                    if (sentOtpFlag) {
                        editTextOTP.setError("Wrong OTP!");
                        editTextOTP.requestFocus();
                    } else
                        controller.showDialog("Generate OTP then proceed.");
                    return;
                }
            }

            if (passwordEditTexts != null && passwordEditTexts[0] != null) {
                if (aMap != null && aMap.size() > 1) {
                    List<String> values = new ArrayList<>(aMap.values());
                    for (String ss : values) {
                        if (ss.equals(values.get(0)))
                            allEqual = allEqual && true;
                        else
                            allEqual = allEqual && false;
                    }
                } else if (aMap != null && aMap.size() > 0) {
                    passwordEditTexts[1].setError("Enter " + passwordEditTexts[1].getHint());
                    passwordEditTexts[1].requestFocus();
                    allEqual = false;
                    validationFlag = false;
                    return;
                } else {
                    passwordEditTexts[0].setError("Enter " + passwordEditTexts[0].getHint());
                    passwordEditTexts[0].requestFocus();
                    allEqual = false;
                    validationFlag = false;
                    return;
                }

                if (allEqual) {
                    passwordEditTexts[0].setError(null);
                    validationFlag = true;
                } else {
                    passwordEditTexts[0].setError(passwordEditTexts[0].getHint() + " Mismatch!");
                    passwordEditTexts[0].requestFocus();
                    validationFlag = false;
                    return;
                }
            }


            if (((Button) view).getHint().toString().equalsIgnoreCase("getBill")) {
                TRACE.i("validationFlag::::::" + validationFlag);
                RequestHandler handler = new RequestHandler(controller.getConnectionManager());
                StringBuilder request = new StringBuilder();
                request.append("userId=" + controller.getConnectionManager().getUserId()).append(SEPARATOR);
                request.append("sessionId=" + controller.getConnectionManager().getSessionId())
                        .append(SEPARATOR);
                request.append("latitude=" + controller.getLatitude()).append(
                        SEPARATOR);
                request.append("longitude=" + controller.getLongitude()).append(
                        SEPARATOR);
                StringBuilder validFields = new StringBuilder();

                StringBuilder entireCheckBox = new StringBuilder();
                if (cbArray != null) {
                    for (String s : cbArray
                            ) {
                        if (s != null)
                            entireCheckBox.append(s + ":");

                    }
                    jRequest.put(listOfCBValues[0], entireCheckBox.toString().replace(":", ""));
                }

                for (int i = 0; i < etVal; i++) {
                    if (isCallable[i] == 1) {
                        if (!editTexts[i].getText().toString().trim().equals("")
                                && editTexts[i].getText().toString().trim().length() >= editTexts[i].getMinEms()) {
                            validationFlag = validationFlag && true;
                            TRACE.i(editTexts[i].getHint() + ">>>>>>>" + editTexts[i].getText().toString() + "+++" + validationFlag);
                            if (!editTexts[i].getHint().toString().trim().equalsIgnoreCase("amount") && ((MEditText) editTexts[i]).getRequiredInReceipt() == 1)
                                jRequest.put(editTexts[i].getTag().toString(), editTexts[i].getText().toString());
                        } else if (editTexts[i].getMinEms() == 0) {
                            validationFlag = validationFlag && true;
                        } else {
                            validFields.append(editTexts[i].getHint().toString().trim() + ",");
                            validationFlag = validationFlag && false;
                        }
                        TRACE.i(editTexts[i].getHint() + ">>>>>>>" + editTexts[i].getText().toString() + validationFlag);

                    }
                }


                if (validationFlag) {
                    request.append("modAppServiceCalls=" + sortRequst(jRequest)).append(
                            SEPARATOR);
                    /*request.append("modAppServiceCalls=" + jRequest.toString()).append(
                            SEPARATOR);*/
                    TRACE.i("Request::" + request.toString());
                    String url = "";
                    try {
                        url = essentialObject.getString("modularAppUrl");
                        TRACE.i("url:::" + url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    controller.setModularRequest(jRequest);
                    controller.showProgressDialog("Fetching details...");
                    controller.getConnectionManager().setCurrentPostType(PostType.GET_DETAILS);
                    ArrayList<NameValuePair> list = handler.getRequest(PostType.GET_DETAILS, request.toString());
                    new PostTask(list, url).execute();
                } else {
                    validationFlag = false;
                    TRACE.i("validFields::::::::::::" + validFields.toString());
                    if (validFields.toString().trim().length() > 0)
                        controller.showDialog("Enter " + validFields.replace(validFields.length() - 1, validFields.length(), ""));
                    else
                        controller.showDialog("Enter complete details");
                    return;
                }
            } else if (((Button) view).getHint().toString().equalsIgnoreCase("charge")) {


                TRACE.i("----------" + controller.getLevelFlag());
                if (et_amount != null) {
                    controller.setEtAmount(et_amount);
                    amount = Double.valueOf(et_amount.getText().toString());
                } else {
                    amount = 1232.00;
                }
                controller.setAmount(getAmount());
                StringBuilder validFields = new StringBuilder();
                if (controller.getLevelFlag() == 2)
                    jRequest.putAll(controller.getModularRequest());
                for (int i = 0; i < etVal; i++) {
                    TRACE.i(">>>>>>" + editTexts[i].getHint().toString().trim() + ">>>>>>>>>>" + isCallable[i]);
                    if (isCallable[i] == 1 || editTexts[i].getHint().toString().trim().equalsIgnoreCase("amount")) {
                        try {
                            if (!editTexts[i].getText().toString().trim().equals("")
                                    && editTexts[i].getText().toString().trim().length() >= editTexts[i].getMinEms()) {
                                validationFlag = validationFlag && true;
                                TRACE.i(editTexts[i].getHint() + ">>>>>>>" + editTexts[i].getText().toString() + "+++" + validationFlag);

                                if (!editTexts[i].getHint().toString().trim().equalsIgnoreCase("amount") && ((MEditText) editTexts[i]).getRequiredInReceipt() == 1)
                                    jRequest.put(editTexts[i].getHint().toString().trim(), editTexts[i].getText().toString());

                            } else if (editTexts[i].getMinEms() == 0) {
                                validationFlag = validationFlag && true;
                            } else {
                                validFields.append(editTexts[i].getHint().toString().trim() + ",");
                                validationFlag = validationFlag && false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                StringBuilder entireCheckBox = new StringBuilder();
                if (cbArray != null) {
                    for (String s : cbArray
                            ) {
                        if (s != null)
                            entireCheckBox.append(s + ":");

                    }
                    jRequest.put("Charges", entireCheckBox.toString());
                }
                TRACE.i("+++++++" + validationFlag);
                TRACE.i(rVal + "+++++++" + controller.getType().getJsonObject());

                if (controller.getType().getJsonObject() != null) {

                    for (int i = 0; i < rVal; i++) {
                        try {
                            JSONObject json = new JSONObject(controller.getType().getJsonObject().getString("fetchServiceResponse"));
                            jRequest.put(receiptData[i], json.getString(receiptData[i]));
                            TRACE.i(receiptData[i] + ">>>>>>>" + json.getString(receiptData[i]));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }


                TRACE.i(jRequest.size() + "-------------" + controller.getLevelFlag());
                if (amount > 0.00 && validationFlag) {
                    validationFlag = true;
                    if (controller.getLevelFlag() != 1) {
                        try {
                            controller.setLevelFlag(2);
                            controller.setModularRequest(jRequest);
                            controller.setFetchServiceResponse(controller.getType().getJsonObject().getJSONObject("fetchServiceResponse").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            controller.showDialog("Lost the Details. Fetch again and try.");
                            controller.callToFragment("Modular");
                            controller.setLevelFlag(1);
                            return;
                        }
                    } else {
                        controller.setLevelFlag(2);
                        controller.setModularRequest(jRequest);
                    }

                    if (controller.getTPSA()) {
                        showTPSA_ListDialog(controller.getTpsaBusinessNameList(), "MERCHANT LIST");
                    } else
                        controller.showPairedDeviceListDialog();
                    if (et_amount != null) {
                        et_amount.setError(null);
                        et_amount.setSelection(et_amount.getText().length());
                    }
                    handleDeviceStatus();
                } else if (amount <= 0.00) {
                    validationFlag = false;
                    if (et_amount != null) {
                        et_amount.setError("Enter valid amount");
                        et_amount.requestFocus();
                    }
                } else {
                    validationFlag = false;
                    TRACE.i("validFields:::::::::" + validFields);
                    if (validFields.toString().trim().length() > 0)
                        controller.showDialog("Enter " + validFields.replace(validFields.length() - 1, validFields.length(), ""));
                    return;
                }

            } else if (((Button) view).getHint().toString().equalsIgnoreCase("Submit")) {
                //Toast.makeText(getActivity(), "clicked Submit", Toast.LENGTH_SHORT).show();
            } else if (((Button) view).getHint().toString().equalsIgnoreCase("TRY AGAIN!")) {
                controller.clearModularRequest();
                controller.post(PostType.FETCH_PARAMS, "Getting Parameters...");
            }
        }
    };

    public void showTPSA_ListDialog(String[] list, String title) {
        MListDialog listDialog = new MListDialog(getContext());
        listDialog.registerMListDialogListener(this);
        listDialog.show();
        listDialog.setTitle(title);
        listDialog.setList(list);
    }

    private String sortRequst(HashMap<String, String> modularRequest) {
        JSONObject requestStr = new JSONObject();
        try {
            Set<String> keys = modularRequest.keySet();
            for (String key : keys) {
                if (!key.equalsIgnoreCase("amount")) {
                    requestStr.put(key, modularRequest.get(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TRACE.i("Inside sortRequst---------------------" + modularRequest.size());
        return requestStr.toString();
    }

    Button chargeButton;

    private void handleDeviceStatus() {
        TRACE.i("Inside handledevice---------------------");
        for (int i = 0; i < buttonCount; i++) {
            if (buttons[i].getText().toString().equalsIgnoreCase("CHARGE"))
                chargeButton = buttons[i];
        }

        if (chargeButton != null) {
            TRACE.i("getType().getId()::::;" + getType().getId());
            if (controller.getCommunicationMode().toLowerCase().equals("bt") && !isBluetoothEnabled()) {
                disableChargeButton(chargeButton);
            } else if (!isValidCardTranscation()) {
                disableChargeButton(chargeButton);
            } else {
                enableChargeButton(chargeButton);
            }
        }
    }

    EditText et_amount;

    private boolean isValidCardTranscation() {
        Double amount = 0.00;
        for (int i = 0; i < editTextCount; i++) {
            if (editTexts[i].getHint().toString().equalsIgnoreCase("AMOUNT"))
                et_amount = editTexts[i];
        }
        if (et_amount != null)
            amount = Double.valueOf(et_amount.getText().toString());
        else
            amount = 0.00;

         /*sale+cashback logic done here
        Nitu:Process the transaction without entering the cashback amount solved*/
        String type = controller.getJsonValue(jsonObject, Controller.KEY_TRANSACTION_TYPE);

        if (!type.equals(Controller.TRANSACTION_TYPE_EMI) && !type.equals(Controller.TRANSACTION_TYPE_HDFC_EMI)) {
            //Double cashbackAmount = Double.valueOf(etCashback.getText().toString());
            Double cashbackAmount = Double.parseDouble(controller.getCashBackAmountOnButton().equals("NA") ? "0.00" : controller.getCashBackAmountOnButton());
            if (getType().getId() == 13 || getType().getId() == 7) {
                if (controller.getCashBackAmountOnButton().equals("NA"))
                    return true;
                else {
                    if (amount > 0 && cashbackAmount > 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        //end logic
        return amount > 0.00;
    }

    private boolean isBluetoothEnabled() {
        if (controller.isBluetoothEnabled()) {
            //showHint();
            return true;
        } else
            return false;
    }

    private void disableChargeButton(Button chargeButton) {
        chargeButton.setClickable(false);
        chargeButton.setBackgroundColor(getResources().getColor(R.color.charge_disabled));
    }

    private void enableChargeButton(Button chargeButton) {
        chargeButton.setClickable(true);
        //chargeButton.setBackgroundColor(Color.parseColor(controller.getPrimaryColor()));
        if(Build.VERSION.SDK_INT >= 21 ){
            chargeButton.setBackgroundResource(R.drawable.button_dialog_pressedfor_numberpad);
        }else{
            chargeButton.setBackgroundResource(R.drawable.button_dialog_pressedfor_numberpad_under21);
        }
    }

    private EditText etMobileNo;

    private boolean validateMobileNo() {
        for (int i = 0; i < editTextCount; i++) {
            if (editTexts[i].getHint().toString().equalsIgnoreCase("Mobile No"))
                etMobileNo = editTexts[i];
        }
        if (getMobileno().length() != 0 && !(controller.isValidMobile(getMobileno()))) {
            etMobileNo.requestFocus();
            etMobileNo.setError(getString(R.string.mobileno_not_valid));
            TRACE.i("Inside 2nd---------------------");
            return false;
        } else {
            etMobileNo.setError(null);
            return true;
        }
    }

    String temp[];
    private final String DEFAULT_AMOUNT = "0.00";

    public void createView(LinearLayout myLayout, JSONObject jsonView) {

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        try {
            if (jsonView.getString("view").equals("") || jsonView.getString("view") == (null)) {
                buttons = new Button[1];
                buttons[0] = new Button(getActivity());
                buttons[0].setText("TRY AGAIN!");
                buttons[0].setHint("TRY AGAIN!");
                buttons[0].setLayoutParams(lp);
                buttons[0].setGravity(Gravity.CENTER_HORIZONTAL);
                myLayout.addView(buttons[0]);
            } else {

                TRACE.i("::::::::::::::" + jsonView.getString("view"));
                switch (jsonView.getString("view")) {
                    case "EditText":
                        if (editTexts != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            isCallable[etVal] = Integer.parseInt(jsonView.getString("isCallable"));


                            if (jsonView.getString("hint").equalsIgnoreCase("amount"))
                                editTexts[etVal] = new EditTextAlwaysLast(getActivity());
                            else {
                                editTexts[etVal] = new MEditText(getActivity());
                                ((MEditText) editTexts[etVal]).setRequiredInReceipt(Integer.parseInt(jsonView.getString("requiredInReceipt")));
                            }
                            editTexts[etVal].setHint(jsonView.getString("hint"));
                            editTexts[etVal].setId(Integer.parseInt(jsonView.getString("viewId")));
                            editTexts[etVal].setTag(jsonView.getString("callingName"));
                            editTexts[etVal].setMinEms(Integer.parseInt(jsonView.getString("minLength")));
                            editTexts[etVal].setMaxEms(Integer.parseInt(jsonView.getString("maxLength")));
                            TRACE.i("getTag: " + editTexts[etVal].getTag());
                            if (!jsonView.getString("maxLength").equals("") || !(null == jsonView.getString("maxLength"))) {
                                editTexts[etVal].setFilters(new InputFilter[]{new InputFilter.LengthFilter(Integer.parseInt(jsonView.getString("maxLength")))});
                            }
                            editTexts[etVal].setTypeface(typeface1, Typeface.NORMAL);
                            editTexts[etVal].setLayoutParams(lp);
                            editTexts[etVal].setGravity(Gravity.CENTER_HORIZONTAL);

                            boolean editableFlag = jsonView.getString("isEditable").equals("1") ? true : false;
                            if (editableFlag) {
                                editTexts[etVal].setInputType(Integer.parseInt(jsonView.getString("inputType")));
                                if (Integer.parseInt(jsonView.getString("inputType")) == 128)
                                    passwordEditTexts[pwdGPVal].setTransformationMethod(PasswordTransformationMethod.getInstance());
                            } else
                                editTexts[etVal].setInputType(InputType.TYPE_NULL);

                            editTexts[etVal].setSelection(editTexts[etVal].getText().toString().length());
                            editTexts[etVal].setTextColor(getResources().getColor(R.color.black));
                            try {
                                Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                                f.setAccessible(true);
                                f.set(editTexts[etVal], R.color.black);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            }

                            if (jsonView.getString("hint").equalsIgnoreCase("amount")) {
                                editTexts[etVal].setText("");
                                et_amount = editTexts[etVal];
                                controller.setEtAmount(et_amount);
                                DecimalFormatSymbols dfs = new DecimalFormatSymbols(getResources().getConfiguration().locale);
                                separator = dfs.getDecimalSeparator();
                                regex = Pattern.compile("^(\\d{1,7}[" + separator + "]\\d{2}){1}$");
                                regexPaste = Pattern.compile("^([0]+\\d{1,6}[" + separator + "]\\d{2})$");
                                if (et_amount.getText().toString().equals(""))
                                    et_amount.setText("0" + separator + "00");

                                et_amount.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.rupee_sign), null);
                                et_amount.setSelection(et_amount.getText().length());
                                et_amount.addTextChangedListener(amountWatcher);
                            }
                            myLayout.addView(editTexts[etVal]);
                            etVal = etVal + 1;
                        }

                        break;

                    case "TextView":
                        if (textViews != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            textViews[txtVal] = new TextView(getActivity());
                            textViews[txtVal].setId(Integer.parseInt(jsonView.getString("viewId")));
                            textViews[txtVal].setText(jsonView.getString("displayName"));
                            textViews[txtVal].setLayoutParams(lp);
                            textViews[txtVal].setTextSize(14);
                            textViews[txtVal].setTextColor(getResources().getColor(R.color.black));
                            //textView.setTag(jsonView.getString("viewName"));
                            textViews[txtVal].setHint(jsonView.getString("hint"));
                            textViews[txtVal].setGravity(Gravity.NO_GRAVITY);
                            textViews[txtVal].setTypeface(typeface1, Typeface.BOLD);
                            textViews[txtVal].setPadding(10, 15, 10, 15);
                            myLayout.addView(textViews[txtVal]);
                            txtVal = txtVal + 1;
                        }

                        break;

                    case "Button":
                        if (buttons != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            buttons[btnVal] = new Button(getActivity());
                            buttons[btnVal].setId(Integer.parseInt(jsonView.getString("viewId")));
                            buttons[btnVal].setText(jsonView.getString("displayName").toUpperCase());
                            buttons[btnVal].setLayoutParams(lp);
                            buttons[btnVal].setClickable(true);
                            buttons[btnVal].setOnClickListener(this);
                            buttons[btnVal].setHint(jsonView.getString("hint"));

                            //button.setTag(jsonView.getString("viewName"));
                            buttons[btnVal].setTextSize(16);
                            buttons[btnVal].setTypeface(typeface1, Typeface.BOLD);
                            buttons[btnVal].setBackgroundColor(Color.parseColor(controller.getPrimaryColor()));
                            buttons[btnVal].setTextColor(getResources().getColor(R.color.black));
                            buttons[btnVal].setGravity(Gravity.CENTER);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 5, 0, 5);
                            buttons[btnVal].setLayoutParams(params);
                            btnVal = btnVal + 1;

                        }
                        break;

                    case "CheckBox":

                        if (checkBoxes != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            checkBoxes[cbVal] = new CheckBox(getActivity());
                            checkBoxes[cbVal].setId(Integer.parseInt(jsonView.getString("viewId")));
                            String temp[] = jsonView.getString("displayName").split(":");
                            listOfCBValues[cbVal] = temp[0];
                            //TRACE.i(cbVal+"###"+  listOfCBValues[cbVal]+":::::"+temp[0]);
                            textView = new TextView(getActivity());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 5, 0, 5);
                            if (cbVal == 0) {
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                            } else if (cbVal > 0 && !temp[0].equals(listOfCBValues[cbVal - 1])) {
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                            }

                            checkBoxes[cbVal].setTextColor(getResources().getColor(R.color.black));
                            checkBoxes[cbVal].setMinEms(Integer.parseInt(jsonView.getString("requiredInReceipt")));
                            checkBoxes[cbVal].setId(Integer.parseInt(jsonView.getString("isAmount")));
                            checkBoxes[cbVal].setTag(jsonView.getString("callingName"));
                            if (Integer.parseInt(jsonView.getString("isAmount")) == 1)
                                checkBoxes[cbVal].setText(temp[1].toUpperCase() + "\n\tRs." + jsonView.getString("callingName"));
                            else
                                checkBoxes[cbVal].setText(temp[1].toUpperCase());
                            checkBoxes[cbVal].setLayoutParams(lp);
                            checkBoxes[cbVal].setClickable(true);
                            checkBoxes[cbVal].setOnCheckedChangeListener(checkEvent);
                            checkBoxes[cbVal].setGravity(Gravity.LEFT);
                            myLayout.addView(checkBoxes[cbVal]);

                            cbVal = cbVal + 1;
                        }
                        break;

                    case "RadioGroup":
                        if (radioGroups != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            String temp[] = jsonView.getString("displayName").split(":");
                            listOfRBValues[rgBtnVal] = temp[0];
                            TRACE.i(rgBtnVal + " listOfRBValues[rgBtnVal] :::::::::" + listOfRBValues[rgBtnVal]);
                            layoutRadioGroup = new LinearLayout(getActivity());
                            layoutRadioGroup.setVisibility(View.VISIBLE);
                            textView = new TextView(getActivity());
                            listRadioGroup = stringToArray(jsonView.getString("displayName"));
                            textView.setText(listRadioGroup[0]);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 5, 0, 5);
                            if (rgBtnVal == 0) {
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                                selectedStatus = 0;
                                radioGroup = new RadioGroup(getActivity());
                                radioGroup.setOrientation(LinearLayout.VERTICAL);
                            } else if (rgBtnVal > 0 && !temp[0].equals(listOfRBValues[rgBtnVal - 1])) {
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                                selectedStatus = 0;
                                radioGroup = new RadioGroup(getActivity());
                                radioGroup.setOrientation(LinearLayout.VERTICAL);
                            }

                            for (int i = 1; i < listRadioGroup.length; i++) {
                                radioButton = new RadioButton(getActivity());
                                radioButton.setId(rgBtnVal);
                                radioButton.setTextColor(getResources().getColor(R.color.black));
                                radioButton.setMinEms(Integer.parseInt(jsonView.getString("requiredInReceipt")));
                                radioButton.setTag(jsonView.getString("callingName"));
                                radioButton.setText(temp[1]);
                                TRACE.i("--------" + radioGroup);
                                TRACE.i(radioButton.getText() + "set id::::" + radioButton.getId());

                                if (selectedStatus == 0) {
                                    radioButton.setChecked(true);
                                    jRequest.put(listOfRBValues[0], radioButton.getTag().toString());
                                    selectedStatus = selectedStatus + 1;
                                }
                                radioGroup.addView(radioButton);
                            }

                            if (rgBtnVal == radioGroupCount - 1) {

                                radioGroups[rgBtnVal] = radioGroup;
                                radioGroups[rgBtnVal].setId(Integer.parseInt(jsonView.getString("viewId")));
                                myLayout.addView(radioGroup);

                            }
                            rgBtnVal = rgBtnVal + 1;
                        }
                        break;

                    case "PasswordGroup":
                        if (passwordEditTexts != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            String temp[] = jsonView.getString("displayName").split(":");
                            listOfpasswordGPValues[pwdGPVal] = temp[0];
                            TRACE.i(pwdGPVal + " listOfRBValues[pwdGPVal] :::::::::" + listOfpasswordGPValues[pwdGPVal]);
                            layoutpasswordGroup = new LinearLayout(getActivity());
                            layoutpasswordGroup.setVisibility(View.VISIBLE);
                            textView = new TextView(getActivity());
                            listpasswordGroup = stringToArray(jsonView.getString("displayName"));
                            textView.setText(listpasswordGroup[0]);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 5, 0, 5);
                            if (pwdGPVal == 0) {
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                            } else if (pwdGPVal > 0 && !temp[0].equals(listOfpasswordGPValues[pwdGPVal - 1])) {
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                            }

                            for (int i = 1; i < listpasswordGroup.length; i++) {
                                passwordEditTexts[pwdGPVal] = new MEditText(getActivity());
                                passwordEditTexts[pwdGPVal].setId(pwdGPVal);
                                TRACE.i(passwordEditTexts[pwdGPVal] + "set id::::" + pwdGPVal);
                                passwordEditTexts[pwdGPVal].setTextColor(getResources().getColor(R.color.black));
                                passwordEditTexts[pwdGPVal].setTag(jsonView.getString("callingName"));
                                //passwordEditTexts[i].setText(temp[1]);
                                passwordEditTexts[pwdGPVal].setMinEms(Integer.parseInt(jsonView.getString("minLength")));
                                passwordEditTexts[pwdGPVal].setMaxEms(Integer.parseInt(jsonView.getString("maxLength")));
                                TRACE.i("--------" + selectedStatus);
                                passwordEditTexts[pwdGPVal].setInputType(Integer.parseInt(jsonView.getString("inputType")));
                                if (Integer.parseInt(jsonView.getString("inputType")) == 128 && pwdGPVal != 0)
                                    passwordEditTexts[pwdGPVal].setTransformationMethod(PasswordTransformationMethod.getInstance());
                                passwordEditTexts[pwdGPVal].setHint(temp[1]);
                                passwordEditTexts[pwdGPVal].setTypeface(typeface1, Typeface.NORMAL);
                                passwordEditTexts[pwdGPVal].setLayoutParams(lp);
                                passwordEditTexts[pwdGPVal].setGravity(Gravity.CENTER_HORIZONTAL);
                                passwordEditTexts[pwdGPVal].setSelection(passwordEditTexts[pwdGPVal].getText().toString().length());
                                passwordEditTexts[pwdGPVal].setTextColor(getResources().getColor(R.color.black));
                                passwordEditTexts[pwdGPVal].addTextChangedListener(pswdTextWatcher);
                                passwordEditTexts[pwdGPVal].setFilters(new InputFilter[]{new InputFilter.LengthFilter(Integer.parseInt(jsonView.getString("maxLength")))});
                                try {
                                    Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                                    f.setAccessible(true);
                                    f.set(passwordEditTexts[pwdGPVal], R.color.black);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                                myLayout.addView(passwordEditTexts[pwdGPVal]);
                            }

                            pwdGPVal = pwdGPVal + 1;
                        }
                        break;

                    case "DropDown":
                        if (spinners != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag()) {
                            String temp[] = jsonView.getString("displayName").split(":");
                            TRACE.i("spnVal=" + spnVal + "         temp[0]=" + temp[0] + "         temp[1]=" + temp[1]);


                            textView = new TextView(getActivity());
                            listSpinner = stringToArray(jsonView.getString("displayName"));
                            for (int i = 0; i < listSpinner.length; i++) {
                                if (i != 0)
                                    listSpinner[i] = listSpinner[i].replace("," + temp[0], "");
                            }

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 5, 0, 5);
                            /*if(spnVal==0){
                                textView.setText(temp[0]);
                                textView.setLayoutParams(params);
                                myLayout.addView(textView);
                                spinnerDropDown = new Spinner(getActivity());
                            }else*/

                            textView.setText(temp[0]);
                            textView.setLayoutParams(params);
                            myLayout.addView(textView);
                            spinnerDropDown = new Spinner(getActivity());
                            spinnerDropDown.setId(Integer.parseInt(jsonView.getString("isAmount")));
                            spinnerDropDown.setTag(jsonView.getString("callingName"));
                            spinnerDropDown.setClickable(true);
                            spinnerDropDown.setOnItemSelectedListener(dropDownEvent);
                            spinnerDropDown.setGravity(Gravity.LEFT);


                            arrayAdapterDropDown = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, listSpinner);
                            spinnerDropDown.setAdapter(arrayAdapterDropDown);
                            spinners[spnVal] = spinnerDropDown;
                            myLayout.addView(spinnerDropDown);
                            spnVal = spnVal + 1;

                            // createDropDown(jsonView, listSpinner);


                        }
                        break;

                    case "Fixed":
                        if (receiptData != null && Integer.parseInt(jsonView.getString("urlNumber")) == controller.getLevelFlag() && jsonView.getString("requiredInReceipt").equals("1")) {
                            TRACE.i(":::::::::::::" + jsonView.getString("displayName"));
                            receiptData[rVal] = new String(jsonView.getString("displayName"));
                            TRACE.i("receiptData[rVal]:::::::::::::" + receiptData[rVal] + "requiredInreceipt::" + jsonView.getString("requiredInReceipt"));
                            rVal = rVal + 1;
                        }
                        break;

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createDropDown(JSONObject jsonView, String[] listOfSpinnerValue) {
        try {

            TRACE.i("Inside createDropDown---------");
            spinnerDropDown.setFadingEdgeLength(Integer.parseInt(jsonView.getString("requiredInReceipt")));
            spinnerDropDown.setId(Integer.parseInt(jsonView.getString("isAmount")));
            spinnerDropDown.setTag(jsonView.getString("callingName"));
            spinnerDropDown.setLayoutParams(lp);
            spinnerDropDown.setClickable(true);
            spinnerDropDown.setOnItemSelectedListener(dropDownEvent);
            spinnerDropDown.setGravity(Gravity.LEFT);

            arrayAdapterDropDown = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_activated_1, listOfSpinnerValue);
            spinnerDropDown.setAdapter(arrayAdapterDropDown);
            spinnerDropDown.setGravity(Gravity.NO_GRAVITY);

            spinners[spnVal] = spinnerDropDown;
            myLayout.addView(spinnerDropDown);
            spnVal = spnVal + 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
    }

    public String[] stringToArray(String value) {
        return value.split(":");
    }

    public Boolean isValidMobileNumber(EditText editText) {
        if (editText.getText().toString().trim().length() != 10) {
            return false;
        } else {
            return true;
        }
    }

    private String validationPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public Boolean isValidEmail(EditText editText) {
        String inputText = editText.getText().toString();
        /*if (validationPattern == null) {
            return false;
        }*/
        return Pattern.compile(validationPattern).matcher(inputText).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        LockableActivity.resetLockTimer();
        return super.onTouch(v, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        controller.unRegisterResponseListener();
    }

    @Override
    public String getAmount() {
        if (et_amount != null)
            return et_amount.getText().toString();
        else
            return amount + "";
    }

    @Override
    public String getInvoice() {
        return etInvoice.getText().toString();
    }

    public String getMobileno() {
        return etMobileNo.getText().toString();
    }

    @Override
    public void handleInvalidTranscation() {
        controller.showDialog(getString(R.string.invalid_amount));
        controller.startReader();
    }

    @Override
    public void onReaderPlugIn() {
    }

    @Override
    public void onReaderPlugOut() {
    }

    @Override
    public void onSuccess(PostType postType) {
    }

    @Override
    public void onFailure(PostType postType) {
        controller.startReader();
    }

    @Override
    public void onMEditTextActionDone() {
    }

    @Override
    public void onMEditTextBackPressed() {
    }

    @Override
    public void handleInput(String input) {
    }

    @Override
    public void handleBackspace() {
    }

    @Override
    public void handleReservedButton() {
    }

    @Override
    public void handleDoneButton() {
    }

    @Override
    public void handleGpsDisabled() {
    }

    @Override
    public void handleOkAction(String btnName) {
    }

    @Override
    public void handleCancelAction() {
        controller.startReader();
    }

    @Override
    public void onAlertConfirmation() {
    }

    @Override
    public void handleLongBackspace() {
    }

    @Override
    public void onItemClick(BluetoothDevice bluetoothDevice) {
    }

    @Override
    public void onItemClickString(String type) {
    }

    @Override
    public void onItemClickTpsaBusiness(String s, int position) {
        if (controller.getTPSA()) {
            controller.setTpsaMDATId(controller.getTpsaMDATIdList()[position]);
            controller.showPairedDeviceListDialog();
        }
    }

    @Override
    public void handleScannedData(String data) {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
    }
}
