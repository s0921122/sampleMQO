package com.paar.ch9;

import java.util.List;

import org.json.JSONObject;

import jp.androidgroup.nyartoolkit.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TestDataSource extends NetworkDataSource {
	
	private static Bitmap icon = null;

	public TestDataSource(Resources res){
		if(res == null) throw new NullPointerException();
		
		icon = BitmapFactory.decodeResource(res, R.drawable.kait);
	}
	
	@Override
	public List<Marker> getMarkers() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String createRequestURL(double lat, double lon, double alt,
			float radius, String locale) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public List<Marker> parse(JSONObject root) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
