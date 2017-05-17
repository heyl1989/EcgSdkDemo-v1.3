/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com All rights reserved.
 * create by lc  2015年6月16日 上午9:56:01 
 */

package com.vodone.o2o.hulianwangyy_guizhou.demander;

import com.mhealth365.osdk.EcgOpenApiCallback.REGISTER_FAIL_MSG;
import com.mhealth365.osdk.EcgOpenApiCallback.RegisterCallback;
import com.mhealth365.osdk.EcgOpenApiHelper;
import com.mhealth365.osdk.UserInfo;
import com.mhealth365.osdk.UserInfo.HEART_DISEASE;
import com.mhealth365.osdk.UserInfo.SEX;
import com.mhealth365.osdk.UserInfo.UserInfoError;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/** 注册 */
public class RegisterActivity extends Activity {

	private Button btnRegister;
	private EditText etName, etPhone, etCid, etOpenId, etUserName;
	private EditText etEmail, etAge, etSex, etHeight;
	private EditText etWeight, etHeartDisease, etAppeal, etMedicalHistory;
	private EditText etAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ecg_sdk_demo_register);

		etName = (EditText) findViewById(R.id.editName);
		etPhone = (EditText) findViewById(R.id.editPhone);
		etCid = (EditText) findViewById(R.id.editCid);
		etOpenId = (EditText) findViewById(R.id.editOpenId);
		etUserName = (EditText) findViewById(R.id.editAccountName);

		etEmail = (EditText) findViewById(R.id.editEmail);
		etAge = (EditText) findViewById(R.id.editAge);
		etSex = (EditText) findViewById(R.id.editSex);
		etHeight = (EditText) findViewById(R.id.editHeight);

		etWeight = (EditText) findViewById(R.id.editWeight);
		etHeartDisease = (EditText) findViewById(R.id.editHeartDisease);
		etAppeal = (EditText) findViewById(R.id.editAppeal);
		etMedicalHistory = (EditText) findViewById(R.id.editMedicalHistory);

		etAddress = (EditText) findViewById(R.id.editAddress);

		btnRegister = (Button) findViewById(R.id.register_button);
		btnRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("RegisterActivity", "register----");
				
				UserInfo ui = new UserInfo();
				ui.userName = "" + getEditTextValue(etUserName);// 可能是手机号或者email
				ui.openId = "" + getEditTextValue(etOpenId);
				ui.phone = "" + getEditTextValue(etPhone);
				ui.realName = "" + getEditTextValue(etName);
				ui.cid = "" + getEditTextValue(etCid);
				ui.email = "" + getEditTextValue(etEmail);

				ui.age = getInt(getEditTextValue(etAge));
				ui.sex = getSexValue(getEditTextValue(etSex));
				ui.height = getInt(getEditTextValue(etHeight));
				ui.weight = getInt(getEditTextValue(etWeight));
				ui.haveHeartDisease = getHeartDiseaseValue(getEditTextValue(etHeartDisease));
				ui.appeal = "" + getEditTextValue(etAppeal);
				ui.medicalHistory = "" + getEditTextValue(etMedicalHistory);
				ui.addr = "" + getEditTextValue(etAddress);

				Log.d("Register", "UserInfo----checkParams");
				UserInfo.UserInfoError err = ui.checkParams();

				if (err == UserInfoError.UserInfoError_No) {
					Log.d("Register", "registerUser----");
					EcgOpenApiHelper mHelper = EcgOpenApiHelper.getInstance();
					mHelper.registerUser(ui, mRegisterCallback);
				} else {
					checkValues(err);
				}
			}
		});
	}

	private void checkValues(UserInfo.UserInfoError err) {
		switch (err) {
		case UserInfoError_Addr:
			ToastText("地址长度不能超过30英文字符");
			break;
		case UserInfoError_Age:
			ToastText("年龄需要是正整数");
			break;
		case UserInfoError_Appeal:
			ToastText("诉求不能超过50个英文字符");
			break;
		case UserInfoError_Cid:
			ToastText("身份证号码超过长度");
			break;
		case UserInfoError_Email:
			ToastText("Email 格式不正确，或者超过50个英文字符");
			break;
		case UserInfoError_Height:
			ToastText("身高需要是正整数");
			break;
		case UserInfoError_MedicalHistory:
			ToastText("病史不能超过10个英文字符");
			break;
		case UserInfoError_OpenId:
			ToastText("openid 不能为空或者超过45个英文字符");
			break;
		case UserInfoError_Phone:
			ToastText("手机号码格式不正确");
			break;
		case UserInfoError_RealName:
			ToastText("昵称不能为空或者超过20个英文字符");
			break;
		case UserInfoError_UserName:
			ToastText("用户名为空或者超过50个英文字符");
			break;
		case UserInfoError_Weight:
			ToastText("体重需要是正整数");
			break;
		default:
			break;
		}
	}

	private boolean checkValues() {
//		EcgOpenApiHelper mHelper = EcgOpenApiHelper.getInstance();
		String userNameStr = getEditTextValue(etUserName);
//		if (!emptyString(userNameStr) && mHelper.checkUserName(userNameStr) == null) {
//			ToastText("用户名：无效值，输入手机号或者email");
//			return false;
//		}
		if (!emptyString(userNameStr)) {
			ToastText("用户名：不能为空");
			return false;
		}

		String ageStr = getEditTextValue(etAge);
		if (!emptyString(ageStr) && getInt(ageStr) < 0) {
			ToastText("年龄：无效值");
			return false;
		}
		String sexStr = getEditTextValue(etSex);
		if (!emptyString(sexStr) && (getSexValue(sexStr) == null)) {
			ToastText("性别：无效值");
			return false;
		}
		String etHeightStr = getEditTextValue(etHeight);
		if (!emptyString(etHeightStr) && getInt(etHeightStr) < 0) {
			ToastText("身高：无效值");
			return false;
		}
		String etWeightStr = getEditTextValue(etWeight);
		if (!emptyString(etWeightStr) && getInt(etWeightStr) < 0) {
			ToastText("体重：无效值");
			return false;
		}
		String etHeartDiseaseStr = getEditTextValue(etHeartDisease);
		if (!emptyString(etHeartDiseaseStr) && getHeartDiseaseValue(etHeartDiseaseStr) == null) {
			ToastText("有无心脏病：无效值");
			return false;
		}
		return true;

	}

	RegisterCallback mRegisterCallback = new RegisterCallback() {

		@Override
		public void registerOk() {
			Log.i("Register", "registerOk");
			ToastText("注册成功");
		}

		@Override
		public void registerFailed(REGISTER_FAIL_MSG msg) {
			String text = "";
			if (msg == null) {
				text = "未知异常";
			} else {
				switch (msg) {
				case REGISTER_FAIL_NO_NET:
					text = "无网络";
					break;
				case REGISTER_FAIL_NO_RESPOND:
					text = "服务器异常";
					break;
				case REGISTER_FAIL_OSDK_INIT_ERROR:
					text = "osdk未始化";
					break;
				case REGISTER_FAIL_USER_EXIST:
					text = "用户已存在";
					break;
				case REGISTER_FAIL_USER_INFO_EMPTY:
					text = "用户信息不完整";
					break;
				case REGISTER_FAIL_USER_INFO_ERROR:// TODO 20151102
					// text ="注册信息错误，身高、体重、年龄不能是负数，手机号只支持11位手机号码（11位不包含+86）";
					text = "注册信息格式错误";// TODO 参数为空、超出范围等
					break;
				case REGISTER_FAIL_UNAUTHORIZED:
					text = "未授权";
					break;
				case REGISTER_FAIL_ACCOUNT_FROZEN:
					text = "账户冻结";
					break;
				case REGISTER_FAIL_PACKAGE_NAME_MISMATCH:
					text = "包名不匹配";
					break;
				// 20150716----------------------------
				case SYS_0:
					text = "系统错误";// 系统错误
					break;
				case SYS_USER_EXIST_E:
					text = "注册用户已回收";// Openid存在，但是账号已回收
					break;
				case SYS_THIRD_PARTY_ID_CHECKING:
					text = "公司id审核中";// thiredpartyId存在，正在审核未生效
					break;
				case SYS_THIRD_PARTY_ID_NOT_EXIST:
					text = "公司id不存在";// thiredpartyId不存在
					break;
				case SYS_APP_ID_CHECKING:
					text = "appid审核中";// appid存在，正在审核未生效
					break;
				case SYS_APP_ID_ERROR:
					// text ="appid不存在，或者appSecret有错误";//appid不存在，或者appSecret有错误
					text = "包名 appId 公司id 不匹配";// TODO 包名 appId 公司id 不匹配
					break;
				case SYS_APP_PACKAGE_ID_NOT_EXIST:
					text = "包名不存在";// 包名不正确
					break;
				case SYS_LOW_VERSION:
					text = "sdk版本低需要升级";
					break;
				default:
					break;
				}
			}

			ToastText("注册失败 " + text);
			Log.i("Register", "registerFailed");
		}

	};

	private HEART_DISEASE getHeartDiseaseValue(String string) {
		if (emptyString(string)) {
			return HEART_DISEASE.UNKNOW;
		} else if (string.equals("有")) {
			return HEART_DISEASE.YES;
		} else if (string.equals("无")) {
			return HEART_DISEASE.NO;
		} else if (string.equals("未知")) {
			return HEART_DISEASE.UNKNOW;
		} else {
			return null;
		}
	}

	private SEX getSexValue(String sex) {
		if (emptyString(sex)) {
			return SEX.SECRET;
		} else if (sex.equals("男")) {
			return SEX.MALE;
		} else if (sex.equals("女")) {
			return SEX.FAMALE;
		} else if (sex.equals("保密")) {
			return SEX.SECRET;
		}
		return null;
	}

	private String getEditTextValue(EditText et) {
		return et.getText().toString().trim();
	}

	final static int TOAST_TEXT = 10002;

	/** 显示刷新 */
	Handler displayMessage = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			int what = msg.what;
			switch (what) {
			case TOAST_TEXT:
				String text = (String) msg.obj;
				if (text != null)
					Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	private int getInt(String value) {
		int getValue = 0;
		if (emptyString(value)) {
			return getValue;
		}
		try {
			getValue = Integer.parseInt(value);
		} catch (Exception e) {
			getValue = -1;
		}

		return getValue;
	}

	private boolean emptyString(String str) {
		if (str == null)
			return true;
		if (str.equals(""))
			return true;
		return false;
	}

	public void ToastText(String text) {
		displayMessage.obtainMessage(TOAST_TEXT, text).sendToTarget();
	}
}