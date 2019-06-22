package com.example.fyp.mechanica;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.ActiveJob;
import com.example.fyp.mechanica.models.DoneJob;
import com.example.fyp.mechanica.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.lv_history) ListView listView;

    User currUser;
    DatabaseReference dbRef;

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

        dbRef.child("completedJobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DoneJob doneJob = snapshot.getValue(DoneJob.class);
                        if (currUser.id.equals(snapshot.getKey())) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
