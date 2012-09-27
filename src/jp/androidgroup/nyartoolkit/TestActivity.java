package jp.androidgroup.nyartoolkit;

import android.R.id;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TestActivity extends Activity{

	// モデルを３つくらい、リスト表示する
	// リスト上でモデルをクリックしたら、NyARTOolkitAndroidActivityに移行し、
	// クリックしたモデルの3DCGを表示する
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.test_layout);

	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
	        // 表示する3DCGを追加します
	        adapter.add("kiageha");
	        adapter.add("miku01");
	        adapter.add("kumataka");
//	        ListView listView = (ListView) findViewById(id.listview);
	        // アダプターを設定します
//	        listView.setAdapter(adapter);
	 }

}
