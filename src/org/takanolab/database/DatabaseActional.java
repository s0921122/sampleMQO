/**
 * データベースの操作をするクラスを作ってみた
 * 
 * @author s0921122
 * @version 1.2
 */

package org.takanolab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseActional {
	
	private static final String TAG = "DatabaseActional";
	DatabaseHelper helper = null;
	SQLiteDatabase db = null;
	
	// 前回更新のモデル名を保持
	String storeName = "";
	// 前回更新のカラム名を保持
	String storeColum = "";
	// 前回更新の値を保持
	int storeInt = 0;
	
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
		Cursor csr = db.rawQuery("select " + colum + " from " + DatabaseHelper.MANIPULATION_TABLE + " where model_name = '" + name + "'", null);
		return csr;
	}
	
	/**
	 * カーソルから内容を取得します．（int専用）
	 * 
	 * @version 1.2
	 * @param csr 検索結果のカーソル
	 * @return カラムの内容(int)
	 */
	public int getNum(Cursor csr){
		csr.moveToFirst();
		int num = csr.getInt(0);
		csr.close();
		return num;
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
			re = db.insert(DatabaseHelper.MANIPULATION_TABLE, null,val);
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
	 * @version 1.2
	 * @param name 更新するモデル名
	 * @param colum 更新するカラム名
	 * @param num 更新する数値
	 * @return 適用レコード数
	 */
	public int update(String name, String colum, int num){
		Log.d(TAG,"updateData : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		int re = 0;
		ContentValues val = new ContentValues();
		try{			
			val.put(colum, num);
			re = db.update(DatabaseHelper.MANIPULATION_TABLE, val, "model_name = '" + name + "'", null);
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
	 * @version 1.2
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
			// 再利用できるように変数に格納
			storeName = name;
			storeColum = colum;
			int newNum = storeInt = untilNow + addNum;
			
			val.put(colum, newNum);
			re = db.update(DatabaseHelper.MANIPULATION_TABLE, val, "model_name = '" + name + "'", null);
			db.setTransactionSuccessful();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				db.endTransaction();
			}
		return re;
	}
	
	/**
	 * データがデータベースにあるかを判断し、<br>
	 * データの挿入または更新を行います．
	 * 
	 * @version 1.2
	 * @param name モデル名
	 * @param colum カラム名
	 * @param num 数値
	 */
	public void weighUpInsert(String name, String colum, int num){
		Log.d(TAG,"Weigh up : " + name);
		// 前回と同じモデル名とカラムなら検索を行わずに更新
		if(storeName == name && storeColum == colum){
			update(name, colum, storeInt, num);
			setTimeStamp(name);
		}else{
			// 検索してヒットするかしないかで処理を分ける
			Cursor csr = select(name, colum);
			if(csr.moveToFirst()){
				int untilNow = csr.getInt(0);
				update(name, colum, untilNow, num);
				setTimeStamp(name);
				csr.close();
			}else{
				insert(name, colum, num);
				setTimeStamp(name);
				csr.close();
			}
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
			re =  db.delete(DatabaseHelper.MANIPULATION_TABLE, DatabaseHelper.COLUM_MODEL_NAME + " = '" + name + "'", null);
		db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			db.endTransaction();
		}
		return re;
	}
	
	/**
	 * 指定したテーブルを削除します．<br>
	 * 
	 * @version 1.1
	 * @param tableName 削除するテーブル名
	 */
	public void deleteTable(String tableName){
		Log.d(TAG,"Delete Table");
		db = helper.getWritableDatabase();
		db.beginTransaction();
		db.execSQL("delete from " + tableName);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * 現在時間をアップデートします．<br>
	 * 格納例  「2012-10-11 22:46:13」
	 * 
	 * @version 1.1
	 * @param name モデル名
	 */
	private void setTimeStamp(String name){
		StringBuilder str = new StringBuilder();
		str.append("UPDATE ").append(DatabaseHelper.MANIPULATION_TABLE).append(" SET ")
		.append(DatabaseHelper.COLUM_DATE_HOUR).append(" = datetime('now', 'localtime') WHERE ")
		.append(DatabaseHelper.COLUM_MODEL_NAME).append(" = '").append(name).append("'");
		
		db.execSQL(new String(str));
	}
	
	/**
	 * 終了処理．<br>
	 * DatabaseHelperとSQLiteDatabaseをcloseします．
	 * 
	 * @version 1.0
	 */
	public void close(){
		db.close();
		helper.close();
	}
	
	/**
	 * テーブルのオートインクリメントをリセットします．
	 * 
	 * @version 1.2
	 * @param tableName リセットしたいテーブル名
	 */
	private void resetAutoincrement(String tableName){
		db.execSQL("update sqlite_sequence set seq = 0 where name='" + tableName + "'");
	}
	
	/**
	 * テーブル削除して、新しくテーブル作成します．<br>
	 * ついでにオートインクリメントもリセット．
	 * 
	 * @version 1.2
	 * @param tableName 再生成するテーブル名
	 */
	public void reCreateTable(String tableName){
		deleteTable(tableName);
		helper.createTable(db);
		resetAutoincrement(tableName);
	}
}
