package ru.rutoken.demobank.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class},version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract UserDao userDao();

    private static AppDataBase INSTANCE;

    public static AppDataBase getdbInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDataBase.class,"DB_NAME")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

}