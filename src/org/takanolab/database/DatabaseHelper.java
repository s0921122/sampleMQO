/**
 * データベースクラス
 * 
 * @author s0921122
 * @version 1.1
 * 
 */

package org.takanolab.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "PERSONAL_DATABASE";
	public static final String MANIPULATION_TABLE = "USER_MANIPULATION";
	public static final String CREATURE_TABLE = "CREATURE";
	
	// カラム名フィールド
	public static final String COLUM_ID = "_id";
	public static final String COLUM_MODEL_NAME = "model_name";
	public static final String COLUM_MOVE = "move";
	public static final String COLUM_ROTATE = "rotate";
	public static final String COLUM_SCALE = "scale";
	public static final String COLUM_CAPTURE = "capture";
	public static final String COLUM_MARKER = "marker";
	public static final String COLUM_USER_SELECT = "user_select";
	public static final String COLUM_TIME_FRAME = "time_frame";
	public static final String COLUM_FAVORITE = "favorite";
	public static final String COLUM_DATE_HOUR = "date_hour";
			
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
	
	public void createTable(SQLiteDatabase database){
        try{
        	String sql;
        	// テーブル作成
        	
//        	sql = "CREATE TABLE " + TABLE_NAME + " ("
//        			+ COLUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//        			+ COLUM_MODEL_NAME + " TEXT UNIQUE NOT NULL,"
//        			+ COLUM_MOVE + " INTEGER,"
//        			+ COLUM_ROTATE + " INTEGER,"
//        			+ COLUM_SCALE + " INTEGER,"
//        			+ COLUM_CAPTURE + " INTEGER,"
//        			+ COLUM_MARKER + " INTEGER,"
//        			+ COLUM_USER_SELECT + " INTEGER,"
//        			+ COLUM_TIME_FRAME + " TIME,"
//        			+ COLUM_FAVORITE + " INTEGER,"
//        			+ COLUM_DATE_HOUR + " DATE"
//        			+ ")";
        	
        	StringBuilder sr = new StringBuilder()
        	.append("CREATE TABLE ").append( MANIPULATION_TABLE ).append(" ( ")
        	.append( COLUM_ID ).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
        	.append( COLUM_MODEL_NAME ).append(" TEXT UNIQUE NOT NULL,")
        	.append( COLUM_MOVE ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_ROTATE ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_SCALE ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_CAPTURE ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_MARKER ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_USER_SELECT ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_TIME_FRAME ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_FAVORITE ).append(" INTEGER DEFAULT 0,")
        	.append( COLUM_DATE_HOUR ).append(" DATE")
        	.append(" )");
        	sql = new String(sr);
        	
        	database.execSQL(sql);
        }catch(Exception e){
        	// テーブル作成失敗かすでにあるとき
        	System.out.println(e.toString());
        }
	}

}
