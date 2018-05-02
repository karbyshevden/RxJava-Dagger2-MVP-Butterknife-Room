package com.karbyshev.droptobasket.ui;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.karbyshev.droptobasket.App;
import com.karbyshev.droptobasket.R;
import com.karbyshev.droptobasket.adapter.MainAdapter;
import com.karbyshev.droptobasket.database.AppDatabase;
import com.karbyshev.droptobasket.database.DroppedProductsDao;
import com.karbyshev.droptobasket.database.ProductsDao;
import com.karbyshev.droptobasket.model.DroppedItem;
import com.karbyshev.droptobasket.model.Item;
import com.karbyshev.droptobasket.utils.BitmapUtils;
import com.karbyshev.droptobasket.utils.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements IOnItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.mainConstraintLayout)
    ConstraintLayout mConstraintLayout;
    @BindView(R.id.mainRecyclerView)
    RecyclerView mMainRecyclerView;

    private MainAdapter mMainAdapter;
    private GridLayoutManager mGridLayoutManager;
    private AppDatabase mAppDatabase;
    private ProductsDao mProductsDao;
    private DroppedProductsDao mDroppedProductsDao;
    private Item item;
    private DroppedItem mDroppedItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mAppDatabase = App.getInstance().getDatabase();
        mProductsDao = mAppDatabase.productsDao();
        mDroppedProductsDao = mAppDatabase.droppedProductsDao();

        item = new Item();
        mDroppedItem = new DroppedItem();

        mMainRecyclerView.setHasFixedSize(true);
        mGridLayoutManager = new GridLayoutManager(this, 3);
        mMainRecyclerView.setLayoutManager(mGridLayoutManager);
        mMainAdapter = new MainAdapter();
        mMainAdapter.setOnItemClickListener(MainActivity.this);
        mMainRecyclerView.setAdapter(mMainAdapter);

        showAllItems();
    }

    @OnClick(R.id.fab)
    public void addProduct(View view) {
        AddingDialog addingDialog = new AddingDialog();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        addingDialog.show(transaction, "dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_dropped) {
            Intent intent = new Intent(MainActivity.this, DroppedActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_select_all) {
            if (mMainAdapter.getItemList().size() == 0) {
                Toast.makeText(this, "Nothing to select", Toast.LENGTH_SHORT).show();
            } else {
                Disposable task = mAppDatabase.productsDao().getAll()
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(Schedulers.io())
                                                .subscribe(items -> {
                                                ArrayList<DroppedItem> dropped = new ArrayList<>();
                                                for(Item it: items) {
                                                        dropped.add(new DroppedItem(it));
                                                    }
                                                mAppDatabase.droppedProductsDao().insertAll(dropped);
                                                mAppDatabase.productsDao().clearTable();
                                            });

                showAllItems();

                Toast.makeText(this, "All selected", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnItemClick(int position, List<Item> list) {
        item = list.get(position);
        mDroppedItem.setProductName(item.getProductName());
        mDroppedItem.setImage(item.getImage());

        Flowable.create(subscriber -> {
            mAppDatabase.droppedProductsDao().insert(mDroppedItem);
            mAppDatabase.productsDao().deleteItem(item);
            subscriber.onNext(new Object());
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        showAllItems();
    }

    public void showAllItems() {

        mAppDatabase.productsDao().getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Item>>() {
                    @Override
                    public void accept(List<Item> list) throws Exception {
                        mMainAdapter.addProduct(list);
                    }
                });
    }
}


