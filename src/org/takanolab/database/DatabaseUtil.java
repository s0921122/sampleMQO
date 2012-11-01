/**
 * データベースの操作をするクラスを作ってみた
 * 
 * @author s0921122
 * @version 1.4
 */

package org.takanolab.database;

import java.util.ArrayList;

import org.takanolab.cache.irc.CacheDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseUtil {
	
	private static final String TAG = "DatabaseActional";
	DatabaseHelper helper = null;
	SQLiteDatabase db = null;
	private static final Boolean Logflag = true;
	
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
	public DatabaseUtil(){
	}
	
	/**
	 * コンテキストを受け取りHelperを作成します．
	 * 
	 * @version 1.0
	 * @param con コンテキスト
	 */
	public DatabaseUtil(Context con){
		startUp(con);
	}

	/**
	 * 初期化
	 * @version 1.4
	 * @param con
	 */
	protected void startUp(Context con){
		if(Logflag) Log.d(TAG,"DatabaseUtil Create!");
		helper = new DatabaseHelper(con);
	}


	/**
	 * 
	 * データベースより検索を行います．
	 * @version 1.4
	 * @param searchColum 検索するカラム名
	 * @param searchValue 検索する値
	 * @param getColum 取得するカラム名
	 * @return 
	 */
	private Cursor search(String searchColum, String searchValue, String... getColum){
		if(Logflag) Log.d(TAG,"search : " + searchValue);
		db = helper.getReadableDatabase();
		
		String colums = "";
		for(String str : getColum){
			colums += str + ",";
		}
		String colum = colums.substring(0,colums.length()-1);
		
		Cursor csr = db.rawQuery("select " + colum + " from " + DatabaseHelper.TABLE_MANIPULATION 
				+ " where " + searchColum + " = '" + searchValue + "'", null);
		return csr;
	}
	
	/**
	 * 
	 * データベースより検索を行います．
	 * 
	 * @version 1.4
	 * @param query SQL文
	 * @return 結果のカーソル
	 */
	private Cursor search(String query){
		if(Logflag) Log.d(TAG,"search : " + query);
		db = helper.getReadableDatabase();
		Cursor csr = db.rawQuery(query, null);
		return csr;
	}
	
	/**
	 * 
	 * 指定したカラムのスコアを取得します．
	 * 
	 * @version 1.4
	 * @param name モデル名
	 * @param colum 取得するカラム
	 * @return スコア
	 */
	public int getScore(String name, String colum){
		Cursor csr = search(DatabaseHelper.COLUM_MODEL_NAME, name, colum);
		int score = csr.getInt(0);
		csr.close();
		return score;
	}
	
	/**
	 * 
	 * データベースから id, model_name, date_hour を除くカラムの合計値を返します．
	 * 
	 * @version 1.3
	 * @param name モデル名
	 * @return スコアの合計値
	 */
	public int getTotalScore(String name){
		int total = 0;
		Cursor csr = search(DatabaseHelper.COLUM_MODEL_NAME, name, "*");
		if(!csr.moveToFirst()){
			return 0;
		}
		String[] colums = csr.getColumnNames();
		for(String index : colums){
			if(index.equals(DatabaseHelper.COLUM_ID) || index.equals(DatabaseHelper.COLUM_MODEL_NAME) ||
					index.equals(DatabaseHelper.COLUM_DATE_HOUR)){
				// 何もしない
			}else{
				// 合計を計算
				total += csr.getInt(csr.getColumnIndex(index));
			}
		}
		return total;
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
	private long insert(String name, String colum, int num){
		if(Logflag) Log.d(TAG,"insertData : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		long re = 0;
		ContentValues val = new ContentValues();
		try{
			val.put(DatabaseHelper.COLUM_MODEL_NAME, name);
			val.put(colum, num);
			re = db.insert(DatabaseHelper.TABLE_MANIPULATION, null,val);
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
	 * データベースへ挿入します
	 * @param val
	 * @return
	 */
	private long insert(ContentValues val){
		if(Logflag) Log.d(TAG,"insertData" + val.getAsString(DatabaseHelper.COLUM_MODEL_NAME));
		db = helper.getWritableDatabase();
		db.beginTransaction();

		long re = 0;
		try{
			re = db.insert(DatabaseHelper.TABLE_MANIPULATION, null,val);
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
	private int update(String name, String colum, int num){
		if(Logflag) Log.d(TAG,"updateData : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		int re = 0;
		ContentValues val = new ContentValues();
		try{			
			val.put(colum, num);
			re = db.update(DatabaseHelper.TABLE_MANIPULATION, val, DatabaseHelper.COLUM_MODEL_NAME + " = '" + name + "'", null);
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
	private int update(String name, String colum, int untilNow, int addNum){
		if(Logflag) Log.d(TAG,"updateData : " + name);
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
			re = db.update(DatabaseHelper.TABLE_MANIPULATION, val, DatabaseHelper.COLUM_MODEL_NAME + " = '" + name + "'", null);
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
		if(Logflag) Log.d(TAG,"Weigh up : " + name);
		// 前回と同じモデル名とカラムなら検索を行わずに更新
		if(storeName == name && storeColum == colum){
			update(name, colum, storeInt, num);
			setTimeStamp(name);
		}else{
			// 検索してヒットするかしないかで処理を分ける
			Cursor csr = search(DatabaseHelper.COLUM_MODEL_NAME, name, DatabaseHelper.COLUM_ID);
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
		if(Logflag) Log.d(TAG,"delete : " + name);
		db = helper.getWritableDatabase();
		db.beginTransaction();
		
		int re=0;
		try{
			re =  db.delete(DatabaseHelper.TABLE_MANIPULATION, DatabaseHelper.COLUM_MODEL_NAME + " = '" + name + "'", null);
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
	private void deleteTable(String tableName){
		if(Logflag) Log.d(TAG,"Delete Table");
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
		str.append("UPDATE ").append(DatabaseHelper.TABLE_MANIPULATION).append(" SET ")
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
	
	/**
	 * テーブルのオートインクリメントをリセットします．
	 * 
	 * @version 1.2
	 * @param tableName リセットしたいテーブル名
	 */
	private void resetAutoincrement(String tableName){
		db.execSQL("update sqlite_sequence set seq = 0 where name='" + tableName + "'");
	}

	public void exportCsv(){
		CsvUtil writer = new CsvUtil(CsvUtil.WRITE_MODE);
		Cursor csr = search("select * from " + DatabaseHelper.TABLE_MANIPULATION);
		while(csr.moveToNext()){
			writer.add(createCsvLine(csr));
		}
		writer.close();
		Log.d("CSV","CSV Export!");
	}
	
	public String createCsvLine(Cursor csr){
		StringBuilder builder = new StringBuilder();
		builder.append(csr.getInt(0)).append(",")
		.append(csr.getString(1)).append(",")
		.append(csr.getInt(2)).append(",")
		.append(csr.getInt(3)).append(",")
		.append(csr.getInt(4)).append(",")
		.append(csr.getInt(5)).append(",")
		.append(csr.getInt(6)).append(",")
		.append(csr.getInt(7)).append(",")
		.append(csr.getInt(8)).append(",")
		.append(csr.getInt(9)).append(",")
		.append(csr.getString(10));
		return builder.toString();
	}
	
	public void importCsv(){
		CsvUtil reader = new CsvUtil(CsvUtil.READ_MODE);
		ArrayList<String> line = reader.importCSV();
		String[] colums;
		for(String str : line){
			Log.d("CSV","Loop:" + str);
			colums = str.split(",", -1);
			insert(setValues(colums));	
		}
		reader.close();
		Log.d("CSV","CSV Import!" + line.size());
	}

	private ContentValues setValues(String[] strs){
		ContentValues val = new ContentValues();
		val.put(DatabaseHelper.COLUM_ID, strs[0]);
		val.put(DatabaseHelper.COLUM_MODEL_NAME, strs[1]);
		val.put(DatabaseHelper.COLUM_MOVE, strs[2]);
		val.put(DatabaseHelper.COLUM_ROTATE, strs[3]);
		val.put(DatabaseHelper.COLUM_SCALE, strs[4]);
		val.put(DatabaseHelper.COLUM_CAPTURE, strs[5]);
		val.put(DatabaseHelper.COLUM_MARKER, strs[6]);
		val.put(DatabaseHelper.COLUM_USER_SELECT, strs[7]);
		val.put(DatabaseHelper.COLUM_TIME_FRAME, strs[8]);
		val.put(DatabaseHelper.COLUM_FAVORITE, strs[9]);
		val.put(DatabaseHelper.COLUM_DATE_HOUR, strs[10]);
		return val;
	}

	
}
