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

import org.takanolab.kGLModel.GLMaterial;
import org.takanolab.kGLModel.GLObject;

import android.os.Environment;
import android.util.Log;

public class ChangeStream {
	
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
	 * ファイルを読みこみオブジェクトを生成
	 * 
	 * @version 1.0
	 * @param name FileName
	 * @return
	 */
	static public GLObject[] read_object(String name){
		GLObject[] obj = null;
		try {
			FileInputStream inFile = new FileInputStream(PATH + name);
			ObjectInputStream in = new ObjectInputStream(inFile);
			obj = (GLObject[]) in.readObject();
			in.close();
			inFile.close();
		} catch(Exception e) {
			Log.d(TAG,"FileInput");
			e.printStackTrace();
		}
		if(obj != null){
			for(GLObject glob : obj){
				glob.matBufferChange();
			}
		}
		return obj;
	}

	/**
	 * オブジェクトをファイルに書き出す
	 * 
	 * @version 1.0
	 * @param obj GLObject
	 * @param name FileName
	 * @return
	 */
	static public boolean write_object(GLObject[] obj,String name){
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
	 * ByteBufferをbyte[]に変換する
	 * 
	 * @param bb
	 * @return
	 */
	private byte[] buffertobyte(ByteBuffer bb){
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
	private ByteBuffer bytetoByteBuffer(byte[] bt){
		return ByteBuffer.wrap(bt);
	}
	
	public void matBufferChange(GLMaterial[] mat){
		for(int i=0; i<=mat.length; i++){
			mat[i].vertexBuffer = bytetoByteBuffer(mat[i]._vertexBuffer);
			mat[i].normalBuffer = bytetoByteBuffer(mat[i]._normalBuffer);
			mat[i].uvBuffer = bytetoByteBuffer(mat[i]._uvBuffer);
			mat[i].colBuffer = bytetoByteBuffer(mat[i]._colBuffer);
		}
	}
	
	public void matByteChange(GLMaterial[] mat){
		for(int i=0; i<=mat.length; i++){
			mat[i]._vertexBuffer = buffertobyte(mat[i].vertexBuffer);
			mat[i]._normalBuffer = buffertobyte(mat[i].normalBuffer);
			mat[i]._uvBuffer = buffertobyte(mat[i].uvBuffer);
			mat[i]._colBuffer = buffertobyte(mat[i].colBuffer);
		}
	}

}
