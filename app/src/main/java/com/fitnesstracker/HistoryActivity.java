package com.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fitnesstracker.mvp.MainContract;
import com.fitnesstracker.mvp.MainPresenter;
import com.fitnesstracker.repository.FitnessTracker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import static com.fitnesstracker.MapActivity.EXTRA_DISTANCE;

public class HistoryActivity extends AppCompatActivity implements MainContract.View {

    private static final String TAG = "HistoryActivity";

    /** MVP Presenter */
    private MainContract.Presenter presenter;

    /** RecyclerView */
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    /** Adapter for the list of Books */
    private FitnessTrackerAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyText, mEmptyText2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mEmptyText = (TextView) findViewById(R.id.empty_text);
        mEmptyText2 = (TextView) findViewById(R.id.empty_text_2);
        mRecyclerView = findViewById(R.id.recycler_view);

        mAdapter = new FitnessTrackerAdapter();
        mLayoutManager = new LinearLayoutManager(this);
        presenter = new MainPresenter(this, getApplication());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        presenter.getAllRunHistory().observe(this, new Observer<List<FitnessTracker>>() {
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

        if (getIntent().hasExtra(EXTRA_DISTANCE)) {

            double distance = Double.parseDouble(getIntent().getExtras().getString(EXTRA_DISTANCE));
            double averageSpeed = Double.parseDouble(getIntent().getExtras().getString(MapActivity.EXTRA_AVERAGE_SPEED));
            double time = Double.parseDouble(getIntent().getExtras().getString(MapActivity.EXTRA_RUN_TIME));
            String date = getIntent().getExtras().getString(MapActivity.EXTRA_DATE);

            @SuppressLint("DefaultLocale") FitnessTracker tracker = new FitnessTracker(date,String.format("%.2f",distance),String.format("%.2f",averageSpeed),String.format("%.2f",time));

            presenter.insert(tracker);

            Log.d(TAG, "onCreate: tracker data distance: " + distance);
            Log.d(TAG, "onCreate: tracker data averageSpeed: " + averageSpeed);
            Log.d(TAG, "onCreate: tracker data time: " + time);
            Log.d(TAG, "onCreate: tracker data date: " + date);

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }

        setOnItemClickListener();
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
                Toast.makeText(HistoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

            }
        }).attachToRecyclerView(mRecyclerView);

       /* mAdapter.setOnItemClickListener(new FitnessTrackerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(HistoryActivity.this, "List item clicked", Toast.LENGTH_SHORT).show();
            }
        });*/

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


