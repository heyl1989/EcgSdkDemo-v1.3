package com.vodone.o2o.hulianwangyy_guizhou.demander.file;

/**
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com
 * create by lc  2015年8月31日 上午11:07:37 
 */


import java.util.ArrayList;

import com.mhealth365.osdk.ecgbrowser.DataSourceEcgBrowser.DataSourceReader;

public class EcgDataSource implements DataSourceReader {

	long startTime = 0;
	int sample = 0;
	private int countEcg = 0;
	private ArrayList<int[]> packageList = new ArrayList<int[]>();

	public EcgDataSource(long startTime, int sample) throws Exception {
		if (sample <= 0)
			throw new Exception("EcgDataSource: sample<=0");
		this.startTime = startTime;
		this.sample = sample;
		countEcg = 0;
	}

	public void fillPackage(ArrayList<int[]> data) {
		if (data == null)
			return;
		packageList.addAll(data);
		countEcg += data.size();
	}

	public void addPackage(int[] data) {
		packageList.add(data);
		countEcg++;
	}

	@Override
	public long getDataStartTime() {
		return startTime;
	}

	@Override
	public int getSample() {
		return sample;
	}

	@Override
	public long getPackageNum() {
		return packageList.size();
	}

	public int getSeconds() {
		if (sample == 0)
			return -1;
		float size = getPackageNum();
		int seconds = (int) Math.floor(size / sample);
		return seconds;
	}

	@Override
	public ArrayList<int[]> read(long index, int num) {
		// Log.i("EcgDataSource", "index:"+index+",num:"+num);
		ArrayList<int[]> copy = new ArrayList<int[]>();
		long end = index + num;
		for (long i = index; i < end; i++) {
			copy.add(packageList.get((int) i));
		}
		return copy;
	}

	@Override
	public void updateIndex(int indexSecond) {
	}

	@Override
	public String toString() {
		float seconds = 0;
		float hz = sample;
		if (hz > 0) {
			seconds = getPackageNum() / hz;
		}
		return "EcgDataSource[" + "startTime:" + startTime + ",sample:" + sample + ",size:" + getPackageNum() + ",seconds:" + seconds + ",countEcg:" + countEcg
				+ "]";
	}

	public ArrayList<int[]> getEcgData() {
		return packageList;
	}
}