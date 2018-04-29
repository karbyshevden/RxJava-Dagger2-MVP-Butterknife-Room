package com.karbyshev.droptobasket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.karbyshev.droptobasket.App;
import com.karbyshev.droptobasket.R;
import com.karbyshev.droptobasket.adapter.DroppedAdapter;
import com.karbyshev.droptobasket.database.AppDatabase;
import com.karbyshev.droptobasket.database.DroppedProductsDao;
import com.karbyshev.droptobasket.model.DroppedItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class DroppedActivity extends AppCompatActivity implements IOnDroppedItemClickListener{
    @BindView(R.id.droppedToolbar)
    Toolbar toolbar;
    @BindView(R.id.droppedRecyclerView)
    RecyclerView mRecyclerView;

    private DroppedAdapter mDroppedAdapter;
    private GridLayoutManager mGridLayoutManager;
    private AppDatabase mAppDatabase;
    private DroppedProductsDao mDroppedProductsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropped);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DroppedActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mAppDatabase = App.getInstance().getDatabase();
        mDroppedProductsDao = mAppDatabase.droppedProductsDao();

        mRecyclerView.setHasFixedSize(true);
        mGridLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mDroppedAdapter = new DroppedAdapter();
        mDroppedAdapter.setOnDroppedItemClickListener(DroppedActivity.this);
        mRecyclerView.setAdapter(mDroppedAdapter);

        showAllDroppedItems();
    }

    @Override
    public void OnItemClick(int position, List<DroppedItem> list) {
        Toast.makeText(this, "Item clicked!", Toast.LENGTH_SHORT).show();
    }

    private void showAllDroppedItems(){
        mAppDatabase.droppedProductsDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<DroppedItem>>() {
                    @Override
                    public void onSuccess(List<DroppedItem> droppedItems) {
                        mDroppedAdapter.addDropped(droppedItems);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "RX ERROR!", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
