package com.mosambee;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mosambee.mpos.cpoc.R;

import org.signal.aesgcmprovider.OpenSSLApiLibrary;

public class MainActivity extends AppCompatActivity {

    TextView tvRandom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ssl);

        tvRandom = findViewById(R.id.tvRandom);




    }

    public void getRandomVal(View view) {

        OpenSSLApiLibrary openSSLApiLibrary = new OpenSSLApiLibrary();

        String res = openSSLApiLibrary.getDRBGRandomNumber("narayan".getBytes());

tvRandom.setText(res + "  "+res.getBytes().length);
    }
}
