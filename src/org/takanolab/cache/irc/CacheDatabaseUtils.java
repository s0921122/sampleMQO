package org.takanolab.cache.irc;

import android.app.SearchableInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CacheDatabaseUtils {
	private static final String TAG = "CacheDatabaseUtil";
	private static final String NUMBER = "insertNumber";
	CacheDatabase database;
	SQLiteDatabase db;
	private int num = 0;
	
	private static final boolean Logflag = true;
	
	/**
	 * コンストラクタ
	 * 
	 * @version 1.0
	 * @param con
	 */
	public CacheDatabaseUtils(Context con){
		database = new CacheDatabase(con);
		if(!search("select " + CacheDatabase.COLUMN_NUMBER + " from " + CacheDatabase.TABLE_CACHE +
				" where " + CacheDatabase.COLUMN_NAME + " = '" + NUMBER + "'")
				.moveToFirst())
		{			
			setNumbers(num);
		}else{
			Cursor csr = search("select " + CacheDatabase.COLUMN_NUMBER + " from " + CacheDatabase.TABLE_CACHE + 
					" where " + CacheDatabase.COLUMN_NAME + " = '" + NUMBER + "'");
			csr.moveToFirst();
			num = csr.getInt(0);
			Log.d(TAG,"db num = " + csr.getInt(0) + "\nnum = " + num);
			csr.close();
		}
	}
	
	private Boolean setNumbers(int num){
		db = database.getWritableDatabase();
		db.beginTransaction();
		ContentValues val = new ContentValues();
		try{
			val.put(CacheDatabase.COLUMN_NAME, NUMBER);
			val.put(CacheDatabase.COLUMN_CATEGORY, "system");
			val.put(CacheDatabase.COLUMN_NUMBER, num);
			val.put(CacheDatabase.COLUMN_ALIVE, 0);
			long re = db.insert(CacheDatabase.TABLE_CACHE, null ,val);
			val.clear();
			db.setTransactionSuccessful();
			db.endTransaction();
			if(re > 0){
				return true;
			}
		}catch (Exception e) {
			// あったらあったらでおｋ
			e.printStackTrace();
			db.endTransaction();
		}
		return false;
	}

	/**
	 * 
	 * データベースから検索を行う．
	 * 
	 * @param searchColum
	 * @param searchValue
	 * @param getColum
	 * @return
	 */
	private Cursor search(String searchColum, String searchValue, String... getColum){
		if(Logflag) Log.d(TAG,"search : " + searchValue);
		db = database.getReadableDatabase();
		
		String colums = "";
		for(String str : getColum){
			colums += str + ",";
		}
		String colum = colums.substring(0,colums.length()-1);
		
		Cursor csr = db.rawQuery("select " + colum + " from " + CacheDatabase.TABLE_CACHE + " where "
				+ searchColum + " = '" + searchValue + "' and " + CacheDatabase.COLUMN_ALIVE + " = 1 order by " + CacheDatabase.COLUMN_NUMBER + " asc", null);
		return csr;
	}
	
	/**
	 * 
	 * データベースから検索を行う．
	 * 
	 * @param searchColum
	 * @param searchValue
	 * @param getColum
	 * @return
	 */
	private Cursor search(String searchColum, int searchValue, String... getColum){
		if(Logflag) Log.d(TAG,"search : " + searchValue);
		db = database.getReadableDatabase();
		
		String colums = "";
		for(String str : getColum){
			colums += str + ",";
		}
		
		String colum = colums.substring(0,colums.length()-1);
		Cursor csr = db.rawQuery("select " + colum + " from " + CacheDatabase.TABLE_CACHE + " where "
				+ searchColum + " = " + searchValue + " and " + CacheDatabase.COLUMN_ALIVE + " = 1 order by " + CacheDatabase.COLUMN_NUMBER + " asc", null);
		return csr;
	}
	
	private Cursor search(String query){
		db = database.getReadableDatabase();
		return db.rawQuery(query, null);
	}
	
	/**
	 * 
	 * データの挿入を行う．
	 * 
	 * @param name
	 * @param category
	 * @param limit
	 * @param live
	 * @return
	 */
	private boolean insert(String name, String category, int limit){
		if(Logflag) Log.d(TAG,"insert : " + name + " , " + category + " , " + limit);
		updateLimitCategory(category, limit/2);
		db = database.getWritableDatabase();
		db.beginTransaction();
		
		try{
			ContentValues val = new ContentValues();
			val.put(CacheDatabase.COLUMN_NAME, name);
			val.put(CacheDatabase.COLUMN_CATEGORY, category);
			val.put(CacheDatabase.COLUMN_LIMIT, limit);
			val.put(CacheDatabase.COLUMN_NUMBER, num);
			val.put(CacheDatabase.COLUMN_ALIVE, 1);
			db.insert(CacheDatabase.TABLE_CACHE, null , val);
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			return false;
		}
	}
	
	/**
	 * 
	 * insertかupdateをしてくれます．
	 * 
	 * @param name
	 * @param category
	 * @param limit
	 */
	public void insertorUpdate(String name, String category, int limit){
		Cursor csr = search(CacheDatabase.COLUMN_NAME, name, CacheDatabase.COLUMN_NAME, CacheDatabase.COLUMN_LIMIT);
		if(csr.moveToFirst()){
			if(csr.getInt(1) == 0){
				ContentValues val = new ContentValues();
				val.put(CacheDatabase.COLUMN_NUMBER, num);
				val.put(CacheDatabase.COLUMN_ALIVE, 1);
				val.put(CacheDatabase.COLUMN_LIMIT, limit);
				update(CacheDatabase.COLUMN_NAME, name, val);
				num++;
				Log.d(TAG,"num = " + num);
			}else{
				update(CacheDatabase.COLUMN_NAME, name, CacheDatabase.COLUMN_LIMIT, limit);
			}
		}else{
			insert(name, category, limit);
			num++;
			Log.d(TAG,"num = " + num);
		}
		csr.close();
	}
	
	/**
	 * 
	 * アップデートを行う．
	 * 
	 * @param searchColum
	 * @param searchValue
	 * @param updateColum
	 * @param update
	 * @return
	 */
	public boolean update(String searchColum, String searchValue, String updateColum, String update){
		if(Logflag) Log.d(TAG,"update : " + searchValue + " , " + updateColum + " , " + update);
		db = database.getWritableDatabase();
		db.beginTransaction();
		try{
			ContentValues val = new ContentValues();
			val.put(updateColum, update);
//			val.put(CacheDatabase.COLUMN_ALIVE,1);
			db.update(CacheDatabase.TABLE_CACHE, val , searchColum  + " = '" + searchValue + "'", null);
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			return false;
		}
	}
	
	/**
	 * 
	 * アップデートを行う．
	 * 
	 * @param searchColum
	 * @param searchValue
	 * @param updateColum
	 * @param update
	 * @return
	 */
	public boolean update(String searchColum, String searchValue, String updateColum, int update){
		if(Logflag) Log.d(TAG,"update : " + searchValue + " , " + updateColum + " , " + update);
		db = database.getWritableDatabase();
		db.beginTransaction();
		try{
			ContentValues val = new ContentValues();
			val.put(updateColum, update);
//			val.put(CacheDatabase.COLUMN_ALIVE, 1);
			db.update(CacheDatabase.TABLE_CACHE, val , searchColum  + " = '" + searchValue + "'", null);
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			return false;
		}
	}
	
	/**
	 * 
	 * アップデートを行う．
	 * 
	 * @param searchColum
	 * @param searchValue
	 * @param val
	 * @return
	 */
	public boolean update(String searchColum, String searchValue, ContentValues val){
		if(Logflag) Log.d(TAG,"update : " + searchValue);
		db = database.getWritableDatabase();
		db.beginTransaction();
		try{
			db.update(CacheDatabase.TABLE_CACHE, val , searchColum  + " = '" + searchValue + "'", null);
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			db.endTransaction();
			return false;
		}
	}
	
	/**
	 * 
	 * categoryが一致するアイテムのlimitにaddを加算
	 * 
	 * @param category
	 * @param add
	 * @return
	 */
	public boolean updateLimitCategory(String category, int add){
		if(Logflag) Log.d(TAG,"category update : " + category + " , " + add);
		Cursor csr = search(CacheDatabase.COLUMN_CATEGORY, category, CacheDatabase.COLUMN_NAME, CacheDatabase.COLUMN_LIMIT);
		while(csr.moveToNext()){
			update(CacheDatabase.COLUMN_NAME, csr.getString(0), CacheDatabase.COLUMN_LIMIT, csr.getInt(1) + add);
		}
		csr.close();
		return true;
	}
	
	/**
	 * 
	 * nameの一致するアイテムを削除します．（非推奨）
	 * 
	 * @param name
	 * @return
	 */
	public boolean delete(String name){
		if(Logflag) Log.d(TAG,"delete : " + name);
		db = database.getWritableDatabase();
		db.beginTransaction();
		try{
			db.delete(CacheDatabase.TABLE_CACHE, CacheDatabase.COLUMN_NAME + " = '" + name + "'", null);
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * idの一致するアイテムを削除します．（非推奨）
	 * @param id
	 * @return
	 */
	public boolean delete(int id){
		if(Logflag) Log.d(TAG,"delete : " + id);
		db = database.getWritableDatabase();
		db.beginTransaction();
		try{
			db.delete(CacheDatabase.TABLE_CACHE, CacheDatabase.COLUMN_ID + " = " + id, null);
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * 生存フラグを消します．
	 * 
	 * @param name
	 * @return
	 */
	public boolean dead(String name){
		if(Logflag) Log.d(TAG,"dead item " + name);
		return update(CacheDatabase.COLUMN_NAME, name, CacheDatabase.COLUMN_ALIVE, 0);
	}
	
	/**
	 * limitの値をdownの値減らす．
	 * 
	 * @param down
	 */
	public void reCastLimit(int down){
		Cursor csr = search(CacheDatabase.COLUMN_ALIVE, 1, CacheDatabase.COLUMN_NAME, CacheDatabase.COLUMN_LIMIT);
		while(csr.moveToNext()){
			update(CacheDatabase.COLUMN_NAME, csr.getString(0), CacheDatabase.COLUMN_LIMIT, csr.getInt(1) - down);
		}
		csr.close();
	}
	
	/**
	 * nameの一致するアイテムのlimitを返す．
	 * 
	 * @param name
	 * @return
	 */
	public int getLimitValue(String name){
		Cursor csr = search(CacheDatabase.COLUMN_NAME, name, CacheDatabase.COLUMN_LIMIT);
		if(csr.moveToFirst()){
			return csr.getInt(0);
		}else{
			return 0;
		}
	}
	
	/**
	 * 一番limitの低いアイテムの名前を返す．
	 * 
	 * @return
	 */
	public String getLimitLowerItemNameforFirst(){
		Cursor csr = search("select " + CacheDatabase.COLUMN_NAME + "," + CacheDatabase.COLUMN_LIMIT + " from " + CacheDatabase.TABLE_CACHE 
				+ " where " + CacheDatabase.COLUMN_ALIVE + " = 1 order by " + CacheDatabase.COLUMN_LIMIT + " asc," + CacheDatabase.COLUMN_NUMBER + " asc");
		csr.moveToFirst();
		if(Logflag) Log.d(TAG,"Lower Item : " + csr.getString(0) +" : "+ csr.getInt(1));
		return csr.getString(0);
	}

	/**
	 * 
	 * 現在キャッシュされているアイテムを返す．
	 * 
	 * @return
	 */
	public String[] getCachingDataNameAll(){
		if(Logflag) Log.d(TAG,"CacheingData :");
		Cursor csr = search("", "", CacheDatabase.COLUMN_NAME);
		String[] names = new String[csr.getCount()];
		int i = 0;
		while(csr.moveToNext()){
			names[i] = csr.getString(0);
			if(Logflag) Log.d(TAG,names[i]);
			i++;
		}
		csr.close();
		return names;
	}
	
	/**
	 * テーブルのオートインクリメントをリセットします．
	 * 
	 * @version 1.0
	 * @param tableName リセットしたいテーブル名
	 */
	private void resetAutoincrement(String tableName){
		db.execSQL("update sqlite_sequence set seq = 0 where name='" + tableName + "'");
	}
	
	/**
	 * 指定したテーブルを削除します．<br>
	 * 
	 * @version 1.0
	 * @param tableName 削除するテーブル名
	 */
	public void deleteTable(String tableName){
		db = database.getWritableDatabase();
		db.beginTransaction();
		db.execSQL("delete from " + tableName);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * テーブル削除して、新しくテーブル作成します．<br>
	 * ついでにオートインクリメントもリセット．
	 * 
	 * @version 1.0
	 * @param tableName 再生成するテーブル名
	 */
	public void reCreateTable(String tableName){
		deleteTable(tableName);
		database.createTable(db);
		resetAutoincrement(tableName);
	}

	/**
	 * 終了処理
	 */
	public void close(){
		Log.d(TAG,"colsenum = " + num);
		update(CacheDatabase.COLUMN_NAME, NUMBER, CacheDatabase.COLUMN_NUMBER, num);
		db.close();
		database.close();
	}
}
