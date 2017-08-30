package com.igbt6.lovelyclock.splashscreen.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.igbt6.lovelyclock.MainActivity;
import com.igbt6.lovelyclock.R;
import com.igbt6.lovelyclock.splashscreen.presenter.SplashPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by igbt6 on 30.08.2017.
 */

public class SplashActivity extends AppCompatActivity implements ISplashView {

    private SplashPresenter mSplashPresenter;

    @BindView(R.id.splash_text)
    TextView mSplashText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mSplashPresenter = new SplashPresenter();
        mSplashPresenter.setView(this);
        ButterKnife.bind(this);
        mSplashText.setText("Hello");
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSplashPresenter.animationFinished();
    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void goToMainView() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
