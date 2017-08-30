package com.igbt6.lovelyclock.splashscreen.presenter;

import com.igbt6.lovelyclock.splashscreen.view.ISplashView;

/**
 * Created by igbt6 on 30.08.2017.
 */

public class SplashPresenter {

    private ISplashView mView;

    public SplashPresenter() {

    }

    public void setView(ISplashView view) {
       this.mView = view;
    }

    public ISplashView getView() {
        return this.mView;
    }

    public void animationFinished() {
        getView().hideProgressBar();
        getView().goToMainView();
    }
}
