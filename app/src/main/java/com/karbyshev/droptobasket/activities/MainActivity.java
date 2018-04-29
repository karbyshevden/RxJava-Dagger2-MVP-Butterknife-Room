package com.karbyshev.droptobasket.activities;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import android.view.ViewGroup.LayoutParams;

import com.karbyshev.droptobasket.App;
import com.karbyshev.droptobasket.R;
import com.karbyshev.droptobasket.adapter.MainAdapter;
import com.karbyshev.droptobasket.database.AppDatabase;
import com.karbyshev.droptobasket.database.DroppedProductsDao;
import com.karbyshev.droptobasket.database.ProductsDao;
import com.karbyshev.droptobasket.model.Item;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements IOnItemClickListener{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.mainConstraintLayout)
    ConstraintLayout mConstraintLayout;
    @BindView(R.id.mainRecyclerView)
    RecyclerView mMainRecyclerView;

    private PopupWindow mPopupWindow;
    private MainAdapter mMainAdapter;
    private GridLayoutManager mGridLayoutManager;
    private AppDatabase mAppDatabase;
    private ProductsDao mProductsDao;
    private DroppedProductsDao mDroppedProductsDao;
    private Item item = new Item();

    private String product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mAppDatabase = App.getInstance().getDatabase();
        mProductsDao = mAppDatabase.productsDao();

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
        openPopupWindow();
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
            if (mMainAdapter.getItemList().size() == 0){
                Toast.makeText(this, "Nothing to select", Toast.LENGTH_SHORT).show();
            } else {
                //Transaction to dropped list

                //Delete all
                Single.create(e -> {
                    mAppDatabase.productsDao().clearTable();
                    e.onSuccess(new Object());
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();

                showAllItems();

                Toast.makeText(this, "All selected", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window, null);

        mPopupWindow = new PopupWindow(customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        //How to implement Butterknife here???!!!
        customView.findViewById(R.id.popupCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        customView.findViewById(R.id.popupOkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) customView.findViewById(R.id.popupEditText);
                product = editText.getText().toString();

                if (TextUtils.isEmpty(product)) {
                    Toast.makeText(getApplicationContext(),
                            "Add something to list!",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    item.setProductName(product);
                    Single.create(e -> {
                        mAppDatabase.productsDao().insert(item);
                        e.onSuccess(new Object());
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();

                    mPopupWindow.dismiss();
                    showAllItems();
                }
            }
        });

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(mConstraintLayout, Gravity.CENTER, 0, 0);
    }

    @Override
    public void OnItemClick(int position, List<Item> list) {
        item = list.get(position);

        Single.create(e -> {
            mAppDatabase.productsDao().deleteItem(item);
            e.onSuccess(new Object());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        showAllItems();

        Toast.makeText(this, "You have just removed an item!)", Toast.LENGTH_SHORT).show();
    }

    private void showAllItems(){

        mAppDatabase.productsDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<Item>>() {
                    @Override
                    public void onSuccess(List<Item> list) {
                        mMainAdapter.addProduct(list);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(),
                                "Something wrong",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

}

