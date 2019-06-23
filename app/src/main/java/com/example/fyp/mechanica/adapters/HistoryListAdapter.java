package com.example.fyp.mechanica.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.mechanica.R;
import com.example.fyp.mechanica.models.DoneJob;
import com.example.fyp.mechanica.models.User;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryListAdapter extends ArrayAdapter {

    private List<DoneJob> jobs;
    private Context context;
    private User user;

    public HistoryListAdapter(@NonNull Context context, List<DoneJob> jobs, User user) {
        super(context, R.layout.history_list_item, jobs);
        this.jobs = jobs;
        this.context = context;
        this.user = user;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        View view = View.inflate(context, R.layout.history_list_item, null);
        TextView tvPKR = view.findViewById(R.id.tv_pkr);
        TextView tvDateTime = view.findViewById(R.id.tv_date_time);
        TextView tvName = view.findViewById(R.id.tv_username);
        CircleImageView civUserPhoto = view.findViewById(R.id.civ_user_photo);
        TextView tvAddress = view.findViewById(R.id.tv_location);

        DoneJob job = jobs.get(position);

        tvName.setText(user.name);
        tvPKR.setText("PKR "+ getPKR(job.startedAt, job.endedAt));

        Date date = new Date(job.endedAt);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
        tvDateTime.setText(String.valueOf(dateFormat.format(date)));

        getAddress(job.customerLat, job.customerLng, tvAddress);

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    private int getPKR(long start, long end) {
        long jobDuration = end - start;
        return (int) ((jobDuration / 1000) / 60) * 5;
    }

    private void getAddress(double lat, double lng, TextView tvAddress) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            tvAddress.setText(add);

//            add = add + "\n" + obj.getCountryName();
//            add = add + "\n" + obj.getCountryCode();
//            add = add + "\n" + obj.getAdminArea();
//            add = add + "\n" + obj.getPostalCode();
//            add = add + "\n" + obj.getSubAdminArea();
//            add = add + "\n" + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
