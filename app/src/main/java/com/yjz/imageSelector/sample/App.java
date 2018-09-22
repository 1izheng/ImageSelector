package com.yjz.imageSelector.sample;

import android.app.Application;

import com.yjz.imageSelector.ImageSelector;

/**
 * @author lizheng
 * @date created at 2018/9/21 下午5:14
 */
public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ImageSelector.getInstance().setImageLoader(new GlideImageLoader());
    }
}
