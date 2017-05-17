/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com
 * create by lc  2015年9月1日 上午10:15:36 
 */

package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.mhealth365.osdk.ecgbrowser.DataSourceEcgBrowser;
import com.mhealth365.osdk.ecgbrowser.DataSourceEcgBrowser.DataSourceIndex;
import com.mhealth365.osdk.ecgbrowser.EcgBrowserInteractive;
import com.mhealth365.osdk.ecgbrowser.Scale;
import com.vodone.o2o.hulianwangyy_guizhou.demander.file.EcgDataSource;
import com.vodone.o2o.hulianwangyy_guizhou.demander.file.EcgFile;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class EcgDataSourceReviewActivity extends Activity {
	
	DataSourceEcgBrowser mEcgBrowser = null;
	TextView tvIndexSecondsLeft, tvIndexSecondsRight, tvIndexSeconds, tvStartTime, tvDataTotalSeconds, tvPage, tvFileName, tvFileState;
	SeekBar mSeekBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ecg_demo_review);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar_index_seconds);
		mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		tvIndexSeconds = (TextView) findViewById(R.id.data_source_index_seconds_center);
		tvIndexSecondsLeft = (TextView) findViewById(R.id.data_source_index_seconds_left);
		tvIndexSecondsRight = (TextView) findViewById(R.id.data_source_index_seconds_right);
		tvStartTime = (TextView) findViewById(R.id.data_source_starttime_value);
		tvDataTotalSeconds = (TextView) findViewById(R.id.data_source_total_seconds_value);
		tvPage = (TextView) findViewById(R.id.data_source_index_page_value);
		tvFileName = (TextView) findViewById(R.id.data_source_filename_value);
		tvFileState = (TextView) findViewById(R.id.data_source_filename_state);

		mEcgBrowser = (DataSourceEcgBrowser) findViewById(R.id.DataSourceEcgBrowser);
		mEcgBrowser.setEcgBrowserInteractive(mEcgBrowserInteractive);
		mEcgBrowser.setDataSourceIndex(mDataSourceIndex);
		mEcgBrowser.setSpeedAndGain(Scale.SPEED_25MM_S, Scale.GAIN_10MM_MV);// 设置增益和走速
		mEcgBrowser.setSample(500);
		mEcgBrowser.showFps(false);
		mEcgBrowser.clearEcg();

		String filename = getIntent().getStringExtra("ecgFile");
		try {
			if (filename != null) {
				openFile(filename);
				tvFileName.setText(filename);
			} else {
				filename = "1489383638808.ecg";
				openAssets(filename);
				tvFileName.setText("assets/"+filename);
			}
			tvFileState.setText("加载成功");
		} catch (Exception e) {
			e.printStackTrace();
			tvFileState.setText("加载失败");
		}
	}

	private void openFile(String filename) throws Exception {
		EcgDataSource dataSourceReader = null;
		try {
			dataSourceReader = EcgFile.read(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		openDataSource(dataSourceReader);
	}

	private void openAssets(String filename) throws Exception {
		Resources r = this.getResources();
		InputStream is = null;
		try {
			is = r.getAssets().open(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (is != null) {
			EcgDataSource dataSourceReader = null;
			try {
				dataSourceReader = EcgFile.read(is);
			} catch (IOException e) {
				e.printStackTrace();
			}

			openDataSource(dataSourceReader);
		}
	}

	private void openDataSource(EcgDataSource dataSourceReader) {
		if (dataSourceReader != null) {
			long time = dataSourceReader.getDataStartTime();
			int seconds = dataSourceReader.getSeconds();
			//Log.w("EcgDataSourceReviewActivity", "---openDataSource---  seconds:" + seconds);
			mSeekBar.setMax(seconds);
			tvDataTotalSeconds.setText("" + seconds);
			tvStartTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(time)));
			mEcgBrowser.setSample(dataSourceReader.getSample());
			mEcgBrowser.setDataSourceReader(dataSourceReader);
		}
	}

	OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mEcgBrowser.setOffsetTimeTo(seekBar.getProgress());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

		}
	};

	EcgBrowserInteractive mEcgBrowserInteractive = new EcgBrowserInteractive() {

		@Override
		public void onChangeGainAndSpeed(int gain, int speed) {
			// displayMessage.obtainMessage(ECG_GAIN_SPEED,gain,speed).sendToTarget();
		}
	};

	DataSourceIndex mDataSourceIndex = new DataSourceIndex() {

		/**
		 * @param indexSecond
		 *            屏幕水平中线时刻
		 * @param startSecond
		 *            屏幕左侧时刻
		 * @param endSecond
		 *            屏幕右侧时刻
		 * */
		@Override
		public void updateIndex(int indexSecond, int startSecond, int endSecond) {
			int index = mSeekBar.getProgress();
			if (index >= startSecond && index <= endSecond) {
			} else {
				mSeekBar.setProgress(indexSecond);
			}
			displayMessage.obtainMessage(MSG_DISPLAY_INDEX_SECOND, startSecond, endSecond, "" + indexSecond + "").sendToTarget();
		}

//		@Override
//		public void updateIndexPage(int indexSecond, int indexPage, int PageNum) {
//			displayMessage.obtainMessage(MSG_DISPLAY_INDEX_PAGE, indexPage, PageNum).sendToTarget();
//
//		}

	};

	final static int MSG_DISPLAY_INDEX_SECOND = 1;
	final static int MSG_DISPLAY_INDEX_PAGE = 2;
	private final Handler displayMessage = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DISPLAY_INDEX_SECOND: {
				String text = (String) msg.obj;
				int left = msg.arg1;
				int right = msg.arg2;
				int index = Integer.parseInt(text);
				// mSeekBar.setProgress(index);
				tvIndexSeconds.setText(text + " s");
				tvIndexSecondsLeft.setText("" + left + " s");
				tvIndexSecondsRight.setText("" + right + " s");
			}
				break;
			case MSG_DISPLAY_INDEX_PAGE: {
				int indexPage = msg.arg1;
				int pageNum = msg.arg2;
				tvPage.setText("" + indexPage + "/" + pageNum);
			}
				break;
			default:
				break;
			}
		}
	};
}