package com.example.den.downloadimage.component;

import com.example.den.downloadimage.module.AppModule;
import com.example.den.downloadimage.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity mainActivity);
}
