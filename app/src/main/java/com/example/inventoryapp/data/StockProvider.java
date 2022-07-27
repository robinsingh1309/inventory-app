package com.example.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.inventoryapp.data.StockContract.StockEntry;

public class StockProvider extends ContentProvider {

    private static final String LOG_TAG = StockProvider.class.getSimpleName();

    private StockDbHelper mDbHelper;
    private static final int STOCKS = 100;
    private static final int STOCKS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_CATEGORY, STOCKS);
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_CATEGORY + "/#", STOCKS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new StockDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projections, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                cursor = database.query(StockEntry.TABLE_NAME, projections, selection, selectionArgs, null, null, sortOrder);
                break;
            case STOCKS_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(StockEntry.TABLE_NAME, projections, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                return insertStock(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertStock(Uri uri, ContentValues contentValues) {

        String productName = contentValues.getAsString(StockEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product requires name: ");
        }

        int productQuantity = contentValues.getAsInteger(StockEntry.COLUMN_PRODUCT_QUANTITY);
        if (productQuantity < 0) {
            throw new IllegalArgumentException("Product requires quantity: ");
        }

        float productPrice = contentValues.getAsFloat(StockEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice < 0) {
            throw new IllegalArgumentException("Product requires price: ");
        }

        String supplierName = contentValues.getAsString(StockEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier name is required: ");
        }

        String supplierContact = contentValues.getAsString(StockEntry.COLUMN_SUPPLIER_CONTACT);
        if (supplierContact == null) {
            throw new IllegalArgumentException("Supplier contact is required: ");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long insertedRow = database.insert(StockEntry.TABLE_NAME, null, contentValues);
        if (insertedRow == -1) {
            Log.e(LOG_TAG, "Failed to insert row for: " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, insertedRow);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case STOCKS:
                rowsDeleted = database.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOCKS_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = sUriMatcher.match(uri);

        switch (match) {
            case STOCKS:
                return updateStock(uri, contentValues, selection, selectionArgs);
            case STOCKS_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateStock(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for: " + uri);
        }

    }

    private int updateStock(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(StockEntry.COLUMN_PRODUCT_NAME)) {
            String productName = contentValues.getAsString(StockEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product requires name: ");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_PRODUCT_QUANTITY)) {
            int productQuantity = contentValues.getAsInteger(StockEntry.COLUMN_PRODUCT_QUANTITY);
            if (productQuantity < 0) {
                throw new IllegalArgumentException("Product requires quantity: ");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_PRODUCT_PRICE)) {
            float productPrice = contentValues.getAsFloat(StockEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice == 0) {
                throw new IllegalArgumentException("Product requires price: ");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(StockEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier name is required: ");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_SUPPLIER_CONTACT)) {
            String supplierContact = contentValues.getAsString(StockEntry.COLUMN_SUPPLIER_CONTACT);
            if (supplierContact == null) {
                throw new IllegalArgumentException("Supplier contact is required: ");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int updatedRow = database.update(StockEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (updatedRow != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRow;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCKS:
                return StockEntry.COLUMN_LIST_TYPE;
            case STOCKS_ID:
                return StockEntry.COLUMN_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri + " with match " + match);
        }
    }
}
