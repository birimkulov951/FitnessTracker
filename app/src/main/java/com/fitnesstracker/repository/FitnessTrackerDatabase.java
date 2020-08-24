package com.fitnesstracker.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {FitnessTracker.class}, version = 1)
public abstract class FitnessTrackerDatabase extends RoomDatabase {

    private static FitnessTrackerDatabase instance;

    public abstract FitnessTrackerDao fitnessTrackerDao();

    public static synchronized FitnessTrackerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    FitnessTrackerDatabase.class,"fitness_tracker_table")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private FitnessTrackerDao teslaDao;

        private PopulateDbAsyncTask(FitnessTrackerDatabase db) {
            teslaDao = db.fitnessTrackerDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }

    }
}
