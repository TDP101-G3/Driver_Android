package com.lu.driver;

import android.content.SharedPreferences;
import android.media.Rating;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StarDialogActivity extends AppCompatActivity {
    private static final String TAG = "StarDialogActivity";
    private CommonTask callTask;
    private RatingBar rb;
    private Button btOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ratingbar_activity);
        rb = findViewById(R.id.ratingBar1);
        btOk = findViewById(R.id.btOk);

        //設定RatingBar 評分的步長
        rb.setStepSize(1.0f);
        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float n =rb.getRating();
                SharedPreferences pref = getSharedPreferences(Common.PREF_FILE,
                        MODE_PRIVATE);
                pref.edit()
                        .putFloat("n",n)
                        .apply();
                setResult(RESULT_OK);
                finish();
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        setTitle("");
    }

    private void showToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }
}
