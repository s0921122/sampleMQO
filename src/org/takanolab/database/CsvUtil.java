package org.takanolab.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class CsvUtil {

	public static final String READ_MODE = "READ";
	public static final String WRITE_MODE = "WRITE";
	
	PrintWriter pw;
	BufferedReader br;
	File file;
	private String mode;
	private String path = Environment.getExternalStorageDirectory().getPath() + "/3DModelData/database.txt";

	
	public CsvUtil(String code){
		mode = code;
		checkFile();
		try{
			if(code.equals(READ_MODE)){
				br = new BufferedReader(new FileReader(file));			
			}
			if(code.equals(WRITE_MODE)){
				pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));			
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkFile(){
		file = new File(path);
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void add(String str){
		if(mode.equals(WRITE_MODE))
			pw.println(str);
	}
	
	public void close(){
		try {
			if(mode.equals(READ_MODE)) br.close();
			if(mode.equals(WRITE_MODE))pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> importCSV(){
		if(mode.equals(READ_MODE)){
			String temp;
			ArrayList<String> line = new ArrayList<String>();
			try{
				// 最後の行まで読み込む
				while((temp = br.readLine()) != null){
					line.add(temp);
					Log.d("CSV",temp);
				}
				return line;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
