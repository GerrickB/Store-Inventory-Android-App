package com.example.android.inventoryapp;


import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryContract.InvEntry;

import java.io.FileDescriptor;
import java.io.IOException;

public class UpdateActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final int INVENTORY_LOADER = 1;
    InventoryDbHelper mDbHelper;
    private Uri currentUri;
    String currentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        Intent intent = getIntent();
        currentUri = intent.getData();
        mDbHelper = new InventoryDbHelper(this);
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        Button orderButton = (Button) findViewById(R.id.order_button);
        final TextView productName = (TextView) findViewById(R.id.name_update);
        final EditText editTextPrice = (EditText) findViewById(R.id.price_update);
        final EditText newOrderQuantity = (EditText) findViewById(R.id.order_amount_text);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newOrderQuantity.getText().toString().trim().length() != 0) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_SUBJECT,
                            "Order Information");
                    intent.putExtra(Intent.EXTRA_TEXT, "Name: " + productName.getText().toString() +
                                "     Order placed:" + newOrderQuantity.getText().toString() +
                                "     Price each: $" + editTextPrice.getText().toString());
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Order field cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    public void buttonOnClick(View view) {
        TextView textQuantity = (TextView) findViewById(R.id.current_quantity);
        String quantityString = textQuantity.getText().toString();
        int quantityInt = Integer.parseInt(quantityString);
        int id = view.getId();
        switch (id) {
            case R.id.add_quantity:
                quantityInt++;
                break;
            case R.id.subtract_quantity:
                if (quantityInt != 0) {
                    quantityInt--;
                } else
                    Toast.makeText(this, "Quantity can't be negative", Toast.LENGTH_SHORT).show();
                break;
        }

        textQuantity.setText(Integer.toString(quantityInt));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_details:
                updateDetails();
                finish();
                break;
            case R.id.action_delete_details:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {InvEntry._ID,
                InvEntry.COLUMN_PRODUCT_NAME,
                InvEntry.COLUMN_PRICE,
                InvEntry.COLUMN_QUANTITY,
                InvEntry.COLUMN_SOLD,
                InvEntry.COLUMN_IMAGE};
        return new CursorLoader(this, currentUri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.isClosed()) {
            TextView textName = (TextView) findViewById(R.id.name_update);
            EditText editTextPrice = (EditText) findViewById(R.id.price_update);
            TextView textQuantity = (TextView) findViewById(R.id.current_quantity);
            TextView textSold = (TextView) findViewById(R.id.sold);

            int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_IMAGE);
            int soldColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_SOLD);

            cursor.moveToNext();
            currentName = cursor.getString(nameColumnIndex);

            int currentPrice = cursor.getInt(priceColumnIndex);
            int currentQuantity = cursor.getInt(quantityColumnIndex);
            int currentSold = cursor.getInt(soldColumnIndex);

            String imageUriString = cursor.getString(imageColumnIndex);
            Uri imageUri = Uri.parse(imageUriString);
            Bitmap bitmap = getBitmapFromUri(imageUri);
            ImageView imageView = (ImageView) findViewById(R.id.image_info_view);
            imageView.setImageBitmap(bitmap);
            textName.setText(currentName);
            textSold.setText(String.valueOf(currentSold));
            editTextPrice.setText(String.valueOf(currentPrice));
            textQuantity.setText(String.valueOf(currentQuantity));
            cursor.close();
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }

    void updateDetails() {
        EditText editTextPrice = (EditText) findViewById(R.id.price_update);
        TextView textQuantity = (TextView) findViewById(R.id.current_quantity);
        TextView textSold = (TextView) findViewById(R.id.sold);
        int currentQuantity = Integer.parseInt(textQuantity.getText().toString());
        int newPrice = 0;
        if (editTextPrice.getText().toString().trim().length() != 0) {
            newPrice = Integer.parseInt(editTextPrice.getText().toString().trim());
        }

        int totalSold = Integer.parseInt(textSold.getText().toString().trim());
        int updatedSold = totalSold;
        int newQuantity = currentQuantity;
        if (newQuantity >= 0) {
            if (editTextPrice.getText().toString().trim().length() == 0) {
                Toast.makeText(this, "Price field can't be empty", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(InvEntry.COLUMN_PRICE, newPrice);
                values.put(InvEntry.COLUMN_QUANTITY, newQuantity);
                values.put(InvEntry.COLUMN_SOLD, updatedSold);

                int result = getContentResolver().update(currentUri, values, null, null);

                if (result == 0) {
                    Toast.makeText(this, getString(R.string.editor_insert_inv_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_inv_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.details_quantity_negative),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        int result = getContentResolver().delete(currentUri, null, null);
        if (result == 0) {
            Toast.makeText(this, "Delete Unsuccessful",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Entry Deleted",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
