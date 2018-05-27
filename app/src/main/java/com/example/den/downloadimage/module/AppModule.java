package com.example.den.downloadimage.module;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.example.den.downloadimage.database.ImageObjDao;
import com.example.den.downloadimage.database.MyDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Singleton @Provides
    public Context provideContext(){
        return context;
    }

    @Singleton @Provides
    public MyDatabase provideMyDatabase(Context context){
        return Room.databaseBuilder(context, MyDatabase.class, "my-db").build();
    }

    @Singleton @Provides
    public ImageObjDao provideUserDao(MyDatabase myDatabase){
        return myDatabase.imageObjDao();
    }
}
