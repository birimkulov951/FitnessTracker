package com.fitnesstracker.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FitnessTrackerDao {

    @Insert
    void insert(FitnessTracker FitnessTracker);

    @Update
    void update(FitnessTracker FitnessTracker);

    @Delete
    void delete(FitnessTracker FitnessTracker);

    @Query("DELETE FROM fitness_tracker_table")
    void deleteAllHistory();

    @Query("SELECT * FROM fitness_tracker_table ORDER BY id DESC")
    LiveData<List<FitnessTracker>> getAllHistory();

}
