package com.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mosambee.mpos.cpoc.R;

import java.util.ArrayList;

public class EMIUserAdapter extends ArrayAdapter<EMIUser> {
    // View lookup cache
    private static class ViewHolder {
        TextView emiTenure;
        TextView bankInterestRate;
        TextView monthlyInstallment;
        TextView cashBackAmt;
        TextView totalAmount;
       
    }

    public EMIUserAdapter(Context context, ArrayList<EMIUser> emiusers) {
       super(context, R.layout.emi_details, emiusers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       // Get the data item for this position
       EMIUser emiuser = getItem(position);
       // Check if an existing view is being reused, otherwise inflate the view
       ViewHolder viewHolder; // view lookup cache stored in tag
       if (convertView == null) {
          viewHolder = new ViewHolder();
          LayoutInflater inflater = LayoutInflater.from(getContext());
          convertView = inflater.inflate(R.layout.emi_details, parent, false);
          viewHolder.emiTenure = (TextView) convertView.findViewById(R.id.tv_emi_tenure);
          viewHolder.bankInterestRate = (TextView) convertView.findViewById(R.id.tv_bank_interest_rate);
          viewHolder.monthlyInstallment = (TextView) convertView.findViewById(R.id.tv_monthly_installments);
         viewHolder.cashBackAmt = (TextView) convertView.findViewById(R.id.tv_cashbackAmt);
          viewHolder.totalAmount = (TextView) convertView.findViewById(R.id.tv_total_amount);
         
          
          convertView.setTag(viewHolder);
       } else {
           viewHolder = (ViewHolder) convertView.getTag();
       }
       
       // Populate the data into the template view using the data object
       //viewHolder.emiTenure.setText(emiuser.getEmiTenure());
        viewHolder.emiTenure.setText(emiuser.getEmiTenure()+" months");
       viewHolder.bankInterestRate.setText(emiuser.getBankInterestRate());
       viewHolder.monthlyInstallment.setText(String.valueOf(emiuser.getMonthlyInstallment()));
   viewHolder.cashBackAmt.setText(emiuser.getCashBackAmt());
       viewHolder.totalAmount.setText(emiuser.getTotalAmount());
       
       // Return the completed view to render on screen
       return convertView;
   }
}
