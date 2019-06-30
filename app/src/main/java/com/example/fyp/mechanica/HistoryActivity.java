package com.example.fyp.mechanica;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.fyp.mechanica.adapters.HistoryListAdapter;
import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.ActiveJob;
import com.example.fyp.mechanica.models.DoneJob;
import com.example.fyp.mechanica.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class HistoryActivity extends BaseDrawerActivity {

    @BindView(R.id.lv_history) ListView listView;

    User currUser;
    DatabaseReference dbRef;

    HistoryListAdapter adapter;
    List<DoneJob> jobs;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle("History");
        }

        currUser = Paper.book().read(Constants.CURR_USER_KEY);
        dbRef = FirebaseDatabase.getInstance().getReference();

        jobs = new ArrayList<>();

        dbRef.child("completedJobs").child(currUser.id)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    jobs.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DoneJob doneJob = snapshot.getValue(DoneJob.class);
//                        if (currUser.id.equals(snapshot.getKey())) {
                            jobs.add(doneJob);

                            if (doneJob != null) {

                                if (currUser.userRole.equals("Mechanic")) {
                                    getUser( doneJob.customerUID);

                                } else {
                                    getUser(doneJob.mechanicUID);
                                }

                            }
//                        }
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new HistoryListAdapter(this, jobs);
        listView.setAdapter(adapter);

    }

    public void getUser(String uid) {
        dbRef.child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
