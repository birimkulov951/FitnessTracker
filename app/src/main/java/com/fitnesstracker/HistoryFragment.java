package com.fitnesstracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitnesstracker.mvp.MainContract;
import com.fitnesstracker.mvp.MainPresenter;
import com.fitnesstracker.repository.FitnessTracker;

import java.util.List;

public class HistoryFragment extends Fragment implements MainContract.View {

    private static final String TAG = "HistoryFragment";

    /** MVP Presenter */
    private MainContract.Presenter presenter;

    /** RecyclerView */
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    /** Adapter for the list of Books */
    private FitnessTrackerAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyText, mEmptyText2;

   /** Required empty fragment to receive data from MapFragment */
    public HistoryFragment() {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyText = (TextView) view.findViewById(R.id.empty_text);
        mEmptyText2 = (TextView) view.findViewById(R.id.empty_text_2);
        mRecyclerView = view.findViewById(R.id.recycler_view);


        mAdapter = new FitnessTrackerAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        presenter = new MainPresenter(this, getActivity().getApplication());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        presenter.getAllRunHistory().observe(getActivity(), new Observer<List<FitnessTracker>>() {
            @Override
            public void onChanged(List<FitnessTracker> fitnessTrackers) {
                mAdapter.submitList(fitnessTrackers);
                if (presenter.getAllRunHistorySize() == 0) {
                    showEmptyRunHistory();
                } else {
                    hideEmptyRunHistory();
                }
            }
        });

        setOnItemClickListener();

        try{
            String distance = getArguments().getString("DISTANCE");
            String averageSpeed = getArguments().getString("AVERAGE_SPEED");
            String time = getArguments().getString("RUN_TIME");
            String date = getArguments().getString("DATE");

            @SuppressLint("DefaultLocale") FitnessTracker tracker = new FitnessTracker(date,
                    String.format("%.2f",Double.valueOf(distance)),
                    String.format("%.2f",Double.valueOf(averageSpeed)),
                    String.format("%.2f",Double.valueOf(time)));

            presenter.insert(tracker);

            Log.d(TAG, "onCreate: tracker data distance: " + distance);
            Log.d(TAG, "onCreate: tracker data averageSpeed: " + averageSpeed);
            Log.d(TAG, "onCreate: tracker data time: " + time);
            Log.d(TAG, "onCreate: tracker data date: " + date);

            Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreateView: bundle is null");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_history, container, false);

        return  v;
    }


    @Override
    public void setOnItemClickListener() {

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.delete(mAdapter.getTeslaAt(viewHolder.getAdapterPosition()));
                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();

            }
        }).attachToRecyclerView(mRecyclerView);

    }

    @Override
    public void showEmptyRunHistory() {
        mEmptyText.setVisibility(View.VISIBLE);
        mEmptyText2.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyRunHistory() {
        mEmptyText.setVisibility(View.INVISIBLE);
        mEmptyText2.setVisibility(View.INVISIBLE);
    }

}
