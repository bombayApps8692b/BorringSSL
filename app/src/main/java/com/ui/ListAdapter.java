package com.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.Data;
import com.data.TRACE;
import com.mosambee.mpos.cpoc.R;

public class ListAdapter extends ArrayAdapter<Data> {

    int layoutResourceId;
    Context context;
    Data data[] = null;
    private int imgId;

    public ListAdapter(Context context, int l, Data[] data) {
        super(context, l, data);
        this.context = context;
        layoutResourceId = l;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.txtSummary);
            holder.txtType = (TextView) row.findViewById(R.id.textViewtype);
            holder.txtCount = (TextView) row.findViewById(R.id.textViewcount);
            holder.txtAmount = (TextView) row.findViewById(R.id.textViewamount);
            holder.imgHost = (ImageView) row.findViewById(R.id.imgHost);
            row.setTag(holder);
        } else {
            holder = (WeatherHolder) row.getTag();
        }

        Data weather = data[position];

        holder.txtTitle.setText(weather.title);

        TRACE.i("ACTION TYPE::::::::::"+weather.action);

        if (weather.type.equals("NA"))
            holder.txtType.setVisibility(View.GONE);
        else {
//                holder.txtType.setText((Html.fromHtml(weather.type)));
                holder.txtType.setText(weather.type);
                holder.txtType.setVisibility(View.VISIBLE);
        }

        if (weather.count.equals("NA"))
            holder.txtCount.setVisibility(View.GONE);
        else {
//            holder.txtCount.setText((Html.fromHtml(weather.count)));
            holder.txtCount.setVisibility(View.VISIBLE);
            holder.txtCount.setText(weather.count);
        }

        if (weather.amount.equals("NA"))
            holder.txtAmount.setVisibility(View.GONE);
        else {
            holder.txtAmount.setText(weather.amount);
//            holder.txtAmount.setText((Html.fromHtml(weather.amount)));
            holder.txtAmount.setVisibility(View.VISIBLE);
        }

        TRACE.i("weather.host:::::-" + weather.host);
        if (weather.host.equals("NA")) {
            holder.imgHost.setVisibility(View.GONE);

        } else {
            holder.imgHost.setImageResource(getBitmapFor(weather.host));
            holder.imgHost.setVisibility(View.VISIBLE);
        }


        return row;
    }

    protected int getBitmapFor(String name) {
        if (name.equalsIgnoreCase("MASTERCARD")) {
            imgId = R.drawable.master_card;
        } else if (name.equalsIgnoreCase("AMEX")) {
            imgId = R.drawable.amex_logo;
        } else if (name.equalsIgnoreCase("VISA")) {
            imgId = R.drawable.visa;
        } else if (name.equalsIgnoreCase("MAESTRO")) {
            imgId = R.drawable.maestro;
        } else if (name.equalsIgnoreCase("HDFC") || name.equalsIgnoreCase("HDFC3M") || name.equalsIgnoreCase("HDFC6M") || name.equalsIgnoreCase("HDFC9M") || name.equalsIgnoreCase("HDFC12M") || name.equalsIgnoreCase("HDFC18M") || name.equalsIgnoreCase("HDFC24M")) {
            imgId = R.drawable.hdfc_logo1;
        } else if (name.equalsIgnoreCase("DINERS")) {
            imgId = R.drawable.dinners_logo;
        } else if (name.equalsIgnoreCase("ATOS") || name.equalsIgnoreCase("YES")) {
            imgId = R.drawable.yesbank_logo;
        } else if (name.equalsIgnoreCase("PNB")) {
            imgId = R.drawable.pnb_logo;
        } else if (name.equalsIgnoreCase("IDBI")) {
            imgId = R.drawable.idbi_logo;
        } else if (name.equalsIgnoreCase("UBI")) {
            imgId = R.drawable.ubi_logo;
        } else if (name.equalsIgnoreCase("GP")) {
            imgId = R.drawable.global_payment_logo;
        } else if (name.equalsIgnoreCase("RUPAY")) {
            imgId = R.drawable.rupay_logo;
        } else if (name.equalsIgnoreCase("BOB")) {
            imgId = R.drawable.bank_of_baroda;
        } else if (name.equalsIgnoreCase("JNK")) {
            imgId = R.drawable.jk_bank;
        } else if (name.equalsIgnoreCase("BOI")) {
            imgId = R.drawable.boi;
        } else if (name.equalsIgnoreCase("RBL")) {
            imgId = R.drawable.rbl;
        } else if (name.equalsIgnoreCase("UNITED")) {
            imgId = R.drawable.united;
        } else if (name.equalsIgnoreCase("ICICI")) {
            imgId = R.drawable.icici;
        } else if (name.equalsIgnoreCase("AXIS")) {
            imgId = R.drawable.axis;
        } else if (name.equalsIgnoreCase("KOTAK")) {
            imgId = R.drawable.kotak;
        } else if (name.equalsIgnoreCase("Mosambee") || name.equalsIgnoreCase("NA")) {
            imgId = R.drawable.mosambee_logo;
        } else if (name.equalsIgnoreCase("PP")) {
            imgId = R.drawable.pp_logo;
        }
        if (name.equalsIgnoreCase("")) {
            imgId = R.drawable.ic_icon;
        }
        return imgId;
    }


    static class WeatherHolder {
        TextView txtType;
        TextView txtCount;
        TextView txtAmount;
        TextView txtTitle;
        ImageView imgHost;
    }


}
