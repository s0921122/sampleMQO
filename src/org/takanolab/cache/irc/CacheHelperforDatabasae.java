/**
 * キャッシュの読み書きをする
 * 
 * @author s0921122
 * @version 3.0
 */

package org.takanolab.cache.irc;

import java.util.HashMap;

import org.takanolab.kGLModel.GLObject;

import android.content.Context;
import android.util.Log;

public class CacheHelperforDatabasae extends CacheDatabaseUtils{

	// ログ出力用
	private static final String TAG = "CacheHelperForDatabase";
	// 保持するキャッシュの数
	private int CACHE_MAX = 2;
	// キャッシュされるたびに引かれる有効期限
	private  int reCastNum = 1;
	// ファイルから読み込むマップ
	private HashMap<String,GLObject[]> cacheTable;
	// Stream操作
	// ログ出力
	private static final boolean LOGFLAG = true;


	/**
	 * コンストラクタ
	 * 
	 * @param con
	 */
	public CacheHelperforDatabasae(Context con){
		startup(con);
		cacheTable = new HashMap<String, GLObject[]>();
		inportCache();
	}

	/**
	 * キャッシュの最大数を変更する．
	 * 
	 * @param num
	 */
	public void setMaxCache(int num){
		CACHE_MAX = num;
	}

	/**
	 * 有効期限の減少値を設定する
	 * 
	 * @param num
	 */
	public void setReCastNum(int num){
		reCastNum = num;
	}

	/**
	 * ファイルからキャッシュデータを読み込む．
	 * 
	 */
	private void inportCache(){
		if(LOGFLAG) Log.d(TAG,"Cache Importing.");
		String[] names = getCachingDataNameAll();
		for(String name : names){
			cacheTable.put(name, ChangeStream.read_object(name));
		}
	}

	/**
	 * キャッシュのデータをファイルに書き出す．
	 * 
	 */
	private void exportCache(){
		if(LOGFLAG) Log.d(TAG,"Cache Exporting");
		String[] names = getCachingDataNameAll();
		for(String name : names){
			ChangeStream.write_object(cacheTable.get(name), name);
		}
	}

	/**
	 * キャッシュのセット及びデータベースの更新をする．
	 * 
	 * @param name
	 * @param obj
	 */
	public void setCacheData(String name, GLObject[] obj){
		String category = "unknown";
		int limit = 0;
		setCacheData(name, category, limit, obj);
	}

	/**
	 * キャッシュのセット及びデータベースの更新をする．
	 * 
	 * @param name
	 * @param category
	 * @param obj
	 */
	public void setCacheData(String name, String category, GLObject[] obj){
		int limit = 0;
		setCacheData(name, category, limit, obj);
	}

	/**
	 * キャッシュのセット及びデータベースの更新をする．
	 * 
	 * @param name
	 * @param category
	 * @param limit
	 * @param obj
	 */
	public void setCacheData(String name, String category, int limit, GLObject[] obj){
		if(LOGFLAG) Log.d(TAG,"Cache Set " + name);
		reCastLimit(reCastNum);
		updateLimitCategory(category, limit/2);

		if(cacheTable.size() >= CACHE_MAX){
			Log.d(TAG,"Remove Low Priority Cache");
			removeLowLimitCache();
		}

		insertorUpdate(name, category, limit);
		cacheTable.put(name, obj);
	}

	/**
	 * キャッシュが存在するか．
	 * 
	 * @param name
	 * @return
	 */
	public boolean isCacheData(String name){
		if(cacheTable.containsKey(name)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * キャッシュを返す
	 * 
	 * @param name
	 * @return
	 */
	public GLObject[] getCacheData(String name){
		update(CacheDatabase.COLUMN_NAME, name, CacheDatabase.COLUMN_LIMIT, 50);
		return cacheTable.get(name);
	}

	/**
	 * 一番優先度の低いものを削除する
	 */
	private void removeLowLimitCache(){
		String item = getLimitLowerItemNameforFirst();
		cacheTable.remove(item);
		dead(item);
		if(LOGFLAG) Log.d(TAG,item + " is Dead");
	}

	/**
	 * キャッシュ全体を削除
	 */
	private void clearCacheTable(){
		if(LOGFLAG) Log.d(TAG,"Table Clear");
		cacheTable.clear();
	}

	/**
	 * ファイルの書き出しとデータベースのクローズ．
	 */
	@Override
	public void close() {
		super.close();
		exportCache();
		clearCacheTable();
	}

}