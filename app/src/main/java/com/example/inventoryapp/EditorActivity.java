package com.example.inventoryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inventoryapp.data.StockContract.StockEntry;
import com.squareup.picasso.Picasso;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText productNameEditText, stockQtyEditText, stockUnitPriceEditText, supplierNameEditText, supplierPhoneNumberEditText;
    private Button decreaseBtn, increaseBtn, uploadImageButton, orderButton;

    private Uri currentUri;
    private static final int EDITOR_LOADER_ID = 1;

    private static final int PICK_IMAGE_REQUEST = 2;
    private ImageView productImageView;
    private Uri imageUri;

    private boolean mStockClicked = false;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mStockClicked = true;
            return false;
        }
    };

    private int quantity = 0;
    String supplierContact;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //initializeUIElements();
        initializeUIElements();

        Intent intent = getIntent();
        currentUri = intent.getData();

        if (currentUri == null) {
            setTitle(getString(R.string.add_a_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_item));
            // When user clicks on some items, order button will be displayed
            // from where user can order the product
            orderButton.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);
        }

        // When user click '-' Button
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This condition is invoked when user try to enter new item and directly starts pressing
                // decreaseBtn('-')
                if (stockQtyEditText.getText().toString().trim().isEmpty()) {

                    if (quantity <= 0) {
                        Toast.makeText(EditorActivity.this, getString(R.string.orders_cannot_be_less_than_zero), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    quantity--;
                    stockQtyEditText.setText(String.valueOf(quantity));
                } else {
                    // This condition is invoked when user try to update item and directly starts pressing
                    // decreaseBtn('-')
                    int quantityOfStock = Integer.parseInt(stockQtyEditText.getText().toString().trim());
                    if (quantityOfStock <= 0) {
                        Toast.makeText(EditorActivity.this, getString(R.string.you_cannot_have_less_than_zero_stock), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    quantityOfStock--;
                    stockQtyEditText.setText(String.valueOf(quantityOfStock));
                }
            }
        });

        // When user click '+' Button
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This condition is invoked when user try to enter new item and directly starts pressing
                // increaseBtn('+')
                if (stockQtyEditText.getText().toString().trim().isEmpty()) {

                    quantity++;
                    stockQtyEditText.setText(String.valueOf(quantity));

                } else {
                    // This condition is invoked when user try to update item and directly starts pressing
                    // increaseBtn('+')
                    int quantityOfStock = Integer.parseInt(stockQtyEditText.getText().toString().trim());
                    quantityOfStock++;
                    stockQtyEditText.setText(String.valueOf(quantityOfStock));

                }
            }
        });

        // When user clicks on Choose Image Button
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(EditorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            }
        });

        // Invoked only when updating item
        // Order Button will be invisible when adding new product while
        // it will be visible when updating the product
        // the user can order the product via the number provided in the database for particular product
        // from supplier
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderProductFromSupplier(v);
            }
        });
    }

    /**
     * This method initializes all UI elements used in this Activity
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeUIElements() {

        // Finding all the relevant views that we will need to read the user input from
        productNameEditText = (EditText) findViewById(R.id.product_name_edit_txt);
        stockQtyEditText = (EditText) findViewById(R.id.stock_qty_edit_txt);
        stockUnitPriceEditText = (EditText) findViewById(R.id.stock_unit_price_edit_txt);
        supplierNameEditText = (EditText) findViewById(R.id.supplier_name_edit_txt);
        supplierPhoneNumberEditText = (EditText) findViewById(R.id.supplier_phone_number_edit_txt);
        decreaseBtn = (Button) findViewById(R.id.neg_qty_button);
        increaseBtn = (Button) findViewById(R.id.pos_qty_button);
        productImageView = (ImageView) findViewById(R.id.product_image);
        uploadImageButton = (Button) findViewById(R.id.add_image_button);
        orderButton = (Button) findViewById(R.id.order_button);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        productNameEditText.setOnTouchListener(onTouchListener);
        stockQtyEditText.setOnTouchListener(onTouchListener);
        stockUnitPriceEditText.setOnTouchListener(onTouchListener);
        supplierNameEditText.setOnTouchListener(onTouchListener);
        supplierPhoneNumberEditText.setOnTouchListener(onTouchListener);
        productImageView.setOnTouchListener(onTouchListener);
        decreaseBtn.setOnTouchListener(onTouchListener);
        increaseBtn.setOnTouchListener(onTouchListener);
    }

    // This will pop the dialog box regarding permission to access the EXTERNAL STORAGE
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        } else {
            // We will show user with Toast message regarding the Permission Denied to read EXTERNAL STORAGE
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // When Permission is given
    // This will select the image and will add the image to the productImageView using Picasso Library
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // this will get the uri for selected image
            imageUri = data.getData();
            // after uri is added to the imageUri, it is passed to the Picasso.get().load() method to upload
            // the desired image
            Picasso.get()
                    .load(imageUri)
                    .into(productImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_save:
                // insert new product
                saveData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mStockClicked) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener = (dialogInterface, i) ->
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get user input from editor and save stock unit into database.
     */
    private void saveData() {
        // Reading from input fields
        // Using trim to eliminate leading or trailing white space
        String productName = productNameEditText.getText().toString().trim();
        String productPrice = stockUnitPriceEditText.getText().toString().trim();
        String productQty = stockQtyEditText.getText().toString().trim();
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierContact = supplierPhoneNumberEditText.getText().toString().trim();

        //This will check if all the information has been entered and if any field is left blank the
        // then we will show Toast message to the user
        if (currentUri == null && (TextUtils.isEmpty(productName) || TextUtils.isEmpty(supplierName)
                || TextUtils.isEmpty(supplierContact)
                || TextUtils.isEmpty(productPrice) || TextUtils.isEmpty(productQty))) {
            Toast.makeText(this, getString(R.string.necessary_details), Toast.LENGTH_SHORT).show();
            return;
        }

        // This will be invoked only when there is updating of product item
        if ((TextUtils.isEmpty(productName) || TextUtils.isEmpty(supplierName) || TextUtils.isEmpty(supplierContact)
                || TextUtils.isEmpty(productPrice) || TextUtils.isEmpty(productQty))) {
            Toast.makeText(this, getString(R.string.necessary_details), Toast.LENGTH_SHORT).show();
            return;
        }

        String productImage = imageUri.toString();
        float stockPrice = Float.parseFloat(productPrice);
        int stockQty = Integer.parseInt(productQty);

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();

        values.put(StockEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(StockEntry.COLUMN_PRODUCT_PRICE, stockPrice);
        values.put(StockEntry.COLUMN_PRODUCT_QUANTITY, stockQty);
        values.put(StockEntry.COLUMN_PRODUCT_IMAGE, productImage);
        values.put(StockEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(StockEntry.COLUMN_SUPPLIER_CONTACT, supplierContact);

        // Add Data
        if (currentUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(StockEntry.CONTENT_URI, values);

            // Showing a Toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.error_in_saving_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
                // Exit activity
                finish();
            }
        } else {
            // Update Data
            int updatedRows = getContentResolver().update(currentUri, values, null, null);

            // Showing a Toast message depending on whether or not the update was successful.
            if (updatedRows == 0) {
                Toast.makeText(this, getString(R.string.error_in_updating_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_updated), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the stocks table
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_PRODUCT_NAME,
                StockEntry.COLUMN_PRODUCT_PRICE,
                StockEntry.COLUMN_PRODUCT_QUANTITY,
                StockEntry.COLUMN_PRODUCT_IMAGE,
                StockEntry.COLUMN_SUPPLIER_NAME,
                StockEntry.COLUMN_SUPPLIER_CONTACT
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                currentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Finding the columns of product attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(StockEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(StockEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = data.getColumnIndex(StockEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = data.getColumnIndex(StockEntry.COLUMN_PRODUCT_IMAGE);
            int supplierNameColumnIndex = data.getColumnIndex(StockEntry.COLUMN_SUPPLIER_NAME);
            int supplierContactColumnIndex = data.getColumnIndex(StockEntry.COLUMN_SUPPLIER_CONTACT);

            // Extract out the value from the Cursor for the given column index
            String productName = data.getString(nameColumnIndex);
            float productPrice = data.getInt(priceColumnIndex);
            int productQuantity = data.getInt(quantityColumnIndex);
            String productImage = data.getString(imageColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            supplierContact = data.getString(supplierContactColumnIndex);

            // Update the views on the screen with the values from the database
            productNameEditText.setText(productName);
            stockUnitPriceEditText.setText(String.valueOf(productPrice));
            stockQtyEditText.setText(String.valueOf(productQuantity));
            imageUri = Uri.parse(productImage);
            supplierNameEditText.setText(supplierName);
            supplierPhoneNumberEditText.setText(supplierContact);

            Picasso.get()
                    .load(imageUri)
                    .placeholder(R.drawable.empty)
                    .into(productImageView);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.discard_your_changes_and_quit_editing));

        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        //If the product hasn't changed, continue with handling back button press
        if (!mStockClicked) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteStock() {
        // Only perform the delete if this is an existing product.
        if (currentUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selectionArgs because the currentUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);

            // Showing a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.error_in_deleting_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_deleted), Toast.LENGTH_SHORT).show();
            }
        }
        // Close the  detail activity
        finish();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_dialog_msg);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteStock();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Orders the product from suppliers using their phone numbers.
     */
    public void orderProductFromSupplier(View view) {

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierContact));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}