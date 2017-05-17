/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com
 * create by lc  2015年8月31日 上午11:24:07 
 */

package com.vodone.o2o.hulianwangyy_guizhou.demander.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

/**
 * 演示数据文件
 * 仅作演示
 */
public class EcgFile {

	public static EcgDataSource read(File file) throws Exception {
		if (file.exists() && file.canRead()) {
		} else {
			return null;
		}
		FileReader fis = new FileReader(file);

		return read(fis);
	}

	public static EcgDataSource read(InputStream is) throws Exception {
		InputStreamReader isr = new InputStreamReader(is);
		return read(isr);
	}

	public static EcgDataSource read(InputStreamReader isr) throws Exception {

		BufferedReader br = new BufferedReader(isr);
		String startTimeString = br.readLine();
		long startTime = 0;
		startTime = Long.parseLong(startTimeString);
		String sampleString = br.readLine();
		int sample = 0;
		sample = Integer.parseInt(sampleString);

		EcgDataSource data = new EcgDataSource(startTime, sample);
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
		if (packageList.isEmpty())
			return null;
		// Log.i("EcgFile", "len:"+packageList.size());

		data.fillPackage(packageList);

		Log.e("EcgFile", "---read---" + data.toString());
		return data;
	}

	public static boolean write(File file, EcgDataSource data) throws IOException {
		if (file == null || data == null)
			return false;
		if (file.exists())
			return false;

		if (!file.createNewFile())
			return false;

		FileWriter fw = new FileWriter(file);
		String dataStartTime = data.startTime + "";
		writeLine(fw, dataStartTime);
		String dataSample = data.sample + "";
		writeLine(fw, dataSample);

		ArrayList<int[]> packageList = data.getEcgData();
		Log.w("EcgFile", "---write---" + data.toString());
		for (int[] is : packageList) {
			int ecg = is[0];
			String ecgString = ecg + "";
			writeLine(fw, ecgString);
		}
		fw.close();
		return true;
	}

	private static void writeLine(FileWriter fw, String line) throws IOException {
		fw.write(line);
		fw.write("\r\n");
	}
}