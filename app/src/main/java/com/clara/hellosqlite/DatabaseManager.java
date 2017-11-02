package com.clara.hellosqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.ContentFrameLayout;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseManager {

	private Context context;
	private SQLHelper helper;
	private SQLiteDatabase db;
	protected static final String DB_NAME = "products.db";

	protected static final int DB_VERSION = 1;
	protected static final String DB_TABLE = "inventory";

	private static final String INT_COL = "_id";
	protected static final String NAME_COL = "product_name";
	protected static final String QUANTITY_COL = "quantity";

	private static final String DB_TAG = "DatabaseManager" ;
	private static final String SQL_TAG = "SQLHelper" ;

	public DatabaseManager(Context c) {
		this.context = c;
		helper = new SQLHelper(c);
		this.db = helper.getWritableDatabase();
	}

	public void close() {
		helper.close(); //Closes the database - very important!

	}


	public int getQuantityForProduct(String productName) {
		String [] cols = {QUANTITY_COL};
		String selection = NAME_COL + " =?";
		String[] selectionArgs = { productName};

		Cursor cursor = db.query(DB_TABLE, cols, selection, selectionArgs, null,null,null);

		if (cursor.getCount() == 1){
			cursor.moveToFirst();
			int quantity = cursor.getInt(0);
			cursor.close();
			return quantity;
		} else {
			return -1; // better way to indicate product not found??
		}
	}

	public boolean updateQuantity(String name, int newQuantity){
		ContentValues updateProduct = new ContentValues();
		updateProduct.put(QUANTITY_COL, newQuantity);
		String[] whereArgs = {name};
		String where = NAME_COL + " =?";

		int rowsChanged = db.update(DB_TABLE, updateProduct, where,whereArgs);

		Log.i(DB_TAG, "Update " + name + " new quantity " + newQuantity +
		" rows modified " + rowsChanged);

		if ( rowsChanged > 0) {
			return true; //if at least one row changed
		} return false;
	}
public boolean deleteProduct(long productId){
	String[] whereArgs = {Long.toString(productId)};
	String where = "_id = ?";
	int rowsDeleted = db.delete(DB_TABLE,where, whereArgs);

	Log.i(DB_TAG, "Delete " + productId + " rows deleted:" + rowsDeleted);

	if (rowsDeleted ==1) {
		return true; //should be 1 row deleted
	}
	return false; //nothing has been deleted so the primary key was likely not assigned
}

	public Cursor getCursorAll() {
		return db.query(DB_TABLE, null, null, null, null, null, NAME_COL);
	}

	public boolean addProduct(String name, int quantity){
		ContentValues newProduct = new ContentValues();
		newProduct.put(NAME_COL, name);
		newProduct.put(QUANTITY_COL, quantity);
		try{
			db.insertOrThrow(DB_TABLE,null,newProduct);
			return true;
		} catch (SQLiteConstraintException sqlce) {
			Log.e(DB_TAG, "error inserting data into table." +
			"Name:" + name + " quantity:" + quantity, sqlce);
			return false;
		}
	}

	public class SQLHelper extends SQLiteOpenHelper {
		public SQLHelper(Context c){
			super(c, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			//Table contains a primary key column, _id which autoincrements - saves you setting the value
			//Having a primary key column is almost always a good idea. In this app, the _id column is used by
			//the list CursorAdapter data source to figure out what to put in the list, and to uniquely identify each element
			//Name column, String
			//Quantity column, int

			String createTable = "CREATE TABLE " + DB_TABLE +
					" (" + INT_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					NAME_COL +" TEXT UNIQUE, " + QUANTITY_COL +" INTEGER);"  ;

			Log.d(SQL_TAG, createTable);
			db.execSQL(createTable);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
			onCreate(db);
			Log.w(SQL_TAG, "Upgrade table - drop and recreate it");
		}
	}
}

