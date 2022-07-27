package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.data.StockContract.StockEntry;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;

public class StockAdapter extends CursorAdapter {
    public StockAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView stockUnitName = (TextView) view.findViewById(R.id.product_name_text_view);
        TextView stockUnitPrice = (TextView) view.findViewById(R.id.stocks_unit_price_text_view);
        TextView stockUnitQuantity = (TextView) view.findViewById(R.id.stock_qty_text_view);
        ImageView stockUnitImage = (ImageView) view.findViewById(R.id.stock_image);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of stock attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(StockEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_PRODUCT_IMAGE);

        // Read the stock attributes from the Cursor for the current stock
        final int productId = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        float productPrice = cursor.getFloat(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        String productImage = cursor.getString(imageColumnIndex);
        Uri imageUri = Uri.parse(productImage);


        // Update the TextViews with the attributes for the current stock
        stockUnitName.setText(productName);
        stockUnitPrice.setText(NumberFormat.getCurrencyInstance().format(productPrice));
        stockUnitQuantity.setText(String.valueOf(productQuantity));

        //Update the ImageView with the attributes for the current stock
        Picasso.get()
                .load(imageUri)
                .placeholder(R.drawable.empty)
                .into(stockUnitImage);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int updatedQuantity = productQuantity - 1;

                ContentValues values = new ContentValues();
                values.put(StockEntry._ID, productId);
                values.put(StockEntry.COLUMN_PRODUCT_QUANTITY, updatedQuantity);

                String selection = StockEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(productId)};

                Uri currentInventoryUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI, productId);

                // If productQuantity gets less than 0, it simply return from the function
                // and will not update the productQuantity
                // We will show a Toast message if the stock gets out of stock
                if (productQuantity <= 0) {
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (updatedQuantity >= 0) {
                    int updatedRows = context.getContentResolver().update(currentInventoryUri, values, selection, selectionArgs);
                    if (updatedRows != 0) {
                        stockUnitQuantity.setText(String.valueOf(updatedQuantity));
                    } else {
                        throw new IllegalArgumentException("Failed to update the stock unit");
                    }
                }
            }
        });
    }
}
