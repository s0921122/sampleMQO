package org.takanolab.kGLModel;

import java.io.Serializable;
import java.nio.ByteBuffer;

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
}