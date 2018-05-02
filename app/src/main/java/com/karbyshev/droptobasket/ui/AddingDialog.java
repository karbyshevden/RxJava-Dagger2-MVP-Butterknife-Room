package com.karbyshev.droptobasket.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_TAKE_FROM_GALLARY = 1;

    private String product;

    @BindView(R.id.popupImageView)
    ImageView mPopupImageView;
    @BindView(R.id.popupEditText)
    EditText mEditText;
    @BindView(R.id.popupAddFromCamera)
    ImageView mPopupAddFromCamera;
    @BindView(R.id.popupAddFromGallery)
    ImageView mPopupAddFromGallery;
    @BindView(R.id.popupVisibilityLinearLayout)
    LinearLayout mPopupVisibilityLinearLayout;

    private BitmapUtils bitmapUtils;
    private Item item;
    private AppDatabase mAppDatabase;
    private String imageFromGallery;

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

        mPopupAddFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Not working yet!",
                        Toast.LENGTH_SHORT)
                        .show();
//                takePhotoFromGallery();
            }
        });

        mPopupAddFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        builder.setTitle("Add your product:")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        product = mEditText.getText().toString();

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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = bitmapUtils.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "com.karbyshev.droptobasket.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    public void takePhotoFromGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , REQUEST_TAKE_FROM_GALLARY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == getActivity().RESULT_OK) {
                    mPopupVisibilityLinearLayout.setVisibility(View.GONE);
                    mPopupImageView.setVisibility(View.VISIBLE);
                    bitmapUtils.setPic(mPopupImageView);
                }
                break;

            case 1:
                if (resultCode == getActivity().RESULT_OK){
                    mPopupVisibilityLinearLayout.setVisibility(View.GONE);
                    mPopupImageView.setVisibility(View.VISIBLE);
                    Uri selectedImage = data.getData();
                    mPopupImageView.setImageURI(selectedImage);
                }
                break;
        }
    }
}
