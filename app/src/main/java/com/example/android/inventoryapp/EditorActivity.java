package com.example.android.inventoryapp;


import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InvEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity {

    InventoryDbHelper mDbHelper;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.inventoryapp";
    private static final int IMAGE_CHOSEN = 0;


    private ImageView mImageView;
    private Bitmap mBitmap;
    private boolean isGalleryPicture = false;

    private Uri mUri;
    private String uriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Button buttonChoose = (Button) findViewById(R.id.image_button);
        mImageView = (ImageView) findViewById(R.id.image_view);
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                writeInventoryData();
                return true;

            case android.R.id.home:
                if (!hasChanged()) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void writeInventoryData() {
        if (isValid()) {
            mDbHelper = new InventoryDbHelper(this);

            EditText textName = (EditText) findViewById(R.id.edit_product_name);
            EditText textQuantity = (EditText) findViewById(R.id.edit_quantity);
            EditText textPrice = (EditText) findViewById(R.id.edit_price);

            String invName = textName.getText().toString();
            int invQuantity = Integer.parseInt(textQuantity.getText().toString());
            int invPrice = Integer.parseInt(textPrice.getText().toString());

            ContentValues values = new ContentValues();
            values.put(InvEntry.COLUMN_PRODUCT_NAME, invName);
            values.put(InvEntry.COLUMN_QUANTITY, invQuantity);
            values.put(InvEntry.COLUMN_PRICE, invPrice);
            values.put(InvEntry.COLUMN_IMAGE, uriString);

            Uri newUri = getContentResolver().insert(InventoryContract.InvEntry.CONTENT_URI, values);


            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_inv_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_inv_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            Toast.makeText(this, getString(R.string.editor_blank),
                    Toast.LENGTH_SHORT).show();

        }
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {

        if (!hasChanged()) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public boolean isValid() {
        EditText textName = (EditText) findViewById(R.id.edit_product_name);
        EditText textQuantity = (EditText) findViewById(R.id.edit_quantity);
        EditText textPrice = (EditText) findViewById(R.id.edit_price);

        return (textName.getText().toString().trim().length() != 0 &&
                textPrice.getText().toString().trim().length() != 0 &&
                textQuantity.getText().toString().trim().length() != 0 &&
                uriString != null);


    }

    private boolean hasChanged() {
        EditText textName = (EditText) findViewById(R.id.edit_product_name);
        EditText textQuantity = (EditText) findViewById(R.id.edit_quantity);
        EditText textPrice = (EditText) findViewById(R.id.edit_price);

        return textName.getText().toString().trim().length() != 0 ||
                textPrice.getText().toString().trim().length() != 0 ||
                textQuantity.getText().toString().trim().length() != 0 ||
                uriString != null;
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_CHOSEN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                mUri = resultData.getData();

                mBitmap = getBitmapFromUri(mUri);
                mImageView.setImageBitmap(mBitmap);
                uriString = getShareableImageUri().toString();
                isGalleryPicture = true;
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            }
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            return image;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Uri getShareableImageUri() {
        Uri imageUri;

        if (isGalleryPicture) {
            String filename = getFilePath();
            saveBitmapToFile(getCacheDir(), filename, mBitmap, Bitmap.CompressFormat.JPEG, 100);
            File imageFile = new File(getCacheDir(), filename);

            imageUri = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, imageFile);

        } else {
            imageUri = mUri;
        }

        return imageUri;
    }

    public String getFilePath() {
        Cursor returnCursor =
                getContentResolver().query(mUri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);

        if (returnCursor != null) {
            returnCursor.moveToFirst();
        }
        String fileName = null;
        if (returnCursor != null) {
            fileName = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
        if (returnCursor != null) {
            returnCursor.close();
        }
        return fileName;
    }


    public boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                    Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format, quality, fos);
            fos.close();

            return true;
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return false;

    }
}
