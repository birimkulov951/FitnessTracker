package com.fitnesstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fitnesstracker.repository.FitnessTracker;

import java.util.Calendar;

public class FitnessTrackerAdapter extends ListAdapter<FitnessTracker,FitnessTrackerAdapter.TeslaHolder> {

    private OnItemClickListener listener;

    private static final DiffUtil.ItemCallback<FitnessTracker> DIFF_CALLBACK = new DiffUtil.ItemCallback<FitnessTracker>() {
        @Override
        public boolean areItemsTheSame(@NonNull FitnessTracker oldItem, @NonNull FitnessTracker newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FitnessTracker oldItem, @NonNull FitnessTracker newItem) {
            return oldItem.getId() == newItem.getId() &&
                    oldItem.getDistance().equals(newItem.getDistance()) &&
                    oldItem.getAverageSpeed().equals(newItem.getAverageSpeed()) &&
                    oldItem.getTime().equals(newItem.getTime()) &&
                    oldItem.getDate().equals(newItem.getDate());
        }
    };

    public FitnessTrackerAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TeslaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.run_history_item, parent, false);
        return new TeslaHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TeslaHolder holder, int position) {
        FitnessTracker currentRoute = getItem(position);
        holder.mDistance.setText(currentRoute.getDistance());
        holder.mAverageSpeed.setText(currentRoute.getAverageSpeed());
        holder.mTime.setText(currentRoute.getTime());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(currentRoute.getDate()));

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int mHour = calendar.get(Calendar.HOUR);
        int mMinutes = calendar.get(Calendar.MINUTE);

        String mAmPm;
        if (calendar.get(Calendar.AM_PM) == 0) {
            mAmPm = "AM";
        } else {
            mAmPm = "PM";
        }

        String dateStr = mDay + "/" + mMonth + "/" + mYear + " at " + mHour + ":" + mMinutes + " " + mAmPm;

        holder.mDate.setText(dateStr);

        // add bitmap here todo

    }

    public FitnessTracker getTeslaAt(int position) {
        return getItem(position);
    }


    //**********************************************************************************************


    class TeslaHolder extends RecyclerView.ViewHolder {

        private TextView mDistance;
        private TextView mAverageSpeed;
        private TextView mTime;
        private TextView mDate;
        //private ImageView mRouteHistory;

        public TeslaHolder(@NonNull View itemView) {
            super(itemView);
            mDistance = itemView.findViewById(R.id.distance);
            mAverageSpeed = itemView.findViewById(R.id.average_speed);
            mTime = itemView.findViewById(R.id.run_time);
            mDate = itemView.findViewById(R.id.date);
            //mRouteHistory = itemView.findViewById(R.id.history_bitmap);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(FitnessTracker note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}