package com.karbyshev.droptobasket.ui;

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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements IOnItemClickListener {
    private static final int REQUEST_TAKE_PHOTO = 1;

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
    private BitmapUtils bitmapUtils = new BitmapUtils(this);
    private DroppedItem mDroppedItem = new DroppedItem();
    private ImageView popupImageView;

    private String product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mAppDatabase = App.getInstance().getDatabase();
        mProductsDao = mAppDatabase.productsDao();
        mDroppedProductsDao = mAppDatabase.droppedProductsDao();

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
        bitmapUtils.setSavedImagePath("");
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
            if (mMainAdapter.getItemList().size() == 0) {
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

    @Override
    public void OnItemClick(int position, List<Item> list) {
        item = list.get(position);
        mDroppedItem.setProductName(item.getProductName());
        mDroppedItem.setImage(item.getImage());

        Single.create(e -> {
            mAppDatabase.droppedProductsDao().insert(mDroppedItem);
            mAppDatabase.productsDao().deleteItem(item);
            e.onSuccess(new Object());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        showAllItems();
    }

    private void openPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.popup_window, null);
        popupImageView = (ImageView) customView.findViewById(R.id.popupImageView);


        mPopupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        //How to implement Butterknife here???!!!
        //Cancel popup
        customView.findViewById(R.id.popupCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        //OK popup
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
                    if (bitmapUtils.getSavedImagePath().equals("")){
                        item.setImage("");
                    } else {
                        item.setImage(Config.fileUriPrefix + bitmapUtils.getSavedImagePath());
                    }
                    Single.create(e -> {
                        mAppDatabase.productsDao().insert(item);
                        e.onSuccess(new Object());
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();

                    mPopupWindow.dismiss();
                    showAllItems();
                    System.out.println(item.getImage());
                }
            }
        });

        //Take a picture
        popupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(mConstraintLayout, Gravity.CENTER, 0, 0);
    }

    private void showAllItems() {

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

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = bitmapUtils.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.karbyshev.droptobasket.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            bitmapUtils.setPic(popupImageView);
        }
    }
}

