package com.fitnesstracker.repository;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fitness_tracker_table")
public class FitnessTracker {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String mDate;
    private String mDistance;
    private String mAverageSpeed;
    private String mTime;
    //private String mRunHistory;

    public FitnessTracker(String mDate, String mDistance, String mAverageSpeed, String mTime /*,String mRunHistory*/) {
        this.mDate = mDate;
        this.mDistance = mDistance;
        this.mAverageSpeed = mAverageSpeed;
        this.mTime = mTime;
        //this.mRunHistory = mRunHistory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return mDate;
    }

    public String getDistance() {
        return mDistance;
    }

    public String getAverageSpeed() {
        return mAverageSpeed;
    }

    public String getTime() {
        return mTime;
    }

    /*public String getRunHistory() {
        return mRunHistory;
    }*/
}
