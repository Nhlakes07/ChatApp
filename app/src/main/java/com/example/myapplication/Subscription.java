package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;

public class Subscription extends AppCompatActivity {

    private int currentImageIndex = 0;
    private int[] imageIds = {R.id.Image1, R.id.Image2,R.id.Image3};
    private Handler handler;
    private ImageView currentImageView;

    private BillingClient billingClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_subscription);




        currentImageView = findViewById(imageIds[currentImageIndex]);
        handler = new Handler();
        // Start displaying images after 3 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNextImage();
            }
        }, 1000);

    }

    private void showNextImage() {
        currentImageView.setVisibility(View.GONE);

        currentImageIndex++;

        // Reset the index to 0 if it reaches the end
        if (currentImageIndex >= imageIds.length) {
            currentImageIndex = 0;
        }

        currentImageView = findViewById(imageIds[currentImageIndex]);
        currentImageView.setVisibility(View.VISIBLE);

        // Schedule the next image after 3 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNextImage();
            }
        }, 5000);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any remaining callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }
    }
