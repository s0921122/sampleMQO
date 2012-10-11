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
		helper = new DatabaseHelper(con);
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
		Log.d(TAG,"Search : " + name);
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
	 * 
	 * データベースへ挿入します．
	 * 
	 * @version 1.1
	 * @param name モデル名
	 * @param colum カラム名
	 * @param num 数値
	 * @return 成功ならばLowid,失敗なら-1
	 */
	public long insert(String name, String colum, int num){
		Log.d(TAG,"insertData : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		long re = 0;
		ContentValues val = new ContentValues();
		try{
			val.put("model_name", name);
			val.put(colum, num);
			re = db.insert(DatabaseHelper.TABLE_NAME, null,val);
			db.setTransactionSuccessful();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.endTransaction();
			val.clear();
		}
		return re;
	}

	/**
	 * アップデートを行います．
	 * 
	 * @version 1.0
	 * @param name モデル名
	 * @param colum 更新カラム名
	 * @param num 更新する値
	 * @return 適用されたレコード数
	 */
	public int update(String name, String colum, int num){
		Log.d(TAG,"updateData : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		int re = 0;
		ContentValues val = new ContentValues();
		try{
			val.put(colum,num);
			re = db.update(DatabaseHelper.TABLE_NAME, val, "model_name = '" + name + "'", null);
			db.setTransactionSuccessful();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				db.endTransaction();
			}
		return re;
	}
	
	/**
	 * アップデートを行います．
	 * 
	 * @version 1.1
	 * @param name モデル名
	 * @param colum 更新するカラム名
	 * @param untilNow 格納されている数値
	 * @param addNum 新しく足す数値
	 * @return 適用レコード数
	 */
	public int update(String name, String colum, int untilNow, int addNum){
		Log.d(TAG,"updateData : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		int re = 0;
		ContentValues val = new ContentValues();
		try{
			val.put(colum,untilNow + addNum);
			re = db.update(DatabaseHelper.TABLE_NAME, val, "model_name = '" + name + "'", null);
			db.setTransactionSuccessful();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				db.endTransaction();
			}
		return re;
	}
	
	/**
	 * データの挿入または更新を行います．
	 * 
	 * @version 1.1
	 * @param name モデル名
	 * @param colum カラム名
	 * @param num 数値
	 */
	public void weighUpInsert(String name, String colum, int num){
		Log.d(TAG,"Weigh up : " + name);
		Cursor csr = select(name, colum);
		if(csr.moveToFirst()){
			int untilNow = csr.getInt(0);
			update(name, colum, untilNow, num);
			timeStamp(name);
		}else{
			insert(name, colum, num);
			timeStamp(name);
		}
	}
	
	/**
	 * 指定したレコードを削除します．
	 * 
	 * @version 1.1
	 * @param name モデルの名前
	 * @return 適用レコード数
	 */
	public int delete(String name){
		Log.d(TAG,"delete : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		int re=0;
		try{
		re =  db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUM_MODEL_NAME + " = '" + name + "'", null);
		db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			db.endTransaction();
		}
		return re;
	}
	
	/**
	 * テーブルを削除します．<br>
	 * デバッグ用
	 * 
	 * @vesion 1.0
	 */
	public void deleteTable(){
		Log.d(TAG,"Delete Table");
		db = helper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL("delete from " + DatabaseHelper.TABLE_NAME);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * 現在時間をアップデートします．<br>
	 * 格納例  「2012-10-11 22:46:13」
	 * 
	 * @vesion 1.1
	 * @param name モデル名
	 */
	public void timeStamp(String name){
		StringBuilder str = new StringBuilder();
		str.append("UPDATE ").append(DatabaseHelper.TABLE_NAME).append(" SET ")
		.append(DatabaseHelper.COLUM_DATE_HOUR).append(" = datetime('now', 'localtime') WHERE ")
		.append(DatabaseHelper.COLUM_MODEL_NAME).append(" = '").append(name).append("'");
		
		db.execSQL(new String(str));
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
