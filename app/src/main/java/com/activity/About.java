package com.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mosambee.mpos.cpoc.R;

/**
 * its about above the class
 * 
 * 
 * */
public class About extends ActiveFragment implements OnTouchListener {
	
    TextView tvLic, tvVersion, tvReleaseNotes;
    private View v;
    private LinearLayout contentLinearLayout;
    private View s;
    //private Button btnStartScan, btnStopScan;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.about, container, false);
        controller = (Controller) getActivity().getApplication();
        controller.getTransactionTextView().setText("ABOUT");
        // Remove notification bar
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
    }

    public void initialize() {
        /*contentLinearLayout = (LinearLayout)v.findViewById(R.id.contentLinearLayout);
        contentLinearLayout.setOnTouchListener(this);*/
        tvVersion = (TextView) v.findViewById(R.id.tv_version);
        tvLic = (TextView) v.findViewById(R.id.tv_lic);
        tvVersion.setText("Mosambee  Application Version  "+controller.getAppVersion());

        tvReleaseNotes= (TextView) v.findViewById(R.id.tvReleaseNotes);

        tvReleaseNotes.setText("Whats New: \nMajor App Changes: NA \nEnhancements: CPOC Device Support Added.");


        String aboutString="Copyright \u00A9 Synergistic Financial Networks Pvt Ltd 2019. All rights reserved.";
        if(!getString(R.string.app_name).equalsIgnoreCase("mosambee"))
            aboutString+="\n"+getString(R.string.app_name);
        tvLic.setText(aboutString);

        //resetLockTimer();

        LockableActivity.resetLockTimer();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Toast.makeText(getActivity(), "onTouch", Toast.LENGTH_SHORT).show();
       /* if(v == contentLinearLayout){
            //resetLockTimer();
            LockableActivity.resetLockTimer();
        }*/
        return true;
    }
}
