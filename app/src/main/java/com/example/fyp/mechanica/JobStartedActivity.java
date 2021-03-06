package com.example.fyp.mechanica;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
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
import com.example.fyp.mechanica.models.UserRating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
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
    @BindView(R.id.btn_ok) Button btnOK;

    long startTime, timeInMilliseconds = 0;
    Handler customHandler = new Handler();

    ActionBar bar;
    User currUser;
    ActiveJob job;
    String jobId;

    DatabaseReference dbRef;

    ActiveJob activeJob;
    String activeJobId;

    float currRating;

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

//        if (!currUser.userRole.equals("Mechanic")) {
//            btnDone.setVisibility(View.GONE);
//        }

        getJobStatus();
        startJobTime();
        ratingBarChange();

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
    public void done() {
        customHandler.removeCallbacks(updateTimerThread);

        bar.setTitle("Job Done");
        btnDone.setEnabled(false);
        llRatingCard.setVisibility(View.VISIBLE);

        if (jobId != null && job != null) {
            getActiveJob(job, jobId);
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
            dbRef.child("completedJobs").child(job.mechanicID).push().setValue(doneJob)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    dbRef.child("activeJobs").child(jobId).removeValue();
                }
            });

        } else {
            getActiveJob(activeJob, activeJobId);
        }

        long pkr = ((timeInMilliseconds / 1000) / 60) * Constants.PKR;
        tvPKR.setText("PKR "+ pkr);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        tvDateTime.setText(currentDateTimeString);
    }

    @OnClick(R.id.btn_ok)
    public void OK() {
        if (currUser.userRole.equals("Mechanic")) {
            Intent intent = new Intent(this, MechanicMapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(this, MapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void getActiveJob(ActiveJob job, final String jobId) {
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
        dbRef.child("completedJobs").child(job.mechanicID).push().setValue(doneJob)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        dbRef.child("activeJobs").child(jobId).removeValue();
                    }
                });
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


    public void getJobStatus() {
        dbRef.child("activeJobs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ActiveJob job = snapshot.getValue(ActiveJob.class);
                        if (job != null && (job.mechanicID.equals(currUser.id) || job.customerID.equals(currUser.id))) {
                            activeJob = job;
                            activeJobId = snapshot.getKey();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            tvTimer.setText(getDateFromMillis(timeInMilliseconds));
            customHandler.postDelayed(this, 1000);
        }
    };


    public void ratingBarChange() {
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                currRating = v;
                if (currUser.id.equals(job.customerID)) {
                    setRating(job.mechanicID, v);

                } else {
                    setRating(job.customerID, v);
                }

            }

        });


    }

    public void setRating(String uid, final float v) {
        dbRef.child("ratings").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserRating rating = dataSnapshot.getValue(UserRating.class);
                    if (rating != null) {
                        float newRating =  calculateRating(rating.count, rating.rating, v);

                        UserRating userRating = new UserRating();
                        userRating.count = rating.count + 1;
                        userRating.rating = newRating;

                        dbRef.child("ratings").child(currUser.id).setValue(userRating);
                    }
                }

                else {
                    UserRating userRating = new UserRating();
                    userRating.count = 1;
                    userRating.rating = currRating;

                    dbRef.child("ratings").child(currUser.id).setValue(userRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public float calculateRating(int count, float overAllRating, float currRating) {
        return (((count * overAllRating) + currRating) / (count+1));
    }

}