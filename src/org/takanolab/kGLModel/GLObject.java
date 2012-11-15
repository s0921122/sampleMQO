package org.takanolab.kGLModel;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * モデルの各オブジェクト情報保持クラス
 * @author kei
 *
 */
public class GLObject implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * オブジェクト名<br>
	 */
	String name = null ;
	/**
	 * 描画有無<br>
	 */
	boolean isVisible = true;
	/**
	 * マテリアル毎の描画情報<br>
	 */
	public GLMaterial[] mat = null ;
	/**
	 * ＯｐｅｎＧＬへ登録した頂点配列バッファＩＤ<br>
	 * （頂点配列バッファを使用する場合にしか値は入らない）<br>
	 */
	int[] VBO_ids = null ;
	
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
	
	public void matBufferChange(){
		for(int i=0; i<=mat.length; i++){
			mat[i].vertexBuffer = bytetoByteBuffer(mat[i]._vertexBuffer);
			mat[i].normalBuffer = bytetoByteBuffer(mat[i]._normalBuffer);
			mat[i].uvBuffer = bytetoByteBuffer(mat[i]._uvBuffer);
			mat[i].colBuffer = bytetoByteBuffer(mat[i]._colBuffer);
		}
	}
	
	public void matByteChange(){
		for(int i=0; i<=mat.length; i++){
			mat[i]._vertexBuffer = buffertobyte(mat[i].vertexBuffer);
			mat[i]._normalBuffer = buffertobyte(mat[i].normalBuffer);
			mat[i]._uvBuffer = buffertobyte(mat[i].uvBuffer);
			mat[i]._colBuffer = buffertobyte(mat[i].colBuffer);
		}
	}
}