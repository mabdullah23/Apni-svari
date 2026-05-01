package com.example.apni_svari;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class animation extends AppCompatActivity {

    private boolean hasNavigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation);

        ImageView carImage = findViewById(R.id.carImage);
        View root = findViewById(R.id.animationRoot);

        root.post(() -> startCarAnimation(root, carImage));
    }

    private void startCarAnimation(View root, ImageView carImage) {
        if (hasNavigated) {
            return;
        }

        float startX = -carImage.getWidth();
        float endX = root.getWidth() + carImage.getWidth();

        carImage.setTranslationX(startX);

        ObjectAnimator animator = ObjectAnimator.ofFloat(carImage, View.TRANSLATION_X, startX, endX);
        animator.setDuration(2500L);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (hasNavigated) {
                    return;
                }
                hasNavigated = true;
                startActivity(new Intent(animation.this, MainRegPage.class));
                finish();
            }
        });
        animator.start();
    }
}
