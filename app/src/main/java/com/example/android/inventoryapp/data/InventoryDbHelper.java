package com.example.android.inventoryapp.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InvEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InvEntry.TABLE_NAME + " ("
                + InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InvEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                + InvEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + InvEntry.COLUMN_SOLD + " INTEGER NOT NULL DEFAULT 0, "
                + InvEntry.COLUMN_IMAGE + " TEXT);";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
