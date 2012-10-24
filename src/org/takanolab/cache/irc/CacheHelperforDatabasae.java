/**
 * キャッシュの読み書きをする
 * 
 * @author s0921122
 * @version 2.0
 */

package org.takanolab.cache.irc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.takanolab.kGLModel.KGLModelData;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class CacheHelperforDatabasae extends CacheDatabaseUtils{

	// ログ出力用
	private static final String TAG = "CacheHelperForDatabase";
	// キャッシュファイルの入出力先のパス
	private static final String PATH =  Environment.getExternalStorageDirectory().getPath() + "/modelcache/";
	// 保持するキャッシュの数
	private int CACHE_MAX = 2;
	// ファイルから読み込むマップ
	private HashMap<String,KGLModelData> cacheTable;
	// キャッシュされるたびに引かれる有効期限
	private static final int reCastNum = 1;


	/**
	 * コンストラクタ
	 * 
	 * @param con
	 */
	public CacheHelperforDatabasae(Context con){
		startup(con);
		cacheTable = new HashMap<String, KGLModelData>(5);
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
	 * ファイルからキャッシュデータを読み込む．
	 * 
	 */
	private void inportCache(){
		String[] names = getCachingDataNameAll();
		for(String name : names){
			cacheTable.put(name, inputModel(name));
		}
	}

	/**
	 * キャッシュのデータをファイルに書き出す．
	 */
	private void exportCache(){
		String[] names = getCachingDataNameAll();
		for(String name : names){
			OutputModel(name, cacheTable.get(name));
		}
	}

	/**
	 * 
	 * オブジェクトをファイルに書き出す．
	 * 
	 * @param name
	 * @param modelData
	 * @return
	 */
	private boolean OutputModel(String name, KGLModelData modelData){
		try{
			File file = new File(PATH + name + ".obj");
			// FileOutputStreamオブジェクトの生成
			FileOutputStream outFile = new FileOutputStream(file);
			// ObjectOutputStreamオブジェクトの生成
			ObjectOutputStream outObject = new ObjectOutputStream(outFile);
			// クラスHelloのオブジェクトの書き込み
			outObject.writeObject(modelData);

			// ストリームのクローズ
			outObject.close(); 
			outFile.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * オブジェクトをファイルから読み込む
	 * @param name
	 * @return
	 */
	private KGLModelData inputModel(String name){
		FileInputStream inFile = null;
		ObjectInputStream inObject = null;
		KGLModelData model = null;
		File file = new File(PATH + name + ".obj");
		try {
			inFile = new FileInputStream(file);
			// ObjectInputStreamオブジェクトの生成
			inObject = new ObjectInputStream(inFile);
			// オブジェクトの読み込み
			model = (KGLModelData)inObject.readObject();

			// ストリームのクローズ
			inObject.close();  
			inFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	/**
	 * キャッシュをセットする．
	 * 
	 * @param name
	 * @param model
	 */
	public void setCacheData(String name, KGLModelData model){
		String category = "unknown";
		int limit = 0;
		reCastLimit(reCastNum);
		updateLimitCategory(category, limit/2);

		if(cacheTable.size() >= CACHE_MAX){
			Log.d(TAG,"Remove Low Priority Cache");
			removeLowLimitCache();
		}

		insertorUpdate(name, category, limit);
		cacheTable.put(name, model);
	}
	
	/**
	 * 
	 * キャッシュをセットする．
	 * 
	 * @param name
	 * @param category
	 * @param model
	 */
	public void setCacheData(String name, String category, KGLModelData model){
		int limit = 0;
		reCastLimit(reCastNum);
		updateLimitCategory(category, limit/2);

		if(cacheTable.size() >= CACHE_MAX){
			Log.d(TAG,"Remove Low Priority Cache");
			removeLowLimitCache();
		}

		insertorUpdate(name, category, limit);
		cacheTable.put(name, model);
	}
	
	/**
	 * 
	 * キャッシュをセットする．
	 * 
	 * @param name
	 * @param category
	 * @param limit
	 * @param model
	 */
	public void setCacheData(String name, String category, int limit, KGLModelData model){
		reCastLimit(reCastNum);
		updateLimitCategory(category, limit/2);

		if(cacheTable.size() >= CACHE_MAX){
			Log.d(TAG,"Remove Low Priority Cache");
			removeLowLimitCache();
		}

		insertorUpdate(name, category, limit);
		cacheTable.put(name, model);
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
	 * モデルを返す．
	 * 
	 * @param name
	 * @return
	 */
	public KGLModelData getCacheData(String name){
		update(CacheDatabase.COLUMN_NAME, name, CacheDatabase.COLUMN_LIMIT, 100);
		return cacheTable.get(name);
	}

	/**
	 * 一番優先度の低いものを削除する
	 */
	public void removeLowLimitCache(){
		String item = getLimitLowerItemNameforFirst();
		cacheTable.remove(item);
		dead(item);
	}

	/**
	 * キャッシュ全体を削除
	 * 
	 * @author s0921122
	 * 
	 */
	public void clearCacheTable(){
		cacheTable.clear();
	}

	/**
	 * ファイルの書き出しとデータベースのクローズ．
	 * 
	 */
	@Override
	public void close() {
		super.close();
		exportCache();
	}


}
