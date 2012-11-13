/**
 * キャッシュの読み書きをする
 * 
 * @author s0921122
 * @version 3.0
 */

package org.takanolab.cache.irc;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.takanolab.kGLModel.GLObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class CacheHelperforDatabasae extends CacheDatabaseUtils{

	// ログ出力用
	private static final String TAG = "CacheHelperForDatabase";
	// 保持するキャッシュの数
	private int CACHE_MAX = 2;
	// キャッシュされるたびに引かれる有効期限
	private  int reCastNum = 1;
	// ファイルから読み込むマップ
	private HashMap<String,GLObject> cacheTable;
	// Stream操作
	private StreamUtil streamUtil;
	// ログ出力
	private static final boolean LOGFLAG = true;


	/**
	 * コンストラクタ
	 * 
	 * @param con
	 */
	public CacheHelperforDatabasae(Context con){
		startup(con);
		cacheTable = new HashMap<String, GLObject>(CACHE_MAX + 1);
		streamUtil = new StreamUtil();
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
			cacheTable.put(name, streamUtil.read_object(name));
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
			streamUtil.write_object(cacheTable.get(name), name);
		}
	}

	/**
	 * キャッシュのセット及びデータベースの更新をする．
	 * 
	 * @param name
	 * @param is
	 */
	public void setCacheData(String name, InputStream is){
		String category = "unknown";
		int limit = 0;
		setCacheData(name, category, limit, is);
	}
	
	/**
	 * キャッシュのセット及びデータベースの更新をする．
	 * 
	 * @param name
	 * @param category
	 * @param is
	 */
	public void setCacheData(String name, String category, InputStream is){
		int limit = 0;
		setCacheData(name, category, limit, is);
	}
	
	/**
	 * キャッシュのセット及びデータベースの更新をする．
	 * 
	 * @param name
	 * @param category
	 * @param limit
	 * @param is
	 */
	public void setCacheData(String name, String category, int limit, InputStream is){
		if(LOGFLAG) Log.d(TAG,"Cache Set " + name);
		reCastLimit(reCastNum);
		updateLimitCategory(category, limit/2);

		if(cacheTable.size() >= CACHE_MAX){
			Log.d(TAG,"Remove Low Priority Cache");
			removeLowLimitCache();
		}

		insertorUpdate(name, category, limit);
		cacheTable.put(name, is);
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
	 * @return is
	 */
	public InputStream getCacheData(String name){
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
	 * 
	 */
	private void clearCacheTable(){
		if(LOGFLAG) Log.d(TAG,"Table Clear");
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
		clearCacheTable();
	}

}

class StreamUtil{
	
	// タグ出力用
	private static final String TAG = "StreamUtil";
	// キャッシュファイルの入出力先のパス
	private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/modelcache/";
	// ログ出力
	private static final boolean LOGFLAG = true;
	
	/**
	 * InputStreamをファイルに書き出す
	 * 
	 * @param name
	 * @param is
	 */
	public void fileOutputforInputStream(String name, InputStream is){
		if(LOGFLAG) Log.d(TAG,"File Output " + name);
		bytetoFile(name, converttoBytes(is));
	}
	
	/**
	 * ファイルからInputStreamを読み込み
	 * 
	 * @param name
	 * @return
	 */
	public InputStream fileInputforInputStream(String name){
		if(LOGFLAG) Log.d(TAG,"File Input " + name);
		return converttoStream(filetoByte(name));
	}
	
	/**
     * InputStreamをバイト配列に変換する
     *
     * @param is
     * @return バイト配列
     */
    private byte[] converttoBytes(InputStream is) {
    	long start = System.currentTimeMillis();
    	if(LOGFLAG) Log.d(TAG,"Start : "+ start);
    	
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = new BufferedOutputStream(baos);
        
        int c;
        try {
            while ((c = is.read()) != -1) {
                os.write(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        long end = System.currentTimeMillis();
        if(LOGFLAG) Log.d(TAG,"End : "+ end);
        if(LOGFLAG) Log.d(TAG,"InputStream → byte : " + (end - start));
        
        return baos.toByteArray();
    }
    
    /**
     * Byte配列からInputStreamに変換する
     * 
     * @param bytes
     * @return InputStream
     */
    private InputStream converttoStream(byte[] bytes){
    	long start = System.currentTimeMillis();
    	if(LOGFLAG) Log.d(TAG,"Start : " + start);
    	
    	InputStream bais = new ByteArrayInputStream(bytes);
    	
    	long end = System.currentTimeMillis();
    	if(LOGFLAG) Log.d(TAG,"End : " + end);
    	if(LOGFLAG) Log.d(TAG,"Byte → InputStream : " + (end - start));
    	
    	return bais;
    }
    
    /**
     * バイト配列をファイルに書き出す
     * 
     * @param name
     * @param data
     */
	private void bytetoFile(String name, byte[] data){
		FileOutputStream fos;
		File file;
		try {
			file = new File(PATH + name);
			checkFile(file);
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ファイルからバイト配列で読み込む
	 * 
	 * @param name
	 * @return
	 */
	private byte[] filetoByte(String name){
		File file = new File(PATH + name);
		FileInputStream fis = null;
		try {
			 fis = new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return converttoBytes(fis);
	}
	
	/**
	 * ファイルチェック
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void checkFile(File file) throws IOException{
		if(!file.exists()){
			new File(file.getParent()).mkdirs();
			file.createNewFile();
		}
	}
	
	/**
	 * ByteBufferをbyte[]に変換する
	 * 
	 * @param bb
	 * @return
	 */
	public byte[] buffertobyte(ByteBuffer bb){
		ArrayList<Byte> byteList = new ArrayList<Byte>();
		
		for(int i=0; i<=bb.limit(); i++){
			byteList.add(bb.get(i));
		}
		
		byte[] array = new byte[byteList.size()];
		for(int j=0; j<=byteList.size(); j++){
			array[j] = byteList.get(j).byteValue();
		}
		
		return array;
	}
	
	/**
	 * byte[]からByteBufferにラップする
	 * 
	 * @param bt
	 * @return
	 */
	public ByteBuffer bytetoByteBuffer(byte[] bt){
		return ByteBuffer.wrap(bt);
	}

	/**
	 * オブジェクトをファイルに書き出す
	 * 
	 * @version 1.0
	 * @param obj Object
	 * @param file FilePath
	 * @return
	 */
	public boolean write_object(GLObject obj,String name){
		for(int i=0; i<=obj.mat.length; i++){
			obj.mat[i]._vertexBuffer = buffertobyte(obj.mat[i].vertexBuffer);
			obj.mat[i]._normalBuffer = buffertobyte(obj.mat[i].normalBuffer);
			obj.mat[i]._uvBuffer = buffertobyte(obj.mat[i].uvBuffer);
			obj.mat[i]._colBuffer = buffertobyte(obj.mat[i].colBuffer);
		}
		
		try {
			FileOutputStream outFile = new FileOutputStream(PATH + name);
			ObjectOutputStream out = new ObjectOutputStream(outFile);
			out.writeObject(obj);
			out.close();
			outFile.close();
		} catch(Exception e) {
			Log.d(TAG,"FileOutput");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * ファイルを読みこみオブジェクトを生成
	 * 
	 * @version 1.0
	 * @param file FilePath
	 * @return
	 */
	public GLObject read_object(String name){
		GLObject obj = new GLObject();
		try {
			FileInputStream inFile = new FileInputStream(PATH + name);
			ObjectInputStream in = new ObjectInputStream(inFile);
			obj = (GLObject) in.readObject();
			in.close();
			inFile.close();
		} catch(Exception e) {
			Log.d(TAG,"FileInput");
			e.printStackTrace();
		}
		
		for(int i=0; i<=obj.mat.length; i++){
			obj.mat[i].vertexBuffer = bytetoByteBuffer(obj.mat[i]._vertexBuffer);
			obj.mat[i].normalBuffer = bytetoByteBuffer(obj.mat[i]._normalBuffer);
			obj.mat[i].uvBuffer = bytetoByteBuffer(obj.mat[i]._uvBuffer);
			obj.mat[i].colBuffer = bytetoByteBuffer(obj.mat[i]._colBuffer);
		}
		
		return obj;
	}

}
