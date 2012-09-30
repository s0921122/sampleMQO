/**
 * モデルを持つクラス
 * 
 */
package jp.androidgroup.nyartoolkit;

import jp.nyatla.kGLModel.KGLModelData;

public class ModelField {
	KGLModelData model = null;
	String modelName = "";
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
}
