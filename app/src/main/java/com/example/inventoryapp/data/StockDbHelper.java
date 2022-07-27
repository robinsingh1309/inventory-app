package com.example.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.inventoryapp.data.StockContract.StockEntry;

public class StockDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";
    public static final int DATABASE_VERSION = 1;

    public StockDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_STATEMENT = "CREATE TABLE " + StockEntry.TABLE_NAME + " ("
                + StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StockEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + StockEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + StockEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + StockEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL, "
                + StockEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + StockEntry.COLUMN_SUPPLIER_CONTACT + " TEXT NOT NULL);";

        db.execSQL(SQL_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
