package com.example.fyp.mechanica;

import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.ActiveJob;
import com.example.fyp.mechanica.models.DoneJob;
import com.example.fyp.mechanica.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class JobStartedActivity extends BaseDrawerActivity {

    @BindView(R.id.tvTimer) TextView tvTimer;
    @BindView(R.id.btn_done) Button btnDone;
    @BindView(R.id.ll_rating_card) LinearLayout llRatingCard;

    @BindView(R.id.civ_user_photo) CircleImageView civUserPhoto;
    @BindView(R.id.tv_date_time) TextView tvDateTime;
    @BindView(R.id.tv_pkr) TextView tvPKR;
    @BindView(R.id.tv_experience) TextView tvExperience;
    @BindView(R.id.rating_bar) RatingBar ratingBar;

    long startTime, timeInMilliseconds = 0;
    Handler customHandler = new Handler();

    ActionBar bar;
    User currUser;
    ActiveJob job;
    String jobId;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_started);
        ButterKnife.bind(this);

        currUser = Paper.book().read(Constants.CURR_USER_KEY);
        job = (ActiveJob) getIntent().getSerializableExtra("JOB");
        jobId = getIntent().getStringExtra("JOB_ID");

        dbRef = FirebaseDatabase.getInstance().getReference();

        bar = getSupportActionBar();
        if (bar != null) {
            if (currUser.userRole.equals("Mechanic")) {
                bar.setTitle("Work in Progress...");

            } else {
                bar.setTitle("Mechanic is Working");
            }
        }

        startJobTime();
    }

    public static String getDateFromMillis(long d) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }

    public void startJobTime() {
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    @OnClick(R.id.btn_done)
    public void done(View v) {
        customHandler.removeCallbacks(updateTimerThread);

        bar.setTitle("Job Done");
        llRatingCard.setVisibility(View.VISIBLE);

        if (jobId != null && job != null) {

            DoneJob doneJob = new DoneJob();
            doneJob.customerLat = job.cusLat;
            doneJob.customerLng = job.cusLon;
            doneJob.customerUID = job.customerID;
            doneJob.endedAt = (new Date()).getTime();
            doneJob.mechanicUID = job.mechanicID;
            doneJob.startedAt = job.startedAt;

            if (currUser.id.equals(job.mechanicID)) {
                showRatingCard(job.customerID);

            } else {
                showRatingCard(job.mechanicID);
            }

            dbRef.child("completedJobs").child(job.customerID).push().setValue(doneJob);
            dbRef.child("completedJobs").child(job.mechanicID).push().setValue(doneJob);

            dbRef.child("activeJobs").child(jobId).removeValue();

        }

        long pkr = ((timeInMilliseconds / 1000) / 60) * 5;
        tvPKR.setText(pkr + " PKR");

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        tvDateTime.setText(currentDateTimeString);


    }

    public void showRatingCard(String uid) {
        dbRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    tvExperience.setText("How was your experience with Mr. " + user.name + " ?");


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            tvTimer.setText(getDateFromMillis(timeInMilliseconds));
            customHandler.postDelayed(this, 1000);
        }
    };
}