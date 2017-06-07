package com.example.android.inventoryapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InvEntry;

public class InventoryCursorAdapter extends CursorAdapter{

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_PRICE);
        int productSoldColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_SOLD);

        // Read the pet attributes from the Cursor for the current pet
        String productName = cursor.getString(nameColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productSold = cursor.getInt(productSoldColumnIndex);

        nameTextView.setText(productName);
        quantityTextView.setText("Quantity: " + Integer.toString(productQuantity));
        priceTextView.setText("Price: $" + Integer.toString(productPrice));

        final int mCurrentQuantity = productQuantity;
        final int mSoldQuantity = productSold;
        final Context mContext = context;
        int idColumnIndex = cursor.getColumnIndex(InvEntry._ID);

        final int rowId = cursor.getInt(idColumnIndex);
        Button buttonSell = (Button) view.findViewById(R.id.sell_button);
        buttonSell.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                updateUI(mCurrentQuantity, mSoldQuantity, mContext, rowId);
            }
        });

    }

    private void updateUI(int currentQuantity, int soldQuantity, Context context, int id) {
        if (currentQuantity != 0) {
            currentQuantity--;
            soldQuantity++;
            ContentValues values = new ContentValues();
            values.put(InvEntry.COLUMN_QUANTITY, currentQuantity);
            values.put(InvEntry.COLUMN_SOLD, soldQuantity);
            Uri newUri = Uri.withAppendedPath(InvEntry.CONTENT_URI, Integer.toString(id));

            int rows = context.getContentResolver().update(newUri, values, null, null);
            if (rows == 0) {
                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No items left", Toast.LENGTH_SHORT).show();
        }

    }
}
