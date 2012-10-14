package jp.androidgroup.nyartoolkit;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.IntBuffer;

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
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;

import org.takanolab.kGLModel.KGLException;
import org.takanolab.kGLModel.KGLModelData;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 
 * NyARToolkit4.0.3
 *
 */
public class NyARToolkitAndroidActivity extends AndSketch implements AndGLView.IGLFunctionEvent
{

	// Log認識用タグ
	private static String TAG = "NyARToolkitAndroid";
	// ActivityResulutの認識コード
	private static final int FIXATION_MODEL= 1;

	CameraPreview _camera_preview;
	AndGLView _glv;
	Camera.Size _cap_size;

	// マーカーの数
	private static final int PAT_MAX = 2;

	// 使用するモデルのパス
	private String modelPath = Environment.getExternalStorageDirectory().getPath() + "/3DModelData/";
	// ユーザが選択したモデル名を受け取る変数
	private String requestName = "";

	// Modelの制御
	float lastX = 0;
	float lastY = 0;
	float scale = 2f;
	float xpos=0,ypos=0,zpos=0,xrot=0,yrot=0,zrot=0;

	// modelの操作フラグ
	int mode = 0;
	// 画面サイズ
	int screen_w,screen_h;


	// 固定表示をするときに使う姿勢制御の変数
	float[] center = new float[]{
			0.99548185f,
			0.091270804f,
			0.026182842f,
			0.0f,
			-0.04013392f,
			0.65435773f,
			-0.7551194f,
			0.0f,
			-0.0860533f,
			0.7506568f,
			0.6550643f,
			0.0f,
			22.488108f,
			-72.82579f,
			-359.72952f,
			1.0f
	};

	// モデルの固定表示フラグ
	boolean displayflag = false;
	// スクリーンキャプチャフラグ
	boolean screenCapture = false;
	// Bitmapの存在フラグ
	boolean isGLBitmap = false;
	// GLの描写部分のBitmap
	Bitmap GLBitmap;
	// 固定表示を行うかのフラグ
	boolean fixationflag = false;

	// Log用カウント
	int count_Position = 0;
	int count_Rotate = 0;
	int count_Scale = 0;
	int count_ScreenCapture = 0;
	private int [] cgframe = new int[PAT_MAX];
	// Log用フラグ
	boolean sdflag = false;

	// for model renderer
	private static final int CROP_MSG = 1;
	private static final int FIRST_TIME_INIT = 2;
	private static final int RESTART_PREVIEW = 3;
	private static final int CLEAR_SCREEN_DELAY = 4;
	private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;
	public static final int SHOW_LOADING = 6;
	public static final int HIDE_LOADING = 7;

	// YUV420 convert RGB(naitive)
	static {
		System.loadLibrary("yuv420sp2rgb");
	}
	public static native void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height, int type);
	public static native void decodeYUV420SP(byte[] rgb, byte[] yuv420sp, int width, int heught, int type);

	/**
	 * onStartでは、Viewのセットアップをしてください。
	 */
	@Override
	public void onStart()
	{
		super.onStart();

		long start = System.currentTimeMillis();

		FrameLayout fr=((FrameLayout)this.findViewById(R.id.sketchLayout));
		// エラー回避用
		_evlistener.clear();
		// エラー回避用
		fr.removeAllViews();
		//カメラの取得
		Log.e("OnStart", "new CameraPreview");
		this._camera_preview=new CameraPreview(this);
		// カメラの解像度
		this._cap_size=this._camera_preview.getRecommendPreviewSize(320,240);
		//		this._cap_size=this._camera_preview.getRecommendPreviewSize(640,480);
		//		this._cap_size=this._camera_preview.getRecommendPreviewSize(1280,720);
		// 画面サイズの計算（画面に表示される大きさ）
		//		int h = this.getWindowManager().getDefaultDisplay().getHeight();
		//		screen_w=(this._cap_size.width*h/this._cap_size.height);
		//		screen_h=h;
		screen_w = this.getWindowManager().getDefaultDisplay().getWidth();
		screen_h = this.getWindowManager().getDefaultDisplay().getHeight();
		//camera
		fr.addView(this._camera_preview, 0, new LayoutParams(screen_w,screen_h));
		//GLview
		this._glv=new AndGLView(this);
		fr.addView(this._glv, 0,new LayoutParams(screen_w,screen_h));
		long end = System.currentTimeMillis();

		Log.d(TAG,"onStart Time " + (end - start) + "ms");

	}

	NyARAndSensor _ss;
	NyARAndMarkerSystem _ms;
	private int[] _mid = new int[PAT_MAX];
	// モデルの名前配列
	private String[] modelNames = new String[PAT_MAX];
	// モデルデータ
	private KGLModelData[] model_data = new KGLModelData[PAT_MAX];
	AndGLTextLabel text;
	AndGLBox box;
	AndGLFpsLabel fps;

	public void setupGL(GL10 gl)
	{
		TAG = "setupGL";
		long start = System.currentTimeMillis();
		try
		{
			Log.d(TAG,"GL Setup Start.");
			AssetManager assetMng = getResources().getAssets();
			//create sensor controller.
			this._ss=new NyARAndSensor(this._camera_preview,this._cap_size.width,this._cap_size.height,30);
			//create marker system
			this._ms=new NyARAndMarkerSystem(new NyARMarkerSystemConfig(this._cap_size.width,this._cap_size.height));

//			if(_mid[PAT_MAX-1] == 0) Log.d(TAG,"_mid is Null");

			this._mid[0]=this._ms.addARMarker(assetMng.open("AR/data/hiro.pat"),16,25,80);
			this._mid[1]=this._ms.addARMarker(assetMng.open("AR/data/kanji.pat"),16,25,80);

//			for(String str : modelNames){
//				if(str == null) Log.d(TAG,"modelNames is Null");
//			}

			// モデルの名前
			modelNames[0] = "Brilliant_Blue_Discus_Fish.mqo";
			modelNames[1] = "miku01.mqo";

//			for(KGLModelData data : model_data){
//				if(data == null) Log.d(TAG,"model_data is Null");
//			}
			
			if(model_data[PAT_MAX-1] == null){
				try {
					// アセットから読み込み
//					model_data[0] = KGLModelData.createGLModel(gl,null,assetMng, modelNames[0], 0.15f);
//					model_data[1] = KGLModelData.createGLModel(gl,null,assetMng, modelNames[1], 0.06f);
					// 外部ストレージから読み込み
					model_data[0] = KGLModelData.createGLModel(gl,null,modelPath, modelNames[0], 0.15f);
					model_data[1] = KGLModelData.createGLModel(gl,null,modelPath, modelNames[1], 0.06f);
				} catch (KGLException e) {
					e.printStackTrace();
					throw new NyARException(e);
				}
			}else{
				// そのままではテクスチャが貼られないのであらためて貼る
				model_data[0].reloadTexture(gl);
				model_data[1].reloadTexture(gl);
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

			long end = System.currentTimeMillis();
			Log.d(TAG,"GL Setup End.");
			Log.d(TAG,"setupGL Time " + (end - start) + "ms");

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
			//背景塗り潰し色の指定
			gl.glClearColor(0,0,0,0);
			//背景塗り潰し
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
			if(ex!=null){
				_debug.draw(ex);
				return;
			}
			fps.draw(0, 0);
			synchronized(this._ss){
				this._ms.update(this._ss);
				for(int id : _mid){
					if(this._ms.isExistMarker(id)) drawModelData(gl, id);
				}
			}
			if(displayflag){
				// フラグがた立っているときモデルを固定表示
				drawModelDataFixation(gl, modelNames[1]);
			}
			if(fixationflag){
				// フラグが立っているときモデルを固定表示
				drawModelDataFixation(gl, requestName);
			}
			if(screenCapture){
				// フラグが立ってるときGL描写部分を画像で保存
				glScreenCapture(gl);
			}
		}catch(Exception e)
		{
			ex=e;
		}
	}


	/**
	 * idに一致するモデルを描写します
	 * 
	 * @param gl
	 * @param id
	 * @throws NyARException
	 */
	private void drawModelData(GL10 gl,int id) throws NyARException{
		this.text.draw("found" + this._ms.getConfidence(id),0,16);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadMatrixf(this._ms.getGlMarkerMatrix(id),0);

		gl.glTranslatef(this.xpos, this.ypos, this.zpos);
		// OpenGL座標系→ARToolkit座標系
		gl.glRotatef(this.xrot, 1.0f,0.0f,0.0f);
		gl.glRotatef(this.yrot, 0.0f,1.0f,0.0f);
		gl.glRotatef(this.zrot, 0.0f,0.0f,1.0f);
		gl.glScalef(this.scale, this.scale, this.scale);
		model_data[id].enables(gl, 10.0f);
		model_data[id].draw(gl);
		model_data[id].disables(gl);
		if(sdflag){
			cgframe[id]++;
			SdLog.put(id + "フレーム = " + cgframe[id]);
		}
	}


	/**
	 * nameの一致するモデルを描写します<br>
	 * マーカーに左右されず画面上に表示されます．
	 * 
	 * @param gl
	 * @param name
	 * @throws NyARException
	 */
	private void drawModelDataFixation(GL10 gl,String name) throws NyARException{
		TAG = "drawModelDataFixation";
		int id = -1;
		// 名前を検索
		for(int i=0;i<modelNames.length;i++){
			if(modelNames[i].startsWith(name)){
				// 一致する添え字をセット
				id = i;
				break;
			}
		}
		if(id<0) return;

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadMatrixf(center,0);

		// 固定表示のための姿勢位置を取得するためのLog
		//		gl.glLoadMatrixf(this._ms.getGlMarkerMatrix(id),0);
		//		String str = "";
		//		float[] mat = this._ms.getGlMarkerMatrix(id);
		//		for(float fl : mat){
		//			str += fl + "\n";
		//		}
		//		Log.d(TAG,"Matrix\n" + str + "end.");

		gl.glTranslatef(this.xpos, this.ypos, this.zpos);
		// OpenGL座標系→ARToolkit座標系
		gl.glRotatef(this.xrot, 1.0f,0.0f,0.0f);
		gl.glRotatef(this.yrot, 0.0f,1.0f,0.0f);
		gl.glRotatef(this.zrot, 0.0f,0.0f,1.0f);
		gl.glScalef(this.scale, this.scale, this.scale);
		model_data[id].enables(gl, 10.0f);
		model_data[id].draw(gl);
		model_data[id].disables(gl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// メニューアイテムの追加
		menu.add(Menu.NONE, 0, Menu.NONE, "Position");
		menu.add(Menu.NONE, 1, Menu.NONE, "Rotate");
		menu.add(Menu.NONE, 2, Menu.NONE, "Scale");
		menu.add(Menu.NONE, 3, Menu.NONE, "DisplayModel");
		menu.add(Menu.NONE, 4, Menu.NONE, "ScreenCapture");
		menu.add(Menu.NONE, 5, Menu.NONE, "SlectFixationModel");
		menu.add(Menu.NONE, 6, Menu.NONE, "Exit");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(displayflag){
			menu.findItem(3).setTitle("HideClear");
		}else{
			menu.findItem(3).setTitle("ShowModel");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// addしたときのIDで識別
		switch (item.getItemId()) {
		case 0:
			mode = 0;
			count_Position++;
			if(sdflag) SdLog.put("count_Position = " + count_Position);
			return true;

		case 1:
			mode = 1;
			count_Rotate++;
			if(sdflag) SdLog.put("count_Rotate = " + count_Rotate);
			return true;

		case 2:
			mode = 2;
			count_Scale++;
			if(sdflag) SdLog.put("count_Scale = " + count_Scale);
			return true;
		case 3:
			if(!displayflag){
				displayflag = true;
			}else{
				displayflag = false;
			}
			return true;
		case 4:
			Shot();
			count_ScreenCapture++;
			if(sdflag) SdLog.put("ScreenCapture = " + count_ScreenCapture);
			return true;
		case 5:
			if(fixationflag){
				fixationflag = false;
				selectFixationModel();
			}else{
				selectFixationModel();
			}
			return true;
		case 6:
			finish();
			break;
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		TAG = "onActivityResult";
		// インテントのリザルトを受け取る
		if(requestCode == FIXATION_MODEL){
			if(resultCode == RESULT_OK){
				// 固定表示フラグを立てる
				fixationflag = true;
				// 表示するモデル名をセット
				requestName = data.getStringExtra("selectitem");
				Log.d(TAG,"getItemName " + data.getStringExtra("selectitem"));
			}
		}
	}

	/**
	 *  固定表示させるモデルを選択する（アクティビティー切り替え）
	 *  
	 */
	private void selectFixationModel(){
		// インテント作成
		Intent it = new Intent();
		// モデル名配列をセット
		it.putExtra("FixationModel", modelNames);
		// 遷移先をセット
		it.setClassName("jp.androidgroup.nyartoolkit","jp.androidgroup.nyartoolkit.QuestActivity");
		// リザルト付きアクティビティースタート
		startActivityForResult(it, FIXATION_MODEL);
	}


	/**
	 * スクリーンキャプチャを撮る．<br>
	 * takePictureでうまくいかなかったので、プレビューを加工してBitmpで保存する．<br>
	 * CGModelは写らないので{@link #glScreenCapture(GL10)}を使う．<br>
	 * 参考 : http://android.roof-balcony.com/camera/preview-get/
	 * 
	 * @author s0921122
	 * @version 1.0
	 */
	private void Shot(){
		TAG = "Shot";
		Log.d(TAG,"Screen Capture Start");
		int width = _cap_size.width;
		int height = _cap_size.height;
		Bitmap cameraBitmap = null;

		try{
			Log.d(TAG,"Empty BMP(ARG8888) Create.");
			// ARGB8888の画素の配列
			int[] rgb = new int[(width * height)]; 
			// ARGB8888で空のビットマップ作成
			cameraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 

			Log.d(TAG,"Decode Start.");
			// YUV420からRGBに変換
			decodeYUV420SP(rgb, _camera_preview.getCurrentBuffer(), width, height, 2);
			Log.d(TAG,"Decode done.");
			Log.d(TAG,"Camera Preview Capture Create Start.");
			// 変換した画素からビットマップにセット
			cameraBitmap.setPixels(rgb, 0, width, 0, 0, width, height); 
			Log.d(TAG,"Camera Preview Capture Create done.");

			// GL描写部分のスクリーンキャプチャを撮るフラグを立てる
			screenCapture = true;

			// GL描写部分のスクリーンキャプチャができるまで待機
			while(true){
				if(isGLBitmap) break;
			}
			// 画像を重ね合わせて保存する
			overlayBMP(cameraBitmap, GLBitmap);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * GLで描写している部分をキャプチャする<br>
	 * 参考 : http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html
	 * 
	 * @author s0921122
	 * @version 1.0
	 * @param gl
	 */
	private void glScreenCapture(GL10 gl){
		TAG = "glScreenCapture";
		Log.d(TAG,"GL Drawing Screen Capture Start.");
		// キャプチャフラグを下げる
		screenCapture = false;
		// スクリーンキャプチャする範囲
		int takeWidth = screen_w;
		int takeHeight = screen_h;
		int[] tmp = new int[takeHeight*takeWidth];
		int[] screenshot = new int[takeHeight*takeWidth];
		Buffer screenshotBuffer = IntBuffer.wrap(tmp);
		screenshotBuffer.position(0);
		gl.glReadPixels(0,0,takeWidth,takeHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, screenshotBuffer); 

		for(int i=0; i<takeHeight; i++) 
		{
			//remember, that OpenGL bitmap is incompatible with Android bitmap 
			//and so, some correction need.      
			for(int j=0; j<takeWidth; j++) 
			{ 
				int pix=tmp[i*takeWidth+j]; 
				int pb=(pix>>16)&0xff; 
				int pr=(pix<<16)&0x00ff0000; 
				int pix1=(pix&0xff00ff00) | pr | pb; 
				screenshot[(takeHeight-i-1)*takeWidth+j]=pix1; 
			} 
		} 
		// アルファありのBitmapを作成する
		// この時にリサイズで保存はできない（変な画像になる）
		Bitmap temp = Bitmap.createBitmap(screenshot, takeWidth, takeHeight, Config.ARGB_8888);
		//　作成したBitmapを元にカメラプレビューサイズにリサイズしたBitmapを作成
		this.GLBitmap = Bitmap.createScaledBitmap(temp, _cap_size.width, _cap_size.height, true);
		temp.recycle();

		Log.d(TAG,"GL Drawing Screen Capture. Create done.");
		// GLをキャプチャしたBitmapを作成したのでフラグを立てる
		isGLBitmap = true;
	}


	/**
	 * 2枚のBitmap重ね合わせて保存する．<br>
	 * このプログラムではカメラとGLのBitmapを重ね合わせて保存する．
	 * 
	 * @param under 背景になる画像
	 * @param upper 上に乗る画像
	 */
	private void overlayBMP(Bitmap under,Bitmap upper){
		TAG = "overlayBMP";
		Log.d(TAG,"Bitmap Ovelray Start.");
		//ARGB_8888,RGB_565,ARGB_4444
		int width = under.getWidth();
		int height = under.getHeight();

		Log.d(TAG,"Create Base Bitmap.");
		// 合成するための下地
		Bitmap newBitmap = Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565);

		Log.d(TAG,"Create Canvas.");
		Canvas canvas = new Canvas(newBitmap);
		Log.d(TAG,"Overlay Start.");
		canvas.drawBitmap(under, 0, 0, (Paint)null);
		canvas.drawBitmap(upper, 0, 0, (Paint)null);
		Log.d(TAG,"Overlay done.");
		Log.d(TAG,"Recycle Start.");
		upper.recycle();
		under.recycle();
		GLBitmap.recycle();
		isGLBitmap = false;
		Log.d(TAG,"Recycle done.");

		FileOutputStream fos;
		try{
			Log.d(TAG,"FileOutput Start.");
			String path = new StringBuilder()
			.append(Environment.getExternalStorageDirectory().getPath()).append("/")
			//.append("Pictures/Screenshots/")
			.append("SampleMQO").append(".jpg")
			.toString();

			fos = new FileOutputStream(path);
			// JPEGで保存
			newBitmap.compress(CompressFormat.JPEG, 100, fos);
			Toast.makeText(this, "ScreenCapture Success.", Toast.LENGTH_LONG).show();
			Log.d(TAG,"FileOutput end. \nOutput Path : " + path);
			Log.d(TAG,"ScreenCapture end.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	Exception ex=null;
}
