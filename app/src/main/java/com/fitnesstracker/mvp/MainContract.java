package com.fitnesstracker.mvp;

import androidx.lifecycle.LiveData;

import com.fitnesstracker.repository.FitnessTracker;

import java.util.List;

public interface MainContract {

    interface View {

        void setOnItemClickListener();

        void showEmptyRunHistory();

        void hideEmptyRunHistory();


    }

    interface Presenter {
        // business logic happens here

        LiveData<List<FitnessTracker>> getAllRunHistory();

        void insert(FitnessTracker fitnessTracker);

        void update(FitnessTracker fitnessTracker);

        void delete(FitnessTracker fitnessTracker);

        void deleteAllRunHistory();

        int getAllRunHistorySize();

    }

}
