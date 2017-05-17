/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com
 * create by lc  2015年6月30日 上午9:24:53 
 */

package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class CrushWriter {
	private static final String LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/mhealth365/osdk/log/";

	public CrushWriter() {
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

	UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			writeErrorLog(ex);
			try {
				App.finishSdk();// 释放sdk所有资源【不可恢复】
			} catch (IOException e) {
				e.printStackTrace();
			}
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	};

	/**
	 * 打印错误日志
	 * 
	 * @param ex
	 */
	protected void writeErrorLog(Throwable ex) {
		String info = null;
		ByteArrayOutputStream baos = null;
		PrintStream printStream = null;
		try {
			baos = new ByteArrayOutputStream();
			printStream = new PrintStream(baos);
			ex.printStackTrace(printStream);
			byte[] data = baos.toByteArray();
			info = new String(data);
			data = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (printStream != null) {
					printStream.close();
				}
				if (baos != null) {
					baos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.d("example", "崩溃信息\n" + info);
		File dir = new File(LOG_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String pacakgeName = App.getApp().getPackageName();
		String LOG_NAME = getCurrentDateString() + "(" + pacakgeName + ")" + ".txt";
		File file = new File(dir, LOG_NAME);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file, true);
			fileOutputStream.write(pacakgeName.getBytes());
			fileOutputStream.write(info.getBytes());
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前日期
	 * 
	 * @return
	 */
	private static String getCurrentDateString() {
		String result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
		Date nowDate = new Date();
		result = sdf.format(nowDate);

		return result;
	}
}
