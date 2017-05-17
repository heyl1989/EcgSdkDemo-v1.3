/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com All rights reserved.
 * create by lc  2015年6月16日 上午9:56:01 
 */
package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.File;
import java.io.IOException;

import com.mhealth365.osdk.ecgbrowser.EcgBrowserInteractive;
import com.mhealth365.osdk.ecgbrowser.RealTimeEcgBrowser;
import com.mhealth365.osdk.ecgbrowser.Scale;
import com.mhealth365.osdk.EcgOpenApiCallback;
import com.mhealth365.osdk.EcgOpenApiCallback.OsdkCallback;
import com.mhealth365.osdk.EcgOpenApiCallback.RECORD_FAIL_MSG;
import com.mhealth365.osdk.EcgOpenApiCallback.RecordCallback;
import com.mhealth365.osdk.EcgOpenApiHelper;
import com.mhealth365.osdk.EcgOpenApiHelper.RECORD_MODE;
import com.vodone.o2o.hulianwangyy_guizhou.demander.file.EcgDataSource;
import com.vodone.o2o.hulianwangyy_guizhou.demander.file.EcgFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * usb 设备特别的处理： 启动项：在AndroidManifest.xml 里添加参数 res 中添加匹配usb的参数文件
 * activity里处理收到插入设备消息的调用，在onResume中
 */
public class EcgSdkUSBDemo extends Activity implements OnClickListener {

	public static final String TAG = EcgSdkUSBDemo.class.getSimpleName();
	private EcgDataSource demoData = null;
	private int ecgSample = 0;
	private int countEcg = 0;
	private EcgOpenApiHelper mOsdkHelper = null;
	private String versionString = EcgOpenApiHelper.ver + "(" + EcgOpenApiHelper.doc + ")";
	private TextView hr, rr, speed, gain, result, counter, ver, tvDeviceStatus;
	private Button mBtnRegister, mBtnLogin, mBtnBluetooth;
	private Button mButtonRecordStart;
	private Button mButtonRecordStop;
	private RealTimeEcgBrowser mEcgBrowser;
	private AlertDialog mRecordTimeDialog;
	private AlertDialog mScreenDialog;
	public final String[] items = { "30秒", "60秒", "10分钟", "30分钟", "60分钟" };
//	private RealTimeEcgBrowser mEcgBrowserSmall;

	void initSdk() {
		mOsdkHelper = EcgOpenApiHelper.getInstance();
		App.getApp().setOsdkCallback(mOsdkCallback);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ecg_sdk_demo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.smallBrowser) {
			initEcgSmall();
		} else if (id == R.id.datasource) {
			startActivity(new Intent(this, EcgDataSourceReviewActivity.class).putExtra("ecgFile", showDataFile));
		} else if (id == R.id.action_settings_screen) {
			mScreenDialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ecg_demo);
		mEcgBrowser = (RealTimeEcgBrowser) findViewById(R.id.ecgBrowser);
		mEcgBrowser.setEcgBrowserInteractive(mEcgBrowserInteractive);
//		mEcgBrowserSmall = (RealTimeEcgBrowser) findViewById(R.id.ecgBrowserSmall);

		mButtonRecordStart = (Button) findViewById(R.id.button_record_start);
		mButtonRecordStop = (Button) findViewById(R.id.button_record_stop);
		mBtnRegister = (Button) findViewById(R.id.button_register);
		mBtnLogin = (Button) findViewById(R.id.button_login);
		mBtnLogin.setOnClickListener(this);
		mBtnRegister.setOnClickListener(this);

		mButtonRecordStart.setOnClickListener(this);
		mButtonRecordStop.setOnClickListener(this);

		mBtnBluetooth = (Button) findViewById(R.id.button_bt);
		mBtnBluetooth.setOnClickListener(this);

		if (!App.hasSystemFeature_USB_HOST()) {
			Toast.makeText(this, "系统不支持usb host，无法连接设备", Toast.LENGTH_LONG).show();
		}
		
		mRecordTimeDialog = new AlertDialog.Builder(this)
			.setTitle("选择测量时间")
			.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					RECORD_MODE mode = RECORD_MODE.RECORD_MODE_3600;
					switch (which) {
					case 0:
						mode = RECORD_MODE.RECORD_MODE_30;
						break;
					case 1:
						mode = RECORD_MODE.RECORD_MODE_60;
						break;
					case 2:
						mode = RECORD_MODE.RECORD_MODE_600;
						break;
					case 3:
						mode = RECORD_MODE.RECORD_MODE_1800;
						break;
					case 4:
						mode = RECORD_MODE.RECORD_MODE_3600;
						break;
					}
					startRecord(mode);
					mRecordTimeDialog.dismiss();
				}
			}).create();
		
		final EditText et = new EditText(this);
		et.setHint("请输入屏幕尺寸");
		et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
		mScreenDialog = new AlertDialog.Builder(this).setTitle("设置屏幕尺寸")
				.setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String input = et.getText().toString();
						if (!TextUtils.isEmpty(input)) {
							float screenInch = 5;
							try {
								screenInch = Float.parseFloat(input.trim());
							} catch (NumberFormatException e) {
								e.printStackTrace();
								ToastText("输入格式错误");
							}
							DisplayMetrics dm = getResources().getDisplayMetrics();
							mEcgBrowser.setScreenDPI(screenInch, dm.widthPixels, dm.heightPixels);
							App.dpi = getDisplayDPI(screenInch, dm.widthPixels, dm.heightPixels);
							mEcgBrowser.clearEcg();
						}
					}
				}).setNegativeButton("取消", null).create();
		
		initLable();
		initSdk();
		initEcg();
//		initEcgSmall();
	}

	void initLable() {
		hr = (TextView) findViewById(R.id.label_heartrate_realtime);
		rr = (TextView) findViewById(R.id.label_rr_value);
		speed = (TextView) findViewById(R.id.label_speed);
		gain = (TextView) findViewById(R.id.label_gain);
		result = (TextView) findViewById(R.id.label_result);
		counter = (TextView) findViewById(R.id.label_counter);
		ver = (TextView) findViewById(R.id.label_ver_value);
		tvDeviceStatus = (TextView) findViewById(R.id.label_device_value);
		clearValue();
	}

	void clearValue() {
		hr.setText("---");
		rr.setText("---");
		speed.setText("");
		gain.setText("");
		ver.setText(versionString);
		counter.setText("---");
		mEcgBrowser.clearEcg();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.v(getClass().getSimpleName(), "onRestart");
	}

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(getClass().getSimpleName(), "onResume");
		init();
	}

	private void init() {
		mOsdkHelper.notifyUSBDeviceAttach();
		if (mOsdkHelper.isDeviceReady()) {
			setEcgSample(mOsdkHelper.getEcgSample());
		}
	}

	public void setEcgSample(int sample) {
		this.ecgSample = sample;
		mEcgBrowser.setSample(sample);
	}

	public void initEcg() {
		mEcgBrowser.setSpeedAndGain(Scale.SPEED_25MM_S, Scale.GAIN_10MM_MV);// 设置增益和走速
		mEcgBrowser.setSample(500);
		mEcgBrowser.showFps(true);
		mEcgBrowser.setScreenDPI(getDPI());
		mEcgBrowser.clearEcg();
	}

	private boolean isSmall = false;

	public void initEcgSmall() {
		if (!isSmall) {
			mEcgBrowser.setBackgroundColor(Color.LTGRAY);
			mEcgBrowser.setEcgColor(Color.WHITE);
			mEcgBrowser.setGridVisible(false);
			mEcgBrowser.setStandRectVisible(false);
			mEcgBrowser.setSpeedGainVisible(false);
			mEcgBrowser.setOpenTouch(false);
			mEcgBrowser.setScreenDPI(300);
			mEcgBrowser.clearEcg();
		} else {
			mEcgBrowser.setBackgroundColor(Color.WHITE);
			mEcgBrowser.setEcgColor(Color.BLACK);
			mEcgBrowser.setGridVisible(true);
			mEcgBrowser.setStandRectVisible(true);
			mEcgBrowser.setSpeedGainVisible(true);
			mEcgBrowser.setOpenTouch(true);
			DisplayMetrics dm = getResources().getDisplayMetrics();
			mEcgBrowser.setScreenDPI(dm.xdpi);
			mEcgBrowser.clearEcg();
		}
		isSmall = !isSmall;
	}

	OsdkCallback mOsdkCallback = new OsdkCallback() {

		@Override
		public void deviceSocketLost() {
			ToastText("usb设备连接断开！");
			tvDeviceStatus.setText("已断开");
		}

		@Override
		public void deviceSocketConnect() {
			ToastText("usb设备已连接！");
			tvDeviceStatus.setText("已连接");
		}

		@Override
		public void devicePlugOut() {
			ToastText("usb设备拔出！");
			tvDeviceStatus.setText("设备拔出");
		}

		@Override
		public void devicePlugIn() {
			ToastText("usb设备插入！");
			tvDeviceStatus.setText("设备插入");
		}

		@Override
		public void deviceReady(int sample) {
			if (sample <= 0) {
				EcgSdkUSBDemo.this.ecgSample = 0;
				ToastText("采集器参数异常：sample=" + ecgSample);
			} else {
				ToastText("心电设备已准备好！");// 可以开始记录心电图
				tvDeviceStatus.setText("准备就绪,设备号:" + mOsdkHelper.getDeviceSN());
				setEcgSample(sample);
			}
		}

		@Override
		public void deviceNotReady(int msg) {
			switch (msg) {
			case EcgOpenApiCallback.DEVICE_NOT_READY_NOT_SUPPORT_DEVICE:// sdk不支持设备
				ToastText("当前sdk设备无法使用此型号设备");// sdk不支持型号
				tvDeviceStatus.setText("型号不支持");
				break;
			case EcgOpenApiCallback.DEVICE_NOT_READY_UNKNOWN_DEVICE:// 未知设备
				ToastText("设备无法使用");// 设备故障或者非熙健产品
				tvDeviceStatus.setText("无法使用");
				break;
			default:
				break;
			}
		}
	};

	public float getDPI() {
		return mEcgBrowser.getDisplayDPI();
	}

	EcgBrowserInteractive mEcgBrowserInteractive = new EcgBrowserInteractive() {

		@Override
		public void onChangeGainAndSpeed(int gain, int speed) {
			displayMessage.obtainMessage(ECG_GAIN_SPEED, gain, speed).sendToTarget();
		}
	};

	RecordCallback mRecordCallback = new RecordCallback() {

		@Override
		public void recordTime(int second) {
			Log.w(getClass().getSimpleName(), "recordTime--- second=" + second);
			displayMessage.obtainMessage(ECG_COUNTER, second, -1).sendToTarget();
		}

//		@Override  //不用了
//		public void recordStatistics(String id, int averageHeartRate, int normalRange, int suspectedRisk) {
//
//		}
		

		@Override
		public void recordStatistics(String id, int averageHeartRate,
				int[] heartRectPercentages, int[] rhythmRectPercentages,
				int rhythmType) {
			if (null != id) {
				// FIXME 节律异常范围，修改为节律正常范围
				String msg = "平均心率：" + averageHeartRate + "(bpm),心率数组值：(" + heartRectPercentages[0] + ","+ heartRectPercentages[1] + ","+ heartRectPercentages[2] + ")" 
				+ ",节律数组：(" + rhythmRectPercentages[0] + ","+ rhythmRectPercentages[1] + ","+ rhythmRectPercentages[2] + ")" 
				+ ",风险提示："+rhythmType;
				displayMessage.obtainMessage(ECG_STAISTICS_RESULT, msg).sendToTarget();
				
				String toastMSG = "平均心率：" + averageHeartRate + "(bpm),心率正常范围：" + heartRectPercentages[0]
				+"%,稍快稍慢："+heartRectPercentages[1]+"%,过快过慢："+heartRectPercentages[2] + "%" 
				+ ",节律正常范围：" + rhythmRectPercentages[0] + "%"
				+"%,疑似心率不齐或早搏："+rhythmRectPercentages[1]+"%,疑似心房颤动或早搏："+rhythmRectPercentages[2] + "%";
				String warnString = "";
				if(rhythmType == 2){
					warnString = "心脏节律异常风险-中"+" --- "+"如您多次在静止、无干扰的状态下测量，异常节律仍高于10%，为了您的健康，请您咨询医师或专业人员。异常节律可以是心律不齐或者早搏、干扰引起，请您咨询医师或专业人员。请您定期或随时监测，跟踪心脏健康风险。";
				} else if(rhythmType == 3){
					warnString = "心脏节律异常风险-高"+" --- "+"如您多次在静止、无干扰的状态下测量，异常节律仍高于20%，提示您的心脏可能存在心律失常风险，建议您尽快咨询医师或专业人员。请您定期和随时监测，跟踪心脏健康风险。";
				}else{//rhythmType = 1
					warnString = "心脏节律异常风险-低" +" --- "+"您的心脏节律异常风险低。请您继续保持良好的生活习惯：清淡饮食、适量运动、保证睡眠、戒烟限酒。少量的异常节律可以是心律不齐或者早搏、干扰引起，请您咨询医师或专业人员。定期和随时监测，有助您提早发现心脏风险。";
				}
				ToastText("统计分析完成:"+toastMSG+"\n"+warnString);
			} else {
				String msg = "【统计数据异常】";// 一般是数据文件错误引起
				displayMessage.obtainMessage(ECG_STAISTICS_RESULT, msg).sendToTarget();
			}
		}

		@Override
		public void recordStart(String id) {
			Log.w(getClass().getSimpleName(), "recordStart--- id=" + id);
			Log.w(getClass().getSimpleName(), "recordStart--- countEcg=" + countEcg);
			try {
				demoData = new EcgDataSource(System.currentTimeMillis(), ecgSample);
			} catch (Exception e) {
				e.printStackTrace();
				ToastText("创建记录失败，ecgSample：" + ecgSample);
			}
		}

		@Override
		public void recordEnd(String id) {
			if (id == null) {
				ToastText("关闭记录，未生成有效数据");
			} else {
				ToastText("记录结束，开始统计分析");
			}
			Log.w(getClass().getSimpleName(), "recordEnd--- id=" + id);
			Log.w(getClass().getSimpleName(), "recordEnd--- countEcg=" + countEcg);
			if (demoData != null) {
				String rootDir = getFileRoot();
				File file = new File(rootDir);
				if (!file.exists()) {
					file.mkdirs();
				}
				String filename = rootDir + System.currentTimeMillis() + ".ecg";
				File demoFile = new File(filename);
				if (demoFile.exists()) {
					demoFile.delete();
				}
				try {
					Log.w(getClass().getSimpleName(), "recordEnd--- demoData:" + demoData.toString());
					boolean ok = EcgFile.write(demoFile, demoData);
					if (ok) {
						showData(filename);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				demoData = null;
			}
		}

		@Override
		public void heartRate(int hr) {
			Log.w(getClass().getSimpleName(), "heartRate--- hr=" + hr);
			displayMessage.obtainMessage(ECG_HEART, hr, -1).sendToTarget();
		}

		@Override
		public void ecg(int[] value) {
			countEcg++;
			mEcgBrowser.ecgPackage(value);
			if (demoData != null)
				demoData.addPackage(value);
		}

		@Override
		public void RR(int ms) {
			Log.v(getClass().getSimpleName(), "RR--- rr=" + ms);
			displayMessage.obtainMessage(ECG_RR, ms, -1).sendToTarget();
		}

		@Override
		public void startFailed(RECORD_FAIL_MSG msg) {
			Log.e(getClass().getSimpleName(), "startFailed--- " + msg.name());
			String text = "";
			switch (msg) {
			case RECORD_FAIL_A_RECORD_RUNNING:
				text = "已经开始记录了";
				break;
			case RECORD_FAIL_DEVICE_NO_RESPOND:
				text = "设备没有响应";// 设备没有响应控制指令，可以重试
				break;
			case RECORD_FAIL_DEVICE_NOT_READY:
				text = "设备没有准备好";// 设备未插入，或者未被识别
				break;
			case RECORD_FAIL_NOT_LOGIN:
				text = "还没有登陆";
				break;
			case RECORD_FAIL_OSDK_INIT_ERROR:
				text = "osdk没有初始化";
				break;
			case RECORD_FAIL_PARAMETER:
				text = "参数错误";
				break;
			default:
				break;
			}
			ToastText("开始记录失败：" + text);
		}
		
		@Override
		public void battery(int value) {
		}

		@Override
		public void addAccelerate(short arg0, short arg1, short arg2) {
		}

		@Override
		public void addAccelerateVector(float arg0) {
		}
		
		@Override
		public void leadOff(boolean isOff) {
			Log.i("导联提示", isOff ? "导联脱落" : "导联正常");
		}
	};

	String showDataFile = null;

	private void showData(String demoFile) {
		displayMessage.obtainMessage(ECG_SHOW_DATA, demoFile).sendToTarget();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(EcgSdkUSBDemo.this).setIconAttribute(android.R.attr.alertDialogIcon).setTitle("查看记录")
				.setPositiveButton("打开", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (showDataFile != null) {
							Intent intent = new Intent(EcgSdkUSBDemo.this, EcgDataSourceReviewActivity.class);
							intent.putExtra("ecgFile", showDataFile);
							startActivity(intent);
						} else {
							ToastText("当前没有记录！");
						}
					}
				}).setNegativeButton("放弃", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create();
	}

	/**
	 * 开始记录
	 */
	public void startRecord(RECORD_MODE mode) {
		try {
			result.setText("");
			countEcg = 0;
			mOsdkHelper.startRecord(mode, mRecordCallback);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			ToastText("【开始记录】文件异常,开始记录失败！");
		} catch (Exception e) {
			e.printStackTrace();
			ToastText("【开始记录】文件异常,开始记录失败！");
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.button_record_start:
			if (mOsdkHelper.isRunningRecord()) {
				ToastText("正在记录中，请等待记录完成后再操作");
				return;
			}
			mRecordTimeDialog.show();
			break;
		case R.id.button_record_stop:
			if (mOsdkHelper.isRunningRecord()) {
				try {
					ToastText("【停止记录】");
					mOsdkHelper.stopRecord();
					mEcgBrowser.clearEcg();
				} catch (IOException e) {
					e.printStackTrace();
					ToastText("【关闭记录】文件异常,开始记录失败！");
				}
			} else {
				ToastText("没有开始记录！");
			}
			break;
		case R.id.button_register:
			if (mOsdkHelper.isRunningRecord()) {
				ToastText("正在记录中，请等待记录完成后再操作");
				return;
			}
			startActivity(new Intent(this, RegisterActivity.class));
			break;
		case R.id.button_login:
			if (mOsdkHelper.isRunningRecord()) {
				ToastText("正在记录中，请等待记录完成后再操作");
				return;
			}
			startActivity(new Intent(this, LoginActivity.class));
			break;
		case R.id.button_bt:
			if (mOsdkHelper.isRunningRecord()) {
				ToastText("正在记录中，请等待记录完成后再操作");
				return;
			}
			startActivity(new Intent(this, EcgSdkBluetoothDemo.class));
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("SDK", "---  onDestroy ");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mOsdkHelper.isRunningRecord()) {
				ToastText("还有记录正在运行，请先停止记录！");
				return false;
			}
			finish();
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	final static int ECG_GAIN_SPEED = 10001;
	final static int TOAST_TEXT = 10002;
	static final int ECG_HEART = 10003;
	static final int ECG_RR = 10004;
	static final int ECG_STAISTICS_RESULT = 10005;
	static final int ECG_COUNTER = 10006;
	static final int ECG_SHOW_DATA = 10007;
	
	private Handler displayMessage = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case ECG_COUNTER:
				counter.setText(msg.arg1 + "");
				break;
			case ECG_STAISTICS_RESULT:
				String text = (String) msg.obj;
				if (text != null)
					result.setText(text);
				break;
			case ECG_HEART:
				int hrValue = msg.arg1;
				if (hrValue >= 1 && hrValue <= 355) {
					hr.setText("" + hrValue);
				} else {
					hr.setText("---");
				}
				break;
			case ECG_RR:
				if (msg.arg1 >= 10000) {
					rr.setText("---");
				} else {
					rr.setText("" + msg.arg1);
				}
				break;
			case ECG_GAIN_SPEED:
				float speedValue = msg.arg2 / 10.0f;
				float gainValue = msg.arg1 / 10.0f;
				speed.setText("" + speedValue + " mm/s");
				gain.setText("" + gainValue + " mm/mv");
				break;
			case TOAST_TEXT:
				String txt = (String) msg.obj;
				if (txt != null)
					Toast.makeText(getBaseContext(), txt, Toast.LENGTH_SHORT).show();
				break;
			case ECG_SHOW_DATA:
				showDataFile = (String) msg.obj;
				if (showDataFile != null) {
					showDialog(0);
				}
				clearValue();
				break;
			default:
				break;
			}
		}
	};

	public void ToastText(String text) {
		displayMessage.obtainMessage(TOAST_TEXT, text).sendToTarget();
	}

	private String getFileRoot() {
		String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EcgSdkDemo/";
		return rootDir;
	}

	public float getDisplayDPI(float inch, int width, int height) {
		/** 对角线像素数 */
		float len = (float) Math.sqrt(width * width + height * height);
		/** 通过对角线直接计算的密度 */
		float DPI = len / inch;
		return DPI;
	}
}