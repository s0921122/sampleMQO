/**
 * データベースクラス
 * 
 * @author s0921122
 * @version 1.0
 * 
 */

package org.takanola.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "PERSONAL_DATABASE";
	public static final String TABLE_NAME = "USER_MANIPULATION";
	
	public DatabaseHelper(Context con){
		super(con, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		createTable(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	private void createTable(SQLiteDatabase database){
        try{
        	// テーブル作成
        	String sql = "CREATE TABLE " + TABLE_NAME + " ("
        			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        			+ "model_name TEXT UNIQUE NOT NULL,"
        			+ "move INTEGER,"
        			+ "rotate INTEGER,"
        			+ "scale INTEGER,"
        			+ "capture INTEGER,"
        			+ "marker INTEGER,"
        			+ "use_select INTEGER,"
        			+ "time_frame TIME,"
        			+ "favorite INTEGER,"
        			+ "date_hour DATE"
        			+ ")";
        	database.execSQL(sql);
        }catch(Exception e){
        	// テーブル作成失敗かすでにあるとき
        	System.out.println(e.toString());
        }
	}

}
