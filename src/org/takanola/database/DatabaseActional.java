/**
 * データベースの操作をするクラスを作ってみた
 * 
 * @author s0921122
 */

package org.takanola.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseActional {
	
	private static final String TAG = "DatabaseActional";
	DatabaseHelper helper = null;
	SQLiteDatabase db;
	
	/**
	 * 何もないです
	 * 
	 * @version 1.0
	 */
	public DatabaseActional(){
	}
	
	/**
	 * コンテキストを受け取りHelperを作成保持します．<br>
	 * 通常はこれを使ってください．
	 * 
	 * @version 1.0
	 * @param con コンテキスト
	 */
	public DatabaseActional(Context con){
		helper = createHelper(con);
	}
	
	/**
	 * DatabaseHelperを作成します．
	 * 
	 * @param con コンテキスト
	 * @return コンテキストから生成されたDatabaseHelper
	 */
	static public DatabaseHelper createHelper(Context con){
		return new DatabaseHelper(con);
	}
	
	/**
	 * 
	 * データベースより検索を行います．
	 * 
	 * @version 1.0
	 * @param name 検索するモデルの名前
	 * @param colum 取得するカラム名
	 * @return 結果のカーソル
	 */
	public Cursor select(String name, String colum){
		db = helper.getReadableDatabase();
		Cursor csr = db.rawQuery("select " + colum + " from " + DatabaseHelper.TABLE_NAME + " where model_name = '" + name + "'", null);
		return csr;
	}
	
	/**
	 * カーソルから内容を取得します．（int専用）
	 * 
	 * @param csr 検索結果のカーソル
	 * @return カラムの内容(int)
	 */
	public int[] getNums(Cursor csr){
		int i = 0;
		int[] nums = new int[csr.getCount()];
		while(csr.moveToNext()){
			nums[i] = csr.getInt(0);
			i++;
		}
		csr.close();
		return nums;
	}
	
	/**
	 * データベースへ挿入します．
	 * 
	 * @version 1.0
	 * @param name モデルの名前
	 * @param colum 格納するカラム名
	 * @param num 格納する数値
	 */
	public void insert(String name, String colum, int num){
		Log.d(TAG,"insertData");
		db = helper.getWritableDatabase();
		db.beginTransaction();

		ContentValues val = new ContentValues();
		try{
			val.put("model_name", name);
			val.put(colum, num);
			db.insert(DatabaseHelper.TABLE_NAME, null,val);
			db.setTransactionSuccessful();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.endTransaction();
			val.clear();
		}
	}
	
	/**
	 * アップデートをします．
	 * 
	 * @version 1.0
	 * @param name モデルの名前
	 * @param colum　更新カラム名
	 * @param num 更新する数値
	 */
	public void update(String name, String colum, int num){
		Log.d(TAG,"updateData");
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		ContentValues val = new ContentValues();
		try{
			val.put(colum,num);
			db.update(DatabaseHelper.TABLE_NAME, val, "model_name = '" + name + "'", null);
			db.setTransactionSuccessful();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				db.endTransaction();
			}
	}
	
	/**
	 * テーブルを削除します．<br>
	 * デバッグ用
	 * 
	 */
	public void deleteTable(){
		db = helper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL("delete from " + DatabaseHelper.TABLE_NAME);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * 終了処理．<br>
	 * DatabaseHelperとSQLiteDAtabaseをclose．
	 * 
	 * @version 1.0
	 */
	public void close(){
		db.close();
		helper.close();
	}

}
