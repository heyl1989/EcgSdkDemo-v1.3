package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.mhealth365.osdk.EcgOpenApiCallback;
import com.mhealth365.osdk.EcgOpenApiHelper;
import com.mhealth365.osdk.EcgOpenApiHelper.DEVICE;
import com.mhealth365.osdk.EcgOpenApiCallback.OsdkCallback;
import com.mhealth365.osdk.EcgOpenApiCallback.EcgConstant;
import com.mhealth365.osdk.EcgOpenApiCallback.RECORD_FAIL_MSG;
import com.mhealth365.osdk.EcgOpenApiCallback.RecordCallback;
import com.mhealth365.osdk.EcgOpenApiHelper.RECORD_MODE;
import com.mhealth365.osdk.ecgbrowser.EcgBrowserInteractive;
import com.mhealth365.osdk.ecgbrowser.RealTimeEcgBrowser;
import com.mhealth365.osdk.ecgbrowser.Scale;
import com.vodone.o2o.hulianwangyy_guizhou.demander.file.EcgDataSource;
import com.vodone.o2o.hulianwangyy_guizhou.demander.file.EcgFile;

@SuppressLint("NewApi")
public class EcgSdkBluetoothDemo extends Activity implements EcgConstant, OnClickListener {

    public static final String TAG = EcgSdkBluetoothDemo.class.getSimpleName();
    /**
     * 开始记录按钮
     */
    private Button mButtonRecordStart;
    /**
     * 结束记录按钮
     */
    private Button mButtonRecordStop;
    /**
     * mBtnRegister:注册按钮 mBtnLogin:登录按钮
     */
    private Button mBtnRegister, mBtnLogin;
    /**
     * mBtnconnect:连接按钮 mBtndisconnect:断开连接按钮
     */
    private Button mBtnconnect, mBtndisconnect;
    /**
     * mFreeze:冻结按钮 mUnFreeze:解冻按钮（这两个变量没有用到）
     */
    private Button mFreeze, mUnFreeze;
    /**
     * mSetBluetoothButton:蓝牙设置（蓝牙设备列表）
     */
    private Button mSetBluetoothButton;
    /**
     * 显示模块
     */
    private RealTimeEcgBrowser mEcgBrowser;
    /**
     * 设备控制
     */
    private EcgOpenApiHelper mOsdkHelper;
    /**
     * 即时心率
     */
    private TextView hr;
    /**
     * rr间期
     */
    private TextView rr;
    /**
     * 步数
     */
    private TextView step;
    /**
     * 运动类型，强度（MET）
     */
    private TextView acc;
    /**
     * 电量
     */
    private TextView battery;
    /**
     * 分析结果
     */
    private TextView result;
    /**
     * 记录时间(s)
     */
    private TextView counter;
    /**
     * 增速
     */
    private TextView speed;
    /**
     * 增益
     */
    private TextView gain;
    /**
     * 运动心率
     */
    private TextView motionHr;
    /**
     * 代谢当量（MET）
     */
    private TextView met;
    /**
     * 能量消耗（千卡）
     */
    private TextView ka;
    /**
     * 运动间期
     */
    private TextView motionRR;
    /**
     * CPU
     */
    private TextView cpu;
    private TextView debugMsg;
    /**
     * 设备状态
     */
    private TextView mDeviceStatusTV;
    private TextView tvMacAddress;
    /**
     * 房颤
     */
    private TextView atrialFibrillation;
    /**
     * 联律类型
     */
    private TextView tvRhythm;
    /**
     * ecgLib
     */
    private TextView tvLibname;
    /**
     * 三轴加速度
     */
    private TextView tvAcc;
    /**
     * r波幅度，r波宽度
     */
    private TextView tvRresult;
    private String macAddress;
    private BluetoothDevice device;
    private int countEcg = 0;
    private int ecgSample = 0;
    private EcgDataSource demoData = null;
    private String showDataFile;
    /**
     * 选择测量时间的dialog
     */
    private AlertDialog mRecordTimeDialog;
    /**
     * 设置屏幕尺寸dialog
     */
    private AlertDialog mScreenDialog;
    /**
     * 设置开始记录延迟dialog
     */
    private AlertDialog mDelayDialog;
    /**
     * 等待dialog
     */
    private ProgressDialog mWaitDialog;
    /**
     * 是否是USB模式，默认false
     */
    private boolean isUsbMode = false;
    /**
     * USB是否插入，默认false
     */
    private boolean isUsbPlugIn = false;
    /**
     * 记录流程
     */
    private TextView mRecordProcessTV;
    /**
     * mBatteryView:设备电量视图
     */
    private View mBatteryView;
    /**
     * mAccView:三轴加速度视图
     */
    private View mAccView;
    /**
     * 测量时间
     */
    public final String[] items = {"30秒", "60秒", "10分钟", "30分钟", "1小时", "手动模式"};

    /**
     * 硬件0.5hz滤波
     */
    public boolean is05Hz = false;
    /**
     * 工频滤波
     */
    public boolean is50Hz = false;
    public boolean is60Hz = false;
    /**
     * 肌电滤波
     */
    public boolean is25Hz = false;
    public boolean is35Hz = false;
    public boolean is40Hz = false;
    /**
     * 基线滤波
     */
    public boolean isBaselineFilter = true;
    /**
     * 工频滤波
     */
    public TextView tvPowerFrequency;
    /**
     * 肌电滤波
     */
    public TextView tvElectrical;
    /**
     * 基线滤波
     */
    public TextView tvBaselineFilter;
    /**
     * 硬件0.5hz滤波
     */
    public TextView tv05Hz;
    private long delayTime = 5000;

    public void initSdk() {
        mOsdkHelper = EcgOpenApiHelper.getInstance();
        mOsdkHelper.setDeviceType(DEVICE.CONNECT_TYPE_BLUETOOTH_DUAL);
        mOsdkHelper.setCountdownTime(delayTime);//这里是设置了点击开始记录后的延迟出图时间，用于测量人调整测量时的姿势，如果不需要延迟可不设置
        App.getApp().setOsdkCallback(mOsdkCallback);
    }

    OsdkCallback mOsdkCallback = new OsdkCallback() {

        @Override
        public void devicePlugIn() {
            isUsbPlugIn = true;
            ToastText("设备插入！");
            mDeviceStatusTV.setText("设备插入");
        }

        @Override
        public void devicePlugOut() {
            isUsbPlugIn = false;
            ToastText("设备拔出！");
            mDeviceStatusTV.setText("设备拔出");
        }

        @Override
        public void deviceSocketConnect() {
            ToastText("设备已连接！");
            mDeviceStatusTV.setText("已连接");
        }

        @Override
        public void deviceSocketLost() {
            ToastText("设备连接断开！");
            mDeviceStatusTV.setText("已断开");
        }

        @Override
        public void deviceReady(int sample) {
            ToastText("心电设备已准备好！");
            mDeviceStatusTV.setText("准备就绪,设备号:" + mOsdkHelper.getDeviceSN());
            ecgSample = sample;
            mEcgBrowser.setSample(sample);
        }

        @Override
        public void deviceNotReady(int msg) {
            switch (msg) {
                case EcgOpenApiCallback.DEVICE_NOT_READY_NOT_SUPPORT_DEVICE:// sdk不支持设备
                    ToastText("当前sdk设备无法使用此型号设备");// sdk不支持型号
                    mDeviceStatusTV.setText("型号不支持");
                    break;
                case EcgOpenApiCallback.DEVICE_NOT_READY_UNKNOWN_DEVICE:// 未知设备
                    ToastText("设备无法使用");// 设备故障或者非熙健产品
                    mDeviceStatusTV.setText("无法使用");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ecg_sdk_demo_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings_recordPng) {
            startActivity(new Intent(this, EcgPngActivity.class));
            return true;
        } else if (id == R.id.action_settings_recordStart) {
            mButtonRecordStart.performClick();
            return true;
        } else if (id == R.id.action_settings_recordStop) {
            mButtonRecordStop.performClick();
            return true;
        } else if (id == R.id.action_settings_datasource) {
            startActivity(new Intent(this, EcgDataSourceReviewActivity.class).putExtra("ecgFile", showDataFile));
            return true;
        } else if (id == R.id.action_settings_screen) {
            mScreenDialog.show();
            return true;
        } else if (id == R.id.action_settings_delay) {
            mDelayDialog.show();
            return true;
        } else if (id == R.id.action_settings_switch) {
            if (mOsdkHelper.isRunningRecord()) {
                ToastText("正在记录中，请等待记录完成后再操作");
                return true;
            }
            if (isUsbMode && isUsbPlugIn) {
                ToastText("请先拔除USB设备");
                return true;
            }
            isUsbMode = !isUsbMode;
            item.setTitle(isUsbMode ? "切换为蓝牙模式" : "切换为USB模式");
            switchMode();
            return true;
        } else if (id == R.id.action_settings_50Hz) {
            if (is50Hz) {
                ToastText("关闭50Hz滤波器");
                mOsdkHelper.set50hzFilter(false);
                is50Hz = false;
            } else {
                if (is60Hz) {
                    ToastText("关闭60Hz滤波器，开启50Hz滤波器");
                    mOsdkHelper.set60hzFilter(false);
                    is60Hz = false;
                } else {
                    ToastText("开启滤波器，开启50Hz滤波器");
                }
                mOsdkHelper.set50hzFilter(true);
                is50Hz = true;
            }
            switchPowerFrequencyFilter();
            return true;
        } else if (id == R.id.action_settings_60Hz) {
            if (is60Hz) {
                ToastText("关闭60Hz滤波器");
                mOsdkHelper.set60hzFilter(false);
                is60Hz = false;
            } else {
                if (is50Hz) {
                    ToastText("关闭50Hz滤波器，开启60Hz滤波器");
                    mOsdkHelper.set50hzFilter(false);
                    is50Hz = false;
                } else {
                    ToastText("开启滤波器，开启60Hz滤波器");
                }
                mOsdkHelper.set60hzFilter(true);
                is60Hz = true;
            }
            switchPowerFrequencyFilter();
            return true;
        } else if (id == R.id.action_settings_25Hz) {
            if (is25Hz) {
                ToastText("关闭滤波器，关闭25Hz滤波器");
                mOsdkHelper.set25hzFilter(false);
                is25Hz = false;
            } else {
                if (is35Hz) {
                    ToastText("关闭35Hz滤波器，开启25Hz滤波器");
                    mOsdkHelper.set35hzFilter(false);
                    mOsdkHelper.set25hzFilter(true);
                    is25Hz = true;
                    is35Hz = false;
                } else if (is40Hz) {
                    ToastText("关闭40Hz滤波器，开启25Hz滤波器");
                    mOsdkHelper.set40hzFilter(false);
                    mOsdkHelper.set25hzFilter(true);
                    is40Hz = false;
                    is25Hz = true;
                } else {
                    ToastText("开启滤波器，开启25Hz滤波器");
                    mOsdkHelper.set25hzFilter(true);
                    is25Hz = true;
                    is35Hz = false;
                    is40Hz = false;
                }
            }
            switchElectricalFilter();
            return true;

        } else if (id == R.id.action_settings_35Hz) {
            if (is35Hz) {
                ToastText("关闭滤波器，关闭35Hz滤波器");
                mOsdkHelper.set35hzFilter(false);
                is35Hz = false;
            } else {
                if (is25Hz) {
                    ToastText("关闭25Hz滤波器，开启35Hz滤波器");
                    mOsdkHelper.set25hzFilter(false);
                    mOsdkHelper.set35hzFilter(true);
                    is25Hz = false;
                    is35Hz = true;
                } else if (is40Hz) {
                    ToastText("关闭40Hz滤波器，开启35Hz滤波器");
                    mOsdkHelper.set40hzFilter(false);
                    mOsdkHelper.set35hzFilter(true);
                    is40Hz = false;
                    is35Hz = true;
                } else {
                    ToastText("开启滤波器，开启35Hz滤波器");
                    mOsdkHelper.set35hzFilter(true);
                    is25Hz = false;
                    is35Hz = true;
                    is40Hz = false;
                }
            }
            switchElectricalFilter();
            return true;

        } else if (id == R.id.action_settings_40Hz) {
            if (is40Hz) {
                ToastText("关闭滤波器，关闭40Hz滤波器");
                mOsdkHelper.set40hzFilter(false);
                is40Hz = false;
            } else {
                if (is25Hz) {
                    ToastText("关闭25Hz滤波器，开启40Hz滤波器");
                    mOsdkHelper.set25hzFilter(false);
                    mOsdkHelper.set40hzFilter(true);
                    is25Hz = false;
                    is40Hz = true;
                } else if (is35Hz) {
                    ToastText("关闭35Hz滤波器，开启40Hz滤波器");
                    mOsdkHelper.set35hzFilter(false);
                    mOsdkHelper.set40hzFilter(true);
                    is40Hz = true;
                    is35Hz = false;
                } else {
                    ToastText("开启滤波器，开启40Hz滤波器");
                    mOsdkHelper.set40hzFilter(true);
                    is25Hz = false;
                    is35Hz = false;
                    is40Hz = true;
                }
            }
            switchElectricalFilter();
            return true;
        } else if (id == R.id.action_settings_baseline) {
            isBaselineFilter = !isBaselineFilter;
            mOsdkHelper.setOpenBaseLineRebuild(isBaselineFilter);
            switchBaselineFilter();
            return true;
        } else if (id == R.id.action_settings_05Hz) {
            if (!mOsdkHelper.isDeviceConnected()) {
                ToastText("没有连接设备");
                return true;
            }
            new Thread(new Runnable() {

                @Override
                public void run() {
                    is05Hz = !is05Hz;
                    mOsdkHelper.set05hzFilter(is05Hz);
                    is05Hz = mOsdkHelper.get05hzFilterIsOpen();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            tv05Hz.setText(is05Hz ? "0.5Hz开启" : "0.5Hz关闭");
                        }
                    });
                }
            }).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchPowerFrequencyFilter() {
        if (is50Hz) {
            tvPowerFrequency.setText("50Hz开启，60Hz关闭");
        }
        if (is60Hz) {
            tvPowerFrequency.setText("50Hz关闭，60Hz开启");
        }
        if (!is50Hz && !is60Hz) {
            tvPowerFrequency.setText("50Hz关闭，60Hz关闭");
        }
    }

    public void switchElectricalFilter() {
        if (is25Hz) {
            tvElectrical.setText("25Hz开启，35Hz关闭，40Hz关闭");
        }
        if (is35Hz) {
            tvElectrical.setText("25Hz关闭，35Hz开启，40Hz关闭");
        }
        if (is40Hz) {
            tvElectrical.setText("25Hz关闭，35Hz关闭，40Hz开启");
        }
        if (!is25Hz && !is35Hz && !is40Hz) {
            tvElectrical.setText("25Hz关闭，35Hz关闭，40Hz关闭");
        }
    }

    public void switchBaselineFilter() {
        if (isBaselineFilter) {
            tvBaselineFilter.setText("基线滤波开");
        } else {
            tvBaselineFilter.setText("基线滤波关");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_bluetooth);
        mEcgBrowser = (RealTimeEcgBrowser) findViewById(R.id.ecgBrowser);
        mEcgBrowser.setEcgBrowserInteractive(mEcgBrowserInteractive);

        mButtonRecordStart = (Button) findViewById(R.id.button_record_start);
        mButtonRecordStop = (Button) findViewById(R.id.button_record_stop);

        mButtonRecordStart.setOnClickListener(this);
        mButtonRecordStop.setOnClickListener(this);

        mBtnconnect = (Button) findViewById(R.id.button_bt_connect);
        mBtndisconnect = (Button) findViewById(R.id.button_bt_disconnect);
        mBtnconnect.setOnClickListener(this);
        mBtndisconnect.setOnClickListener(this);

        mSetBluetoothButton = (Button) findViewById(R.id.button_select_bt);
        mSetBluetoothButton.setOnClickListener(this);

        mFreeze = (Button) findViewById(R.id.button_freeze);
        mFreeze.setOnClickListener(this);

        mUnFreeze = (Button) findViewById(R.id.button_unfreeze);
        mUnFreeze.setOnClickListener(this);

        mBtnRegister = (Button) findViewById(R.id.button_register);
        mBtnLogin = (Button) findViewById(R.id.button_login);
        mBtnRegister.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);

        mRecordTimeDialog = new AlertDialog.Builder(this).setTitle("选择测量时间")
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
                            case 5:
                                mode = RECORD_MODE.RECORD_MODE_MANUAL;
                                break;
                        }
                        startRecord(mode);
                        mRecordTimeDialog.dismiss();
                    }
                }).create();

        final EditText et = new EditText(this);
        et.setHint("请输入屏幕尺寸");
        et.setText("5");
        et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        mScreenDialog = new AlertDialog.Builder(this).setTitle("设置屏幕尺寸").setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (!TextUtils.isEmpty(input)) {
                            float screenInch = 5;
                            try {
                                screenInch = Float.parseFloat(input.trim());
                                if (screenInch < 3.5f || screenInch > 14f) {
                                    ToastText("屏幕尺寸允许的范围是：3.5到14");
                                    return;
                                }
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

        final EditText delayEdt = new EditText(this);
        delayEdt.setHint("请输入开始记录延迟时间");
        delayEdt.setText("5");
        delayEdt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        mDelayDialog = new AlertDialog.Builder(this).setTitle("设置开始记录延迟时间（秒）").setView(delayEdt)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = delayEdt.getText().toString();
                        if (!TextUtils.isEmpty(input)) {
                            float delay = 5;
                            try {
                                delay = Float.parseFloat(input.trim());
                                if (delay < 0) {
                                    ToastText("请输入正数");
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                ToastText("输入格式错误");
                            }
                            delayTime = (long) delay * 1000;
                            mOsdkHelper.setCountdownTime(delayTime);
                        }
                    }
                }).setNegativeButton("取消", null).create();

        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setMessage("即将开始测量，请保持好正确的测量姿势");
        mWaitDialog.setIndeterminate(true);
        mWaitDialog.setCancelable(false);

        initLable();
        initSdk();
        initEcg();
    }

    public void initLable() {
        hr = (TextView) findViewById(R.id.label_heartrate_realtime);
        rr = (TextView) findViewById(R.id.label_rr_value);
        step = (TextView) findViewById(R.id.label_step_value);
        acc = (TextView) findViewById(R.id.label_acc_value);
        battery = (TextView) findViewById(R.id.label_bat_value);

        result = (TextView) findViewById(R.id.label_result);
        counter = (TextView) findViewById(R.id.label_counter);
        speed = (TextView) findViewById(R.id.label_speed);
        gain = (TextView) findViewById(R.id.label_gain);
        motionHr = (TextView) findViewById(R.id.label_motion_hr_value);

        met = (TextView) findViewById(R.id.label_met_value);
        ka = (TextView) findViewById(R.id.label_ka_value);

        motionRR = (TextView) findViewById(R.id.label_motion_rr_value);

        cpu = (TextView) findViewById(R.id.label_cpu_value);
        debugMsg = (TextView) findViewById(R.id.label_debug1);
        atrialFibrillation = (TextView) findViewById(R.id.label_atrialFibrillation_value);
        tvRhythm = (TextView) findViewById(R.id.label_rhythm_value);
        tvLibname = (TextView) findViewById(R.id.label_lib_value);
        tvAcc = (TextView) findViewById(R.id.tv_acc);
        tvRresult = (TextView) findViewById(R.id.label_ecg_result_value);

        mDeviceStatusTV = (TextView) findViewById(R.id.label_device_value);
        mRecordProcessTV = (TextView) findViewById(R.id.tv_record_process);
        mBatteryView = findViewById(R.id.view_battery);
        mAccView = findViewById(R.id.view_acc);

        tvPowerFrequency = (TextView) findViewById(R.id.tv_powerFrequency);
        tvElectrical = (TextView) findViewById(R.id.tv_electrical);
        tvBaselineFilter = (TextView) findViewById(R.id.tv_baselinefilter);
        tv05Hz = (TextView) findViewById(R.id.tv_05hz);
    }

    public void clearValue() {
        hr.setText("---");
        rr.setText("---");
        step.setText("");
        acc.setText("");
        battery.setText("---");
//		speed.setText("");
//		gain.setText("");
        motionHr.setText("");

        met.setText("");
        ka.setText("");
        motionRR.setText("");
        cpu.setText("");
        debugMsg.setText("");
        atrialFibrillation.setText("");
        tvRhythm.setText("");
        tvLibname.setText("");
        tvAcc.setText("");
        tvRresult.setText("");
    }

    public void switchMode() {
        if (isUsbMode) {
            if (!App.hasSystemFeature_USB_HOST()) {
                Toast.makeText(this, "系统不支持usb host，无法连接设备", Toast.LENGTH_LONG).show();
            }
            mOsdkHelper.setDeviceType(DEVICE.CONNECT_TYPE_USB);
            mRecordProcessTV.setText("记录流程：注册->登录->开始记录->结束记录");
            mBatteryView.setVisibility(View.GONE);
            mAccView.setVisibility(View.GONE);
            mSetBluetoothButton.setVisibility(View.GONE);
            mBtnconnect.setVisibility(View.GONE);
            mBtndisconnect.setVisibility(View.GONE);
        } else {
            mOsdkHelper.setDeviceType(DEVICE.CONNECT_TYPE_BLUETOOTH_DUAL);
            mRecordProcessTV.setText("记录流程：注册->登录->蓝牙设置->连接->开始记录->结束记录");
            mBatteryView.setVisibility(View.VISIBLE);
            mAccView.setVisibility(View.VISIBLE);
            mSetBluetoothButton.setVisibility(View.VISIBLE);
            mBtnconnect.setVisibility(View.VISIBLE);
            mBtndisconnect.setVisibility(View.VISIBLE);
        }
        clearValue();
        mDeviceStatusTV.setText("---");
        result.setText("");
        counter.setText("");
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
        mEcgBrowser.showFps(false);
        mEcgBrowser.setScreenDPI(mEcgBrowser.getDisplayDPI());
        mEcgBrowser.clearEcg();
    }

    EcgBrowserInteractive mEcgBrowserInteractive = new EcgBrowserInteractive() {

        @Override
        public void onChangeGainAndSpeed(int gain, int speed) {
            displayMessage.obtainMessage(ECG_GAIN_SPEED, gain, speed).sendToTarget();
        }
    };

    /**
     * 开始记录
     */
    public void startRecord(RECORD_MODE mode) {
        try {
            mWaitDialog.show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mWaitDialog != null && mWaitDialog.isShowing()) {
                        mWaitDialog.dismiss();
                    }
                }
            }, delayTime);
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
            case R.id.button_select_bt:
                if (mOsdkHelper.isRunningRecord()) {
                    ToastText("正在记录中，请等待记录完成后再操作");
                    return;
                }
                if (mOsdkHelper.getDeviceType() == DEVICE.CONNECT_TYPE_BLUETOOTH_DUAL) {
                    startActivityForResult(new Intent(this, BleDeviceListActivity.class), REQUEST_CODE);
                }
                break;
            case R.id.button_record_start:
                if (TextUtils.isEmpty(mOsdkHelper.getDeviceSN())) {
                    ToastText("请先连接设备");
                    return;
                }
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
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastText("【关闭记录】文件异常,开始记录失败！");
                    }
                } else {
                    ToastText("没有开始记录");
                }
                break;
            case R.id.button_bt_connect:
                if (mOsdkHelper.isRunningRecord()) {
                    ToastText("正在记录中，请等待记录完成后再操作");
                    return;
                }
                if (!TextUtils.isEmpty(macAddress)) {// 连接蓝牙
                    int type = BluetoothDevice.DEVICE_TYPE_CLASSIC;
                    if (device != null) {
                        type = device.getType();
                    }
                    mOsdkHelper.connectBluetooth(macAddress, type);
                } else {
                    ToastText("没有设置蓝牙");
                }
                break;
            case R.id.button_bt_disconnect:
                try {
                    if (mOsdkHelper.isRunningRecord()) {
                        mOsdkHelper.stopRecord();
                    }
                    mOsdkHelper.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mDeviceStatusTV.setText("已断开");
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
            default:
                break;
        }
    }

    RecordCallback mRecordCallback = new RecordCallback() {

        @Override
        public void recordTime(int second) {
            Log.w(getClass().getSimpleName(), "recordTime--- second=" + second);
            displayMessage.obtainMessage(ECG_COUNTER, second, -1).sendToTarget();
        }

//		@Override //不用了
//		public void recordStatistics(String id, int averageHeartRate, int normalRange, int suspectedRisk) {
//			if (null != id) {
//				String msg = "平均心率：" + averageHeartRate + "(bpm),心率正常范围：" + normalRange + "%" + ",节律正常范围：" + suspectedRisk + "%";
//				displayMessage.obtainMessage(ECG_STAISTICS_RESULT, msg).sendToTarget();
//				ToastText("统计分析完成");
//			} else {
//				String msg = "【统计数据异常】";// 一般是数据文件错误引起
//				displayMessage.obtainMessage(ECG_STAISTICS_RESULT, msg).sendToTarget();
//			}
//		}


        @Override
        public void recordStatistics(String id, int averageHeartRate,
                                     int[] heartRectPercentages, int[] rhythmRectPercentages,
                                     int rhythmType) {
            if (null != id) {
                // FIXME 节律异常范围，修改为节律正常范围
                String msg = "平均心率：" + averageHeartRate + "(bpm),心率数组值：(" + heartRectPercentages[0] + "," + heartRectPercentages[1] + "," + heartRectPercentages[2] + ")"
                        + ",节律数组：(" + rhythmRectPercentages[0] + "," + rhythmRectPercentages[1] + "," + rhythmRectPercentages[2] + ")"
                        + ",风险提示：" + rhythmType;
                displayMessage.obtainMessage(ECG_STAISTICS_RESULT, msg).sendToTarget();

                String toastMSG = "平均心率：" + averageHeartRate + "(bpm),心率正常范围：" + heartRectPercentages[0]
                        + "%,稍快稍慢：" + heartRectPercentages[1] + "%,过快过慢：" + heartRectPercentages[2]
                        + "%,节律正常范围：" + rhythmRectPercentages[0]
                        + "%,疑似心率不齐或早搏：" + rhythmRectPercentages[1] + "%,疑似心房颤动或早搏：" + rhythmRectPercentages[2] + "%";
                String warnString = "";
                if (rhythmType == 2) {
                    warnString = "心脏节律异常风险-中" + " --- " + "如您多次在静止、无干扰的状态下测量，异常节律仍高于10%，为了您的健康，请您咨询医师或专业人员。异常节律可以是心律不齐或者早搏、干扰引起，请您咨询医师或专业人员。请您定期或随时监测，跟踪心脏健康风险。";
                } else if (rhythmType == 3) {
                    warnString = "心脏节律异常风险-高" + " --- " + "如您多次在静止、无干扰的状态下测量，异常节律仍高于20%，提示您的心脏可能存在心律失常风险，建议您尽快咨询医师或专业人员。请您定期和随时监测，跟踪心脏健康风险。";
                } else {//rhythmType = 1
                    warnString = "心脏节律异常风险-低" + " --- " + "您的心脏节律异常风险低。请您继续保持良好的生活习惯：清淡饮食、适量运动、保证睡眠、戒烟限酒。少量的异常节律可以是心律不齐或者早搏、干扰引起，请您咨询医师或专业人员。定期和随时监测，有助您提早发现心脏风险。";
                }
                ToastText("统计分析完成:" + toastMSG + "\n" + warnString);
            } else {
                String msg = "【统计数据异常】";// 一般是数据文件错误引起
                displayMessage.obtainMessage(ECG_STAISTICS_RESULT, msg).sendToTarget();
            }
        }

        @Override
        public void recordStart(String id) {
            if (mWaitDialog != null && mWaitDialog.isShowing()) {
                mWaitDialog.dismiss();
            }
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
                        createPng(filename);
                        displayMessage.obtainMessage(ECG_SHOW_DATA, filename).sendToTarget();
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
            if (mWaitDialog != null && mWaitDialog.isShowing()) {
                mWaitDialog.dismiss();
            }
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
                    text = "还没有登录";
                    break;
                case RECORD_FAIL_OSDK_INIT_ERROR:
                    text = "osdk没有初始化";
                    break;
                case RECORD_FAIL_PARAMETER:
                    text = "参数错误";
                    break;
                case RECORD_FAIL_LOW_VERSION:
                    text = "开发者验证失败,版本低,需要升级sdk";
                    break;
                case RECORD_FAIL_VALIDATE_SDK_FAILED_PACKAGE_NAME_MISMATCH:
                    text = "开发者验证失败,包名不匹配";
                    break;
                case RECORD_FAIL_VALIDATE_SDK_FAILED_ACCOUNT_FROZEN:
                    text = "开发者验证失败,账户冻结";
                    break;
                case RECORD_FAIL_VALIDATE_SDK_FAILED_NETWORK_UNAVAILABLE:
                    text = "开发者验证失败,没有网络";
                    break;
                case RECORD_FAIL_VALIDATE_SDK_FAILED:
                    text = "开发者验证失败";
                    break;
                default:
                    break;
            }
            ToastText("开始记录失败：" + text);
        }

        @Override
        public void battery(int value) {
            displayMessage.obtainMessage(ECG_BATTERY, value, -1).sendToTarget();
        }

        @Override
        public void addAccelerate(short x, short y, short z) {
            displayMessage.obtainMessage(ECG_ACC, "x:" + x + " y:" + y + " z:" + z).sendToTarget();
        }

        @Override
        public void addAccelerateVector(float arg0) {
        }

        @Override
        public void leadOff(boolean isOff) {
            Log.i("导联提示", isOff ? "导联脱落" : "导联正常");
        }
    };

    /**
     * 生成png图
     *
     * @param filename
     */
    private void createPng(final String filename) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Context context = getApplicationContext();
                InputStream is = null;
                try {
                    is =  new FileInputStream(new File(filename));// 心率60，usb设备，振幅1.0mV.ecg
                    int[] ecg = readEcg(is);

                    int ecgSampleHz = 200;// 采样率：USB设备的采样率为500，蓝牙设备的采样率为200
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
                    displayMessage.obtainMessage(0, msg).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return new AlertDialog.Builder(this).setIconAttribute(android.R.attr.alertDialogIcon).setTitle("查看记录")
                .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (showDataFile != null) {
                            Intent intent = new Intent(EcgSdkBluetoothDemo.this, EcgDataSourceReviewActivity.class);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mOsdkHelper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("SDK", "---  onDestroy ");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
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

    final static int REQUEST_CODE = 1000;// 蓝牙地址

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult ---  requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        if (requestCode == REQUEST_CODE) {
            String mac = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i(TAG, "onActivityResult ---  mac=" + mac);
            macAddress = mac;
        }
    }

    static final int ECG_GAIN_SPEED = 10001;
    static final int TOAST_TEXT = 10002;
    static final int CPU_STATE = 10003;
    static final int DEBUG_STATE = 10004;
    static final int LIB_NAME = 10005;
    static final int ECG_COUNTER = 10006;
    static final int ECG_SHOW_DATA = 10007;
    static final int ECG_STAISTICS_RESULT = 10008;
    static final int ECG_ACC = 10009;

    /**
     * 显示刷新
     */
    Handler displayMessage = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 0:
                    Toast.makeText(getBaseContext(), "生成图片成功", Toast.LENGTH_SHORT).show();
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
                        rr.setText("" + msg.arg1 + " 节律比例：" + mOsdkHelper.rrIndicator(msg.arg1) + "%");
                    }
                    break;
                case ECG_COUNTER:
                    counter.setText(msg.arg1 + "");
                    break;
                case ECG_STAISTICS_RESULT:
                    String text = (String) msg.obj;
                    if (text != null)
                        result.setText(text);
                    break;
                case ECG_GAIN_SPEED:
                    float speedValue = msg.arg2 / 10.0f;
                    float gainValue = msg.arg1 / 10.0f;
                    speed.setText("" + speedValue + " mm/s");
                    gain.setText("" + gainValue + " mm/mV");
                    break;
                case TOAST_TEXT:
                    String t = (String) msg.obj;
                    if (t != null)
                        Toast.makeText(getBaseContext(), t, Toast.LENGTH_SHORT).show();
                    break;
                case ECG_SHOW_DATA:
                    showDataFile = (String) msg.obj;
                    if (showDataFile != null) {
                        showDialog(0);
                    }
                    mEcgBrowser.clearEcg();
                    clearValue();
                    break;
                case ECG_BATTERY:
                    battery.setText(msg.arg1 + "/3");
                    if (msg.arg1 == 0) {
                        battery.setText("电量不足：" + battery.getText());
                    }
                    break;
                case ECG_ACC:
                    String acc = (String) msg.obj;
                    if (acc != null) {
                        tvAcc.setText(acc);
                    }
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


    //**************************************************以下是生成图片
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
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
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