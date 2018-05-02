package com.karbyshev.droptobasket.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.karbyshev.droptobasket.App;
import com.karbyshev.droptobasket.R;
import com.karbyshev.droptobasket.database.AppDatabase;
import com.karbyshev.droptobasket.model.Item;
import com.karbyshev.droptobasket.utils.BitmapUtils;
import com.karbyshev.droptobasket.utils.Config;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddingDialog extends DialogFragment {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private String product;

    @BindView(R.id.popupImageView)
    ImageView popupImageView;
    @BindView(R.id.popupEditText)
    EditText editText;

    private BitmapUtils bitmapUtils;
    private Item item;
    private AppDatabase mAppDatabase;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.popup_window, null);
        ButterKnife.bind(this, customView);
        builder.setView(customView);

        bitmapUtils = new BitmapUtils(getActivity().getApplicationContext());
        bitmapUtils.setSavedImagePath("");
        item = new Item();
        mAppDatabase = App.getInstance().getDatabase();

        popupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        builder.setTitle("Add your product:")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        product = editText.getText().toString();

                        if (TextUtils.isEmpty(product)) {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Add something to list!",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            item.setProductName(product);
                            if (bitmapUtils.getSavedImagePath().equals("")) {
                                item.setImage("");
                            } else {
                                item.setImage(Config.fileUriPrefix + bitmapUtils.getSavedImagePath());
                            }

//                            ((MainActivity) getActivity()).okClicked();
                            Single.create(e -> {
                                mAppDatabase.productsDao().insert(item);
                                e.onSuccess(new Object());
                            }).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();

                            ((MainActivity) getActivity()).showAllItems();
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddingDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = bitmapUtils.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "com.karbyshev.droptobasket.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == getActivity().RESULT_OK) {
            bitmapUtils.setPic(popupImageView);
        }
    }
}
