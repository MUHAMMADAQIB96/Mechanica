package com.example.fyp.mechanica.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fyp.mechanica.R;
import com.example.fyp.mechanica.models.DoneJob;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryListAdapter extends ArrayAdapter {

    private List<DoneJob> jobs;
    Context context;

    public HistoryListAdapter(@NonNull Context context, List<DoneJob> jobs) {
        super(context, R.layout.history_list_item, jobs);
        this.jobs = jobs;
        this.context = context;

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

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}
