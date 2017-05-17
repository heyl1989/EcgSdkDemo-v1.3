/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com All rights reserved.
 * create by lc  2015年6月16日 上午9:56:01 
 */
package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.File;
import java.io.IOException;

import com.mhealth365.osdk.EcgOpenApiCallback.OsdkCallback;
import com.mhealth365.osdk.EcgOpenApiHelper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class App extends Application {
	private final static String defThirdPartyId = "9002e85acfc29e3687c849a88e46c40b";
	private final static String defAppId = "709f1da5e8e1eb9c591c11ffaaaa4bf6";
//	private final static String defThirdPartyId = "";//这里填写申请到的thirdpartyID，不填写的话会初始化失败
//	private final static String defAppId = "";//这里填写申请到的APPID，不填写的话会初始化失败
//	private final static String defAppSecret = "";

	public String thirdPartyId = "";
	public String appId = "";
	public String pkgName = "";
	public String appSecret = "";
	public String UserOrgName = "天泽互联网医院";
	// 20150923修改定义机构（厂商）名称，（自定义）长度不超过25字节,不为空
	// 20150831,增加保存数据和读取保存数据

	private static Context mAppContext;
	private static App app;
	private static int versionCode = 0;
	private static String versionName = "";
	private static String rootDir;
	public final static String newRoot = "/mhealth365/demo/sdk";
	public static String APPNAME = "";
	private CrushWriter crushWriter;
	private LogcatHelper mLogcatHelper;
	final static private boolean logcat2file = false;
	public static float dpi = 0;
	public SharedPreferences mSharedPreferences;
	public final static String KEY_THIRD_PARTY_ID = "KEY_THIRD_PARTY_ID";
	public final static String KEY_APP_ID = "KEY_APP_ID";
	public final static String KEY_APP_PKG_NAME = "KEY_APP_PKG_NAME";

	@Override
	public void onCreate() {
		super.onCreate();
		thirdPartyId = defThirdPartyId;
		appId = defAppId;
		mSharedPreferences = getSharedPreferences("mhealth365", Context.MODE_PRIVATE);
		readValue();
		crushWriter = new CrushWriter();
		app = this;
		mAppContext = getApplicationContext();

		WindowManager wm = (WindowManager) mAppContext.getSystemService(Context.WINDOW_SERVICE);
		Display dis = wm.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();

		dis.getMetrics(dm);
		dpi = dm.ydpi;

		hasSystemFeatureCheck();
		String pn = getPackageName();
		APPNAME = "sdk-demo";
		initDir();
		try {
			versionCode = mAppContext.getPackageManager().getPackageInfo(pn, 0).versionCode;
			versionName = mAppContext.getPackageManager().getPackageInfo(pn, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (logcat2file) {
			mLogcatHelper = LogcatHelper.getInstance(getEcgContext());
			mLogcatHelper.start();
		}

		EcgOpenApiHelper mHelper = EcgOpenApiHelper.getInstance();
		Log.i("App", "--- thirdPartyId:" + thirdPartyId);
		Log.i("App", "--- appId:" + appId);
		Log.i("App", "--- appSecret:" + appSecret);
		Log.i("App", "--- pkgName:" + pkgName);
		try {
			mHelper.initOsdk(mAppContext, thirdPartyId, appId, appSecret, UserOrgName, mOsdkCallback, pkgName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setValue(String thirdPartyId, String appId, String pkgName) {
		mSharedPreferences.edit().putString(KEY_THIRD_PARTY_ID, thirdPartyId).commit();
		mSharedPreferences.edit().putString(KEY_APP_ID, appId).commit();
		mSharedPreferences.edit().putString(KEY_APP_PKG_NAME, pkgName).commit();
		readValue();
	}

	private void readValue() {
		this.thirdPartyId = mSharedPreferences.getString(KEY_THIRD_PARTY_ID, thirdPartyId);
		this.appId = mSharedPreferences.getString(KEY_APP_ID, appId);
		this.pkgName = mSharedPreferences.getString(KEY_APP_PKG_NAME, getPackageName());
	}

	public void setDefaultValue() {
		setValue(defThirdPartyId, defAppId, getPackageName());
	}

	public static void finishSdk() throws IOException {
		EcgOpenApiHelper mHelper = EcgOpenApiHelper.getInstance();
		mHelper.finishSdk();
	}

	public static App getApp() {
		return app;
	}

	OsdkCallback displayMessage;

	public void setOsdkCallback(OsdkCallback osdkCallback) {
		displayMessage = osdkCallback;
	}

	OsdkCallback mOsdkCallback = new OsdkCallback() {

		@Override
		public void deviceSocketLost() {
			if (displayMessage != null)
				displayMessage.deviceSocketLost();
		}

		@Override
		public void deviceSocketConnect() {
			if (displayMessage != null)
				displayMessage.deviceSocketConnect();
		}

		@Override
		public void devicePlugOut() {
			if (displayMessage != null)
				displayMessage.devicePlugOut();
		}

		@Override
		public void devicePlugIn() {
			if (displayMessage != null)
				displayMessage.devicePlugIn();
		}

		@Override
		public void deviceReady(int sample) {
			if (displayMessage != null)
				displayMessage.deviceReady(sample);
		}

		@Override
		public void deviceNotReady(int msg) {
			if (displayMessage != null)
				displayMessage.deviceNotReady(msg);
		}
	};

	public void onTerminate() {
		if (mLogcatHelper != null) {
			mLogcatHelper.stop();
		}
		try {
			App.finishSdk();// 释放sdk所有资源【不可恢复】
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void hasSystemFeatureCheck() {
		StringBuffer sb = new StringBuffer();

		fileString("usb 从", hasSystemFeature_USB_ACCESSORY(), sb);
		fileString("usb 主", hasSystemFeature_USB_HOST(), sb);

		Log.i("App", sb.toString());
	}

	void fileString(String name, boolean yes, StringBuffer sb) {
		sb.append("\n");
		sb.append(name);
		sb.append("：");
		if (yes) {
			sb.append("YES");
		} else {
			sb.append("NO");
		}
		sb.append("\n");
	}

	private void initDir() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			File internalCacheDir = mAppContext.getCacheDir();
			rootDir = internalCacheDir.getAbsolutePath();
		}
		rootDir = rootDir + newRoot;
		createFileDir(rootDir);
	}

	public static String getRootDir() {
		return rootDir;
	}

	public static Context getEcgContext() {
		return mAppContext;
	}

	public static String getVersionName() {
		return versionName;
	}

	public static String getVersion() {
		return versionName + "(" + versionCode + ")";
	}

	public static String AppName() {
		return APPNAME;
	}

	public static void createFileDir(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static boolean hasSystemFeature_USB_HOST() {
		return mAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
	}

	public static boolean hasSystemFeature_USB_ACCESSORY() {
		return mAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY);
	}

	public static void killProcess() {
		Log.v("activity", "kill process------------1");
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}