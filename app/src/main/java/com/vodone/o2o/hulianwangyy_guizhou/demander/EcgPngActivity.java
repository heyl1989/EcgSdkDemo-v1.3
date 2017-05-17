package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class EcgPngActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ecg_png);

		Button button = (Button) findViewById(R.id.make_png);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						Context context = getApplicationContext();
						InputStream is = null;
						try {
							is = context.getAssets().open("60hr500hz1mv.ecg");// 心率60，usb设备，振幅1.0mV.ecg
							int[] ecg = readEcg(is);

							int ecgSampleHz = 500;// 采样率：USB设备的采样率为500，蓝牙设备的采样率为200
							int seconds = 60;// 生成报告的时长，一张图片最多可生成1分钟的数据，超过1分钟的数据需要多次调用，生成多张图片
							int len = ecgSampleHz * seconds;
							int[] copy = new int[len];
							for (int i = 0; i < copy.length; i++) {
								copy[i] = ecg[i];
							}
							
							String root = "/mhealth365/demo/sdk/";
							String dir = Environment.getExternalStorageDirectory().getPath() + root;
							createFileDir(dir);

							Bitmap bitmap = EcgRender.test(copy, ecgSampleHz);
							String fileName = dir + "123456.png";
							fileName = tryANewFile(fileName);
							boolean ok = createPng(fileName, bitmap);
							Log.i("Main", "fileName:" + fileName + ",ok:" + ok);
							String msg = ok ? "生成图片成功！" : "生成图片失败！";
							handler.obtainMessage(0, msg).sendToTarget();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
		};
	};

	public static void createFileDir(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static String tryANewFile(String filename) {
		String get = filename;
		int count = 0;
		int index = filename.lastIndexOf('.');
		String name = filename.substring(0, index);
		String suffix = filename.substring(index);
		File f = new File(filename);
//		String path = f.getParent();
//		File dir = new File(path);
//		if (!dir.exists())
//			dir.mkdirs();
		if (f.isFile() && f.exists()) {
			count++;
			get = tryANewFile(name, count, suffix);
		}
		return get;
	}

	static String tryANewFile(String filename, int count, String filesuffix) {
		String get = filename + count + filesuffix;
		File f = new File(get);
		// String path = f.getParent();
		if (f.isFile() && f.exists()) {
			count++;
			get = tryANewFile(filename, count, filesuffix);
		}
		return get;
	}

	private boolean createPng(String fileName, Bitmap bitmap) {
		File file = new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (fos != null) {
				bitmap.compress(CompressFormat.PNG, 100, fos);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static int[] readEcg(InputStream is) throws IOException {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String startTimeString = br.readLine();
		long startTime = 0;
		startTime = Long.parseLong(startTimeString);
		String sampleString = br.readLine();
		int sample = 0;
		sample = Integer.parseInt(sampleString);

		ArrayList<int[]> packageList = new ArrayList<int[]>();
		String readLine = null;
		do {
			readLine = null;
			readLine = br.readLine();
			if (readLine != null) {
				// Log.i("EcgFile", readLine);
				int ecgValue = Integer.parseInt(readLine);
				packageList.add(new int[] { ecgValue });
			}

		} while (readLine != null);
		br.close();
		is.close();
		if (packageList.isEmpty())
			return null;

		int[] ecgData = new int[packageList.size()];
		for (int i = 0; i < ecgData.length; i++) {
			ecgData[i] = packageList.get(i)[0];
		}
		return ecgData;
	}
}