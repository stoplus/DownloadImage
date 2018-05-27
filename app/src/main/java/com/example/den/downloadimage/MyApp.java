package com.example.den.downloadimage;

import android.app.Application;

import com.example.den.downloadimage.component.AppComponent;
import com.example.den.downloadimage.component.DaggerAppComponent;
import com.example.den.downloadimage.module.AppModule;

public class MyApp extends Application {
    private static MyApp app;
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(getApplicationContext()))
                .build();
    }

    public static MyApp app(){
        return app;
    }

    public AppComponent appComponent(){
        return appComponent;
    }
}
