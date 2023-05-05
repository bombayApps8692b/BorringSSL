package com.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.mosambee.mpos.cpoc.R;

public class MActionDialogForTerms extends Dialog implements OnClickListener {
    private Context context;

    private MActionDialogListener dialogListener;

    private TextView title;
    private WebView webview;
    private Button cancelButton;
    private Button okButton;

    public MActionDialogForTerms(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.action_dialog_terms);
        setCancelable(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        title = (TextView) findViewById(R.id.title);

        webview = (WebView) findViewById(R.id.message);



        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.setWebViewClient(new MyBrowser());
        webview.getSettings().setPluginState(WebSettings.PluginState.ON);




        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);



        webview.getSettings().setAllowFileAccessFromFileURLs(true);
        webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setWebChromeClient(new WebChromeClient());





        webview.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if(event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    WebView webView = (WebView) v;

                    switch(keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }

                return false;
            }
        });








        cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(this);

        okButton = (Button) findViewById(R.id.button_ok);
        okButton.setOnClickListener(this);
        okButton.requestFocus();

        if(Build.VERSION.SDK_INT >= 21 ){
            okButton.setBackgroundResource(R.drawable.button_dialog);
            cancelButton.setBackgroundResource(R.drawable.button_dialog);
        }else{
            okButton.setBackgroundResource(R.drawable.button_dialog_under21);
            cancelButton.setBackgroundResource(R.drawable.button_dialog_under21);
        }

        Typeface typeface1= Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        cancelButton.setTypeface(typeface1);
        okButton.setTypeface(typeface1);
    }

    public void registerMActionDialogListener(MActionDialogListener listener) {
        dialogListener = listener;
    }


    class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            view.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void performClick() throws Exception {

                }
            }, "login");
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub

            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);

        }

    }

    @Override
    public void onClick(View v) {
        if (v == okButton) {
            dialogListener.handleOkAction(okButton.getText().toString());
        } else if(v == cancelButton) {
            dialogListener.handleCancelAction();
        }

        dismiss();
    }

 /*   @Override
    protected void onStop() {
        super.onStop();
        dialogListener.handleCancelAction();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dialogListener.handleCancelAction();
    }*/

    public void setTitle(int titleId) {
        title.setText(context.getString(titleId));
    }

    public void setMessage(String urlAddress) {
        webview.loadUrl(urlAddress);
    }

    public void setOkButtonText(String buttonTextId) {
        okButton.setText(buttonTextId);
    }
    public void setCancelButtonText(String buttonTextId) {
        cancelButton.setText(buttonTextId);
    }
}

