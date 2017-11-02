package com.clara.hellosqlite;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class ProductsActivity extends AppCompatActivity {

	//Simple app which uses a SQLite Database
	//Product info database
	//User types in a product name (string) and quantity in stock (int) and presses a button to save
	//App records these in a database
	//App has a search button to look for a specific product

	EditText productNameET;
	EditText productQuantityET;
	EditText searchNameET;
	EditText updateProductQuantityET;

	ListView allProductsListView;

	ProductListAdapter allProductListAdapter;
	Cursor allProductsCursor;

	Button addProductButton;
	Button searchProductsButton;
	Button updateQuantityButton;

	private DatabaseManager dbManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products);

		dbManager = new DatabaseManager(this);

		productNameET = (EditText)findViewById(R.id.add_new_product_name_et);
		productQuantityET = (EditText)findViewById(R.id.add_new_product_quantity_et);
		searchNameET = (EditText)findViewById(R.id.search_et);
		updateProductQuantityET = (EditText)findViewById(R.id.update_quantity_et);

		addProductButton = (Button)findViewById(R.id.add_product_button);
		searchProductsButton = (Button)findViewById(R.id.search_products_button);
		updateQuantityButton = (Button)findViewById(R.id.update_quantity_button);

	allProductsListView = (ListView)findViewById(R.id.all_products_listview);
        allProductsCursor = dbManager.getCursorAll();
        allProductListAdapter = new ProductListAdapter(this, allProductsCursor, false);
        allProductsListView.setAdapter(allProductListAdapter);

		addProductButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String newName = productNameET.getText().toString();
				String newQuantity = productQuantityET.getText().toString();

				if ( newName.length() == 0  || !newQuantity.matches("^\\d+$")) {   //regex validation
					Toast.makeText(ProductsActivity.this, "Please enter a product name and numerical quantity",
							Toast.LENGTH_LONG).show();
					return;
				}

				int quantity = Integer.parseInt(newQuantity);

				if (dbManager.addProduct(newName,quantity)){
                    Toast.makeText(ProductsActivity.this, "Product added to database", Toast.LENGTH_LONG).show();

                    productNameET.getText().clear();
                    productQuantityET.getText().clear();
                    allProductListAdapter.changeCursor(dbManager.getCursorAll());
                } else {
                    Toast.makeText(ProductsActivity.this, newName + " is already in the database",
                            Toast.LENGTH_LONG).show();
                }
			}
		});


		searchProductsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String searchName = searchNameET.getText().toString();
				if ( searchName.equals("")) {
					Toast.makeText(ProductsActivity.this, "Please enter a product to search for",
							Toast.LENGTH_LONG).show();
					return;
				}

				int quantity = dbManager.getQuantityForProduct(searchName);

                if (quantity == -1) {
                    //Product not found
                    Toast.makeText(ProductsActivity.this, "Product " + searchName + " not found",
                            Toast.LENGTH_LONG).show();
                } else {
                    updateProductQuantityET.setText(Integer.toString(quantity));
                }
			}
		});

		updateQuantityButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String newQuantityString = updateProductQuantityET.getText().toString();
				String productName = searchNameET.getText().toString();

				if (!newQuantityString.matches("^\\d+$") || productName.length() == 0) {
					Toast.makeText(ProductsActivity.this, "Please enter a numerical quantity and a product name", Toast.LENGTH_LONG).show();
					return;
				}

				int newQuantity = Integer.parseInt(updateProductQuantityET.getText().toString());

                if (dbManager.updateQuantity(productName, newQuantity)){
                    Toast.makeText(ProductsActivity.this, "Quantity updated", Toast.LENGTH_LONG).show();
                    allProductListAdapter.changeCursor(dbManager.getCursorAll());
                } else {
                    Toast.makeText(ProductsActivity.this, "Product not found in database", Toast.LENGTH_LONG).show();
                }
			}
		});


		//ListView's OnItemLongClickListener to delete product.
		//TODO remember to configure the list view! This template app will crash on this line since allProductsListView is null.
		allProductsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			//The last argument is the value from the database _id column, provided by the ProductListAdapter
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {

                Cursor cursor = (Cursor) allProductListAdapter.getItem(position);

                String name = cursor.getString(1);

                // We can delete by id, no problem, so could simply call dbManager.deleteProduct(id)
                // In this case, we'd like to show a confirmation dialog
                // with the name of the product, so need to get some data about this list item
                // Want the data? Need to call getItem to get the Cursor for this row

                new AlertDialog.Builder(ProductsActivity.this)
                        .setTitle("Delete")
                        .setMessage("Delete " + name + "?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user clicks ok, then delete
                        dbManager.deleteProduct(id);
                        Toast.makeText(ProductsActivity.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                        allProductListAdapter.changeCursor(dbManager.getCursorAll());
                    }
                }).setNegativeButton(android.R.string.cancel, null) // no negative buttn. do nothing on deletion
                        .create().show();
                return false;
            }
		});



}}
