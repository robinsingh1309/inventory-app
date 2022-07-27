package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.inventoryapp.data.StockContract.StockEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the stock data loader
     */
    private static final int STOCK_LOADER_ID = 0;
    /**
     * Cursor Adapter for the ListView
     */
    private StockAdapter mAdapter;

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        ListView listView = (ListView) findViewById(R.id.list);
        View emptyView = (View) findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivity(intent);
        });

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent1 = new Intent(CatalogActivity.this, EditorActivity.class);
            Uri currentStockUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI, id);
            intent1.setData(currentStockUri);
            startActivity(intent1);
        });

        // Setup an Adapter to create a list item for each row of stock data in the Cursor.
        // There is no stock data yet (until the loader finishes) so pass in null for the Cursor.
        mAdapter = new StockAdapter(this, null);
        listView.setAdapter(mAdapter);

        //Initialize the loader
        getLoaderManager().initLoader(STOCK_LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_dummy_data:
                insertData();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllEntries();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all
     * rows of stocks database
     */
    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(StockEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from stocks database");
    }

    /**
     * Helper method to insert hardcoded stock data into the database. For debugging purposes only.
     */
    private void insertData() {

        // Create a ContentValues object where column names are the keys,
        // and Stock unit attributes are the values.
        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_product_name));
        values.put(StockEntry.COLUMN_PRODUCT_PRICE, getString(R.string.dummy_product_price));
        values.put(StockEntry.COLUMN_PRODUCT_QUANTITY, getString(R.string.dummy_product_quantity));
        values.put(StockEntry.COLUMN_PRODUCT_IMAGE, R.drawable.empty);
        values.put(StockEntry.COLUMN_SUPPLIER_NAME, getString(R.string.dummy_supplier_name));
        values.put(StockEntry.COLUMN_SUPPLIER_CONTACT, getString(R.string.dummy_contact_number));

        // Insert a new row for Bread into the provider using the ContentResolver.
        // Use the {@link StockEntry#CONTENT_URI} to indicate that we want to insert
        // into the inventory database table(stocks).
        Uri uri = getContentResolver().insert(StockEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_PRODUCT_NAME,
                StockEntry.COLUMN_PRODUCT_PRICE,
                StockEntry.COLUMN_PRODUCT_QUANTITY,
                StockEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(
                this,
                StockEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link StockAdapter} with this new cursor containing updated stock unit data
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mAdapter.swapCursor(null);
    }
}