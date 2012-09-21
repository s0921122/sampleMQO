package jp.androidgroup.nyartoolkit;


import javax.microedition.khronos.opengles.GL10;

import jp.androidgroup.nyartoolkit.markersystem.NyARAndMarkerSystem;
import jp.androidgroup.nyartoolkit.markersystem.NyARAndSensor;
import jp.androidgroup.nyartoolkit.sketch.AndSketch;
import jp.androidgroup.nyartoolkit.utils.camera.CameraPreview;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLBox;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLDebugDump;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLFpsLabel;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLTextLabel;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLView;
import jp.nyatla.kGLModel.KGLException;
import jp.nyatla.kGLModel.KGLModelData;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Hiroマーカの上にカラーキューブを表示します。
 * 定番のサンプルです。
 *
 */
public class NyARToolkitAndroidActivity extends AndSketch implements AndGLView.IGLFunctionEvent
{
	CameraPreview _camera_preview;
	AndGLView _glv;
	Camera.Size _cap_size;
	private KGLModelData[] model_data = new KGLModelData[2];
	
	float lastX = 0;
	float lastY = 0;
	float scale = 2f;
	float xpos=0,ypos=0,zpos=0,xrot=0,yrot=0,zrot=0;
	GL10 gl;
	int mode = 0;
	
	// for model renderer
	private static final int CROP_MSG = 1;
	private static final int FIRST_TIME_INIT = 2;
	private static final int RESTART_PREVIEW = 3;
	private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;
    public static final int SHOW_LOADING = 6;
    public static final int HIDE_LOADING = 7;
	
	/**
	 * onStartでは、Viewのセットアップをしてください。
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		FrameLayout fr=((FrameLayout)this.findViewById(R.id.sketchLayout));
		//カメラの取得
		this._camera_preview=new CameraPreview(this);
		this._cap_size=this._camera_preview.getRecommendPreviewSize(320,240);
		//画面サイズの計算
		int h = this.getWindowManager().getDefaultDisplay().getHeight();
		int screen_w,screen_h;
		screen_w=(this._cap_size.width*h/this._cap_size.height);
		screen_h=h;
		//camera
		fr.addView(this._camera_preview, 0, new LayoutParams(screen_w,screen_h));
		//GLview
		this._glv=new AndGLView(this);
		fr.addView(this._glv, 0,new LayoutParams(screen_w,screen_h));
	}

	NyARAndSensor _ss;
	NyARAndMarkerSystem _ms;
	private int[] _mid = new int[2];
	AndGLTextLabel text;
	AndGLBox box;
	AndGLFpsLabel fps;
	
	public void setupGL(GL10 gl)
	{
		try
		{
			AssetManager assetMng = getResources().getAssets();
			//create sensor controller.
			this._ss=new NyARAndSensor(this._camera_preview,this._cap_size.width,this._cap_size.height,30);
			//create marker system
			this._ms=new NyARAndMarkerSystem(new NyARMarkerSystemConfig(this._cap_size.width,this._cap_size.height));
			this._mid[0]=this._ms.addARMarker(assetMng.open("AR/data/hiro.pat"),16,25,80);
			this._mid[1]=this._ms.addARMarker(assetMng.open("AR/data/kanji.pat"),16,25,80);
			
			try {
				//LocalContentProvider content_provider=new LocalContentProvider("Kiageha.mqo");
				//model_data = KGLModelData.createGLModel(gl,null,content_provider,0.015f, KGLExtensionCheck.IsExtensionSupported(gl,"GL_ARB_vertex_buffer_object"));
				model_data[0] = KGLModelData.createGLModel(gl,null,assetMng,"Kiageha.mqo", 0.15f);
				model_data[1] = KGLModelData.createGLModel(gl,null,assetMng,"miku01.mqo", 0.06f);
			} catch (KGLException e) {
				e.printStackTrace();
				throw new NyARException(e);
			}
			
			this._ss.start();
			//setup openGL Camera Frustum
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadMatrixf(this._ms.getGlProjectionMatrix(),0);
			this.text=new AndGLTextLabel(this._glv);
			this.box=new AndGLBox(this._glv,40);
			this._debug=new AndGLDebugDump(this._glv);
			this.fps=new AndGLFpsLabel(this._glv,"MarkerPlaneActivity");
			this.fps.prefix=this._cap_size.width+"x"+this._cap_size.height+":";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.finish();
		}
	}
	AndGLDebugDump _debug=null;
	/**
	 * 継承したクラスで表示したいものを実装してください
	 * @param gl
	 */
	public void drawGL(GL10 gl)
	{
		try{
			this.gl = gl;
			//背景塗り潰し色の指定
			gl.glClearColor(0,0,0,0);
	        //背景塗り潰し
	        gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
	        if(ex!=null){
	        	_debug.draw(ex);
	        	return;
	        }
	        fps.draw(0, 0);
	        Log.d("draw","sychro");
			synchronized(this._ss){
				this._ms.update(this._ss);
				if(this._ms.isExistMarker(this._mid[0])){
			        this.text.draw("found"+this._ms.getConfidence(this._mid[0]),0,16);
					gl.glMatrixMode(GL10.GL_MODELVIEW);
					gl.glLoadMatrixf(this._ms.getGlMarkerMatrix(this._mid[0]),0);
					//this.box.draw(0,0,20);
					Log.d("Draw",xpos + "/" +  ypos);
					gl.glTranslatef(this.xpos, this.ypos, this.zpos);
// 					// OpenGL座標系→ARToolkit座標系
 					gl.glRotatef(this.xrot, 1.0f,0.0f,0.0f);
 					gl.glRotatef(this.yrot, 0.0f,1.0f,0.0f);
 					gl.glRotatef(this.zrot, 0.0f,0.0f,1.0f);
					gl.glScalef(this.scale, this.scale, this.scale);
					model_data[0].enables(gl, 10.0f);
					model_data[0].draw(gl);
					model_data[0].disables(gl);
				}else{
					if(this._ms.isExistMarker(this._mid[1])){
				        this.text.draw("found"+this._ms.getConfidence(this._mid[1]),0,16);
						gl.glMatrixMode(GL10.GL_MODELVIEW);
						gl.glLoadMatrixf(this._ms.getGlMarkerMatrix(this._mid[1]),0);
						//this.box.draw(0,0,20);
						Log.d("Draw",xpos +"" +  ypos);
						gl.glTranslatef(this.xpos, this.ypos, this.zpos);
	 					// OpenGL座標系→ARToolkit座標系
	 					gl.glRotatef(this.xrot, 1.0f,0.0f,0.0f);
	 					gl.glRotatef(this.yrot, 0.0f,1.0f,0.0f);
	 					gl.glRotatef(this.zrot, 0.0f,0.0f,1.0f);
						gl.glScalef(this.scale, this.scale, this.scale);
						model_data[1].enables(gl, 10.0f);
						model_data[1].draw(gl);
						model_data[1].disables(gl);
					}
				}
		}
		}catch(Exception e)
		{
			ex=e;
		}
	}
  
    public boolean onCreateOptionsMenu(Menu menu){
    	 
        // メニューアイテムの追加
        menu.add(Menu.NONE, 0, Menu.NONE, "Position");
        menu.add(Menu.NONE, 1, Menu.NONE, "Rotate");
        menu.add(Menu.NONE, 2, Menu.NONE, "Scale");
 
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	 
        // addしたときのIDで識別
        switch (item.getItemId()) {
        case 0:
            mode = 0;;
            return true;
     
        case 1:
            mode = 1;
            return true;
     
        case 2:
            mode = 2;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

    	Log.d("OnTouch","ontouch");
    	switch (event.getAction()) {
    	case MotionEvent.ACTION_DOWN:
    		lastX = event.getX();
    		lastY = event.getY();
    		break;

    	case MotionEvent.ACTION_MOVE:
    		float dX = lastX - event.getX();
    		float dY = lastY - event.getY();
    		lastX = event.getX();
    		lastY = event.getY();

    		switch(mode){
    		case 0 :
    			setXpos(-dX/1.0f);
    			setYpos(dY/1.0f);
    			Log.d("ontatuc",xpos +"/"+  ypos);
    			break;
    		case 1 :
    			setXrot(0.80f*-dY);
    			setYrot(0.80f*-dX);
    			Log.d("rotate",xrot +"/"+yrot);
    			return true;
    		case 2 :
    			setScale(dY/10.0f);
    			Log.d("scale",scale + "");
    			return true;
    		}

    	case MotionEvent.ACTION_UP:
    		break;
    	}
    	return true;
    }
	
	public void setScale(float f) {
		this.scale += f;
		if(this.scale < 0.0001f)
			this.scale = 0.0001f;
	}

	public void setXrot(float dY) {
		this.xrot += dY;
	}

	public void setYrot(float dX) {
		this.yrot += dX;
	}

	public void setXpos(float f) {
		this.xpos += f;
	}

	public void setYpos(float f) {
		this.ypos += f;
	}

	Exception ex=null;
}
