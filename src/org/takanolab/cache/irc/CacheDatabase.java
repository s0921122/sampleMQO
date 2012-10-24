package org.takanolab.cache.irc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CacheDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "CACHE_DATABASE";
	public static final String TABLE_CACHE = "CACHE";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_CATEGORY = "category";
	public static final String COLUMN_LIMIT = "limited";
	public static final String COLUMN_NUMBER = "numbers";
	public static final String COLUMN_ALIVE = "alive";
	
	public CacheDatabase(Context con){
		super(con, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
	}
	
	public void createTable(SQLiteDatabase db){
		String sql;

		StringBuilder sr = new StringBuilder()
		.append("CREATE TABLE ").append( TABLE_CACHE ).append(" ( ")
		.append( COLUMN_ID ).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
		.append( COLUMN_NAME ).append(" TEXT UNIQUE NOT NULL,")
		.append( COLUMN_CATEGORY ).append(" TEXT,")
		.append( COLUMN_NUMBER ).append(" INTEGER DEFAULT 0,")
		.append( COLUMN_LIMIT ).append(" INTEGER DEFAULT 0,")
		.append( COLUMN_ALIVE).append(" INTEGER DEFAULT 0")
		.append(" )");
		sql = new String(sr);
		try{
			db.execSQL(sql);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
