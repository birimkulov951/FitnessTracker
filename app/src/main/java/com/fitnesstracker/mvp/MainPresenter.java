package com.fitnesstracker.mvp;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fitnesstracker.repository.FitnessTracker;
import com.fitnesstracker.repository.Repository;

import java.util.List;

public class MainPresenter implements MainContract.Presenter{

    private static final String TAG = "MainPresenter";

    private MainContract.View view;

    private Repository repository;
    private LiveData<List<FitnessTracker>> allRunHistory;

    public MainPresenter(MainContract.View view, Application application) {
        this.view = view;
        repository = new Repository(application);
        allRunHistory = repository.getAllRunHistory();
    }

    public void onDestroy() {
        view = null;
    }


    @Override
    public LiveData<List<FitnessTracker>> getAllRunHistory() {
        return repository.getAllRunHistory();
    }

    @Override
    public void insert(FitnessTracker fitnessTracker) {
        repository.insert(fitnessTracker);
    }

    @Override
    public void update(FitnessTracker fitnessTracker) {
        repository.update(fitnessTracker);
    }

    @Override
    public void delete(FitnessTracker fitnessTracker) {
        repository.delete(fitnessTracker);
    }

    @Override
    public void deleteAllRunHistory() {
        repository.deleteAllHistory();
    }

    @Override
    public int getAllRunHistorySize() {
        return repository.getAllRunHistory().getValue().size();
    }
}
