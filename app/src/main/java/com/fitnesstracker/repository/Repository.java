package com.fitnesstracker.repository;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;

public class Repository {

    private FitnessTrackerDao fitnessTrackerDao;
    private LiveData<List<FitnessTracker>> allRunHistory;

    public Repository(Application application) {
        FitnessTrackerDatabase database = FitnessTrackerDatabase.getInstance(application);
        fitnessTrackerDao = database.fitnessTrackerDao();
        allRunHistory = fitnessTrackerDao.getAllHistory();
    }

    public void insert(FitnessTracker fitnessTracker) {
        new InsertTeslaAsyncTask(fitnessTrackerDao).execute(fitnessTracker);
    }
    public void update(FitnessTracker fitnessTracker) {
        new UpdateTeslaAsyncTask(fitnessTrackerDao).execute(fitnessTracker);
    }
    public void delete(FitnessTracker fitnessTracker) {
        new DeleteTeslaAsyncTask(fitnessTrackerDao).execute(fitnessTracker);
    }
    public void deleteAllHistory() {
        new DeleteAllTeslaAsyncTask(fitnessTrackerDao).execute();
    }
    public LiveData<List<FitnessTracker>> getAllRunHistory() {
        return allRunHistory;
    }

    private static class InsertTeslaAsyncTask extends AsyncTask<FitnessTracker, Void, Void> {

        private FitnessTrackerDao teslaDao;

        private InsertTeslaAsyncTask(FitnessTrackerDao dao) {
            this.teslaDao = dao;
        }

        @Override
        protected Void doInBackground(FitnessTracker... inventories) {
            teslaDao.insert(inventories[0]);
            return null;
        }

    }

    private static class UpdateTeslaAsyncTask extends AsyncTask<FitnessTracker, Void, Void> {

        private FitnessTrackerDao teslaDao;

        private UpdateTeslaAsyncTask(FitnessTrackerDao dao) {
            this.teslaDao = dao;
        }

        @Override
        protected Void doInBackground(FitnessTracker... inventories) {
            teslaDao.update(inventories[0]);
            return null;
        }

    }

    private static class DeleteTeslaAsyncTask extends AsyncTask<FitnessTracker, Void, Void> {

        private FitnessTrackerDao teslaDao;

        private DeleteTeslaAsyncTask(FitnessTrackerDao dao) {
            this.teslaDao = dao;
        }

        @Override
        protected Void doInBackground(FitnessTracker... inventories) {
            teslaDao.delete(inventories[0]);
            return null;
        }

    }

    private static class DeleteAllTeslaAsyncTask extends AsyncTask<FitnessTracker, Void, Void> {

        private FitnessTrackerDao teslaDao;

        private DeleteAllTeslaAsyncTask(FitnessTrackerDao dao) {
            this.teslaDao = dao;
        }

        @Override
        protected Void doInBackground(FitnessTracker... inventories) {
            teslaDao.deleteAllHistory();
            return null;
        }

    }
}
