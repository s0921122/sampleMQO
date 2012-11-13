package org.takanolab.kGLModel;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * マテリアルの描画情報
 * @author kkoni
 *
 */
public class GLMaterial implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * マテリアル名
	 */
	String name ;
	/**
	 * 描画有無<br>
	 */
	boolean isVisible = true;
	/**
	 * 色情報
	 */
	float[]	color = null ;
	/**
	 * 拡散光
	 */
	float[]	dif = null ;
	/**
	 * 環境光
	 */
	float[]	amb = null ;
	/**
	 * 放射輝度
	 */
	float[]	emi = null ;
	/**
	 * 鏡面反射
	 */
	float[]	spc = null ;
	/**
	 * 鏡面反射強度
	 */
	float[] power = null;

	/**
	 * シェーディングモード<br>
	 * GL_SMOOTH or GL_FLAT
	 */
	boolean shadeMode_IsSmooth = true ; //OpenGLのデフォルトはGL_SMOOTH

	/**
	 * 頂点数
	 */
	int vertex_num ;
	/**
	 * テクスチャＩＤ（未使用の場合０）<br>
	 */
	int	texID = 0 ;
	// reload 用
	String texName = null;
	String alphaTexName = null;

	// interleaveFormat は無いので
	// ShortBuffer indexBuffer;
	transient public ByteBuffer vertexBuffer;
	transient public ByteBuffer normalBuffer;
	transient public ByteBuffer uvBuffer = null;
	transient public ByteBuffer colBuffer = null;

	public byte[] _vertexBuffer;
	public byte[] _normalBuffer;
	public byte[] _uvBuffer;
	public byte[] _colBuffer;

	boolean uvValid = false;
	boolean colValid = false;

	// int indexCount;

}
