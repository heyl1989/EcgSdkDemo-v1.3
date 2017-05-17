package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

/**
 * log日志统计保存
 * 
 * @author way
 * 
 */
public class LogcatHelper {
	
	final static private String readlog = "android.permission.READ_LOGS";
	private static LogcatHelper INSTANCE = null;
	private static String PATH_LOGCAT;
	private LogDumper mLogDumper = null;
	private int mPId;
	private boolean hasPermission = false;

	/**
	 * 初始化目录
	 */
	public void init(Context context) {
		hasPermission = checkPermissions(context);
		if (!hasPermission) {
			Log.i("LogcatHelper", "没有log权限");
			return;
		}
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
//			PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SdkDemoLog";
			PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mhealth365/osdk/log/";
		} else {// 如果SD卡不存在，就保存到本应用的目录下
			PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + "SdkDemoLog";
		}
		File file = new File(PATH_LOGCAT);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static LogcatHelper getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new LogcatHelper(context);
		}
		return INSTANCE;
	}

	private LogcatHelper(Context context) {
		init(context);
		mPId = android.os.Process.myPid();
		Log.i("LogcatHelper", "mPId:" + mPId);
	}

	public void start() {
		if (!hasPermission)
			return;
		if (mLogDumper == null)
			mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
		mLogDumper.start();
	}

	public void stop() {
		if (!hasPermission)
			return;
		if (mLogDumper != null) {
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	private class LogDumper extends Thread {

		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = true;
		String cmds = null;
		private String mPID;
		private FileOutputStream out = null;

		public LogDumper(String pid, String dir) {
			mPID = pid;
			try {
				out = new FileOutputStream(new File(dir, "SdkDemo-" + MyDate.getFileName() + ".log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**
			 * 
			 * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
			 * 
			 * 显示当前mPID程序的 E和W等级的日志.
			 * 
			 * */

			// cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
			cmds = "logcat  | grep \"(" + mPID + ")\"";// 打印所有日志信息
			// cmds = "logcat -s way";//打印标签过滤信息
			// cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
		}

		public void stopLogs() {
			mRunning = false;
		}

		@Override
		public void run() {
			try {
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if (out != null && line.contains(mPID)) {
						out.write((MyDate.getDateEN() + "  " + line + "\n").getBytes());
						// Log.i("LogcatHelper", "line:"+line);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (logcatProc != null) {
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null) {
					try {
						mReader.close();
						mReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out = null;
				}
			}
		}
	}

	private boolean checkPermissions(Context appContext) {
		String pn = appContext.getPackageName();
		PackageManager packageManager = null;
		PackageInfo packageInfo = null;
		packageManager = appContext.getPackageManager();
		try {
			packageInfo = packageManager.getPackageInfo(pn, PackageManager.GET_PERMISSIONS);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
			packageInfo = null;
		}
		String permissions[] = packageInfo.requestedPermissions;
		// int[] permissionsFlags = packageInfo.requestedPermissionsFlags;
		if (permissions == null)
			return false;
		ArrayList<String> list = new ArrayList<String>();
		for (String str : permissions) {
			list.add(str);
			Log.i("LogcatHelper", "permission：" + str);
		}
		if (!list.contains(readlog)) {
			return false;
		}
		return true;
	}
}

class MyDate {
	public static String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;// 2012年10月03日 23:41:31
	}

	public static String getDateEN() {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date1 = format1.format(new Date(System.currentTimeMillis()));
		return date1;// 2012-10-03 23:41:31
	}
}
