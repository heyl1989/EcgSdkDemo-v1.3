package com.vodone.o2o.hulianwangyy_guizhou.demander;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Color;
/**
 * 南京熙健信息技术有限公司
 * 				
 * 			心电图绘制演示
 * 
 * Copyright (C) 2017 mhealth365.com All rights reserved.
 * create by 刘畅  2017年3月14日
 * */
public class EcgRender {
	
	/** 每英寸对应的毫米数 */
	public static final float MM_PER_INCH = 25.4f;
	public int widthMM;
	public int heightMM;
	public String measureTime = "";
	public String name = "";
	public String hr = "";
	public String gender = "";
	public String age = "";
	
	public EcgRender(int dpi) {
		mDPI = dpi;
		mDpmm = getDpmm(mDPI);
	}
	
	/**
	 * 创建图片  
	 * @param widthMM  图片宽度 单位mm
	 * @param heightMM 图片高度 单位mm
	 * */
	public Bitmap createBitmap(int widthMM,int heightMM){
		this.widthMM = widthMM;
		this.heightMM = heightMM;
		int w = (int) (getDpmm() * widthMM);
		int h = (int) (getDpmm() * heightMM);
		return Bitmap.createBitmap(w, h, Config.RGB_565);
	}
	
	/**
	 * 将心电图绘制到图片上
	 * @param bitmap 图片
	 * @param ecg 心电数据  单位uv 基线0
	 * @param ecgSampleHz 心电采样率
	 * @param speed 走速
	 * @param gain 增益
	 * */
	public void drawEcg2Bitmap(Bitmap bitmap, int[] ecg, int ecgSampleHz, float speed, float gain) {
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		int space = (int) (getDpmm() * 5);// 5mm
		drawGrid(canvas, space, space, bitmap.getWidth() - space - space, bitmap.getHeight() - space - space, paint);

		int leftmm = 25;
		int offsetX = (int) (getDpmm() * leftmm);// 25mm
		int baseLineYmm = 50;
		
		int length = ecg.length;// 数据总数
		int lineData = ecgSampleHz * 10;// 每一行的数据数
		int lines = length % lineData == 0 ? length / lineData : length / lineData + 1;// 总行数
		for (int i = 0; i < lines; i++) {
			int startIndex = i * ecgSampleHz * 10;
			int endIndex = startIndex + ecgSampleHz * 10;
			endIndex = endIndex > length ? length : endIndex;
			int[] lineEcg = Arrays.copyOfRange(ecg, startIndex, endIndex);
			int offsetY = (int) (getDpmm() * baseLineYmm);// 25mm
			drawEcgLine(canvas, offsetX, offsetY, lineEcg, ecgSampleHz, speed, gain, 1, Color.BLACK, paint);
			drawStandSquareWave5mm(leftmm, baseLineYmm, gain, 1, null, canvas);
			int textSize = 5;
			drawText(canvas, "ECG", leftmm - 5, baseLineYmm - gain + textSize, textSize, Align.RIGHT);
			baseLineYmm += 25;
		}
		drawText(canvas, "测量时间: " + measureTime, 10, 15, 5, Align.LEFT);
		drawText(canvas, "姓名: " + name, 110, 15, 5, Align.LEFT);
		drawText(canvas, "HR: " + hr, 210, 15, 5, Align.RIGHT);
		drawText(canvas, "" + speed + " mm/s " + gain + " mm/mV", 280, 15, 5, Align.RIGHT);
		drawText(canvas, "性别: " + gender, 10, 25, 5, Align.LEFT);
		drawText(canvas, "年龄: " + age, 55, 25, 5, Align.LEFT);
		drawTimeLine(canvas, offsetX, (int) (getDpmm() * (heightMM - 5)), speed);
	}
	
	/**
	 * 绘制字符
	 * @param canvas
	 * @param text
	 * @param offsetXmm  横轴坐标  单位毫米
	 * @param offsetYmm  纵轴坐标  单位毫米
	 * @param sizemm 字体高度 单位毫米
	 * @param align
	 * */
	public void drawText(Canvas canvas,String text,float offsetXmm,float offsetYmm,float sizemm,Align align){
		Paint pText = new Paint();
		pText.setTextSize(getDpmm()*sizemm);
		pText.setTextAlign(align);
		canvas.drawText(text, getDpmm()*offsetXmm, getDpmm()*offsetYmm, pText);
	}
	
	/**
	 * 绘制底部时间线
	 * @param canvas
	 * @param offsetX 横轴坐标  单位 点
	 * @param offsetY 纵轴坐标  单位 点
	 * @param speed 走速
	 */
	public void drawTimeLine(Canvas canvas, float offsetX, float offsetY, float speed) {
		float textPos = offsetX;
		float linebottom = offsetY;
		float linetop = linebottom - 2 * getDpmm();

		Paint pText = new Paint();
		pText.setTextSize(getDpmm() * 3);
		pText.setColor(Color.BLACK);

		Paint pLine = new Paint();
		pLine.setColor(Color.BLACK);

		for (int i = 0; i < 11; i++) {
			canvas.drawText(i + "s", textPos - (1.5f * getDpmm()), linetop, pText);
			canvas.drawLine(textPos, linetop, textPos, linebottom, pLine);
			textPos = (textPos + speed * getDpmm());
		}
		float lineMid = linetop + 1.5F;
		canvas.drawLine(offsetX, lineMid, textPos, lineMid, pLine);

		pText.setTextSize(getDpmm() * 2);
		canvas.drawText("(" + widthMM + "mm * " + heightMM + " mm)  @ (" + mDPI + " dpi)", textPos - 70 * getDpmm(), linebottom + 3 * getDpmm(), pText);
	}
	
	/**
	 * 绘制背景网格
	 * */
	public void drawGrid(Canvas canvas,int left,int top,int width,int height, Paint paint){
		drawGrid(canvas, left,left+ width, top, top+height, getDpmm() ,paint );
	}

	
	
	/**
	 * 绘制心电图
	 * 
	 * @param left 左侧开始位置
	 * @param baseLineY 基线位置
	 * @param ecg 心电数据  单位uv 基线0
	 * @param ecgSampleHz 心电采样率
	 * @param ecgSpeed 走速  mm/s
	 * @param ecgGain 增益   mm/mv
	 * 
	 * */
	public void drawEcgLine(Canvas canvas,int left,int baseLineY,int[] ecg,int ecgSampleHz,float ecgSpeed,float ecgGain, float lineSize,int color,Paint paint){
	
		paint.reset();
		paint.setStyle(Style.FILL);
		paint.setColor(color);
		paint.setStrokeWidth(lineSize);
		float[] pixels = fillData(ecg, ecgSampleHz, left, baseLineY, ecgSpeed, ecgGain);
		canvas.drawLines(pixels, paint);
		
	}
	
	
	private int mDPI = 0;
	private float mDpmm = 0;
	/**
	 * 
	 * 坐标系
	 * ┏━━━>x
	 * ┃
	 * ┃
	 * ﹀
	 * y
	 * 
	 * 单行ecg数据坐标计算
	 * 
	 * @param offsetX 第一个点起始位置
	 * @param offsetY 基线高度
	 * @param dpi 分辨率  dot per inch （同：ppi  pixel per inch）
	 * 
	 * @param ecg 心电数据  数据基线为0，单位为uv 微伏
	 * @param ecgSample 心电数据采样率 hz  赫兹
	 * 
	 * @param ecgSpeed 走速（x轴宽度）mm/s  毫米/秒
	 * @param ecgGain 增益（y轴幅度）mm/mv  毫米/毫伏
	 * @return  返回数据按照 drawLine（float[] data） 格式 每四个值为一组数据，代表一个线段的两个端点数值 x0,y0,x1,y1
	 * 
	 * **/
	public float[] fillData(int[] ecg,int ecgSampleHz,float offsetX, float offsetY,float ecgSpeed,float ecgGain) {
		float dpmm = getDpmm();//dot per mm
		float mXspace = getXspace(dpmm, ecgSpeed, ecgSampleHz);
		float mYspace = getYspace(dpmm, ecgGain);
		int linesSize = ecg.length * 4;
		float[] lines = new float[linesSize];
		float left = offsetX;
		int ponitNum = ecg.length - 1;
		float temp = 0;
		
		for (int index = 0; index < ponitNum; index++) {
			int newindex = index;
			temp = lines[index * 4] = (index) * mXspace + left;
			lines[index * 4 + 1] = (0 - ecg[newindex]) * mYspace + offsetY;
			lines[index * 4 + 2] = temp + mXspace;
			lines[index * 4 + 3] = (0 - ecg[newindex + 1]) * mYspace + offsetY;
		}
		
		return lines;
	}
	
	
	/**
	 * 
	 *  |---|  top
	 *  |   |
	 *  |   |
	 * -|   |- bottom  baseline
	 * 
	 * 12 3 45
	 * ****/
	private void drawStandSquareWave5mm(float right,float baselineY,float standY,float linesize,PointF endPoint,Canvas canvas){
		right-=5;
		float bottom = baselineY;
		float top = baselineY - standY;
		
		float topWidth = 2f;
		float bottomWidth = 1.5f;
		float startX1 = right;
		float startX2 = right+bottomWidth;
		float startX4 = (right+topWidth+bottomWidth);
		float startX5 = (right+bottomWidth+topWidth+bottomWidth);
		int color = Color.BLACK;
		Paint pLine = new Paint();
		pLine.reset();
		pLine.setStyle(Style.FILL);
		pLine.setColor(color);
		pLine.setStrokeWidth(linesize);
//		pLine.setAntiAlias(true);
		drawLine(startX1, bottom, startX2, bottom, linesize, color,canvas,pLine);//1
		drawLine(startX2, bottom, startX2, top, linesize, color,canvas,pLine);//2
		drawLine(startX2, top, startX4, top, linesize, color,canvas,pLine);//3
		drawLine(startX4, bottom, startX4, top, linesize, color,canvas,pLine);//4
		drawLine(startX4, bottom, startX5, bottom, linesize, color,canvas,pLine);//5
		if(endPoint!=null){
			endPoint.set(startX5, bottom);
		}
	}
	
	public void drawLine(float x0, float y0, float x1, float y1, float LineWidth,
			int color,Canvas canvas,Paint paint) {
		
		canvas.drawLine(mm2PixelNums(x0), mm2PixelNums(y0),
				mm2PixelNums(x1), mm2PixelNums(y1), paint);
	}
	
	
	public float mm2PixelNums(float mm){
		return getDpmm()*mm;
	}
	
	public static float getXspace(float dpmm,float ecgSpeed,int ecgSample){
		return (1.0f/ecgSample) * ecgSpeed * dpmm;
	}
	public static float getYspace(float dpmm,float ecgGain){
		return (1 / 1000.0f) * ecgGain * dpmm;
	}
	public static float getDpmm(float dpi){
		float dpmm = dpi/MM_PER_INCH;
		return dpmm;
	}
	
	public float getDpmm(){
		return mDpmm;
	}
	

	
	/**
	 * 绘制网格，单位间隔1mm
	 * 
	 * 考虑到网格在通道画布移动时可以对正绘制基线，所以使用滚动参数
	 * 
	 * 避免屏幕外的网格绘制，过度消耗资源尤其是在幅度放大以后
	 * 
	 * @param channelsWidth
	 *            所有通道宽度
	 * @param channelsHeigth
	 *            所有通道高度
	 * @param panelTop
	 *            相对于网格绘制起点的高度，网格从此高度需要绘制
	 * @param panelBottom
	 *            相对于网格绘制起点的高度，网格从此高度停止绘制
	 * */
	static private	void drawGrid(Canvas canvas, float panelLeft, float panelRight,
			float panelTop, float panelBottom,float DPMM, Paint paint) {
		//网格包含三种线条，外围实线，1mm间隔的点线，5mm间隔的高密点线
		float linePointSize = DPMM * 0.1f;
		int	color = Color.BLACK;
		
		
		Paint paintLongLine = new Paint();
		paintLongLine.setColor(color);
		paintLongLine.setStrokeWidth(linePointSize);
		
		
		float space_x =  DPMM;
		float space_y =  DPMM;
		int line_space = 5;
		
		float offsetX = panelLeft;
		int countNum = 0;
		float width = panelRight - panelLeft;
		float height = panelBottom - panelTop;
		while(offsetX<=panelRight){//竖线
			if(offsetX>0){
				if(countNum%line_space==0){
						drawVerticalPiontLine(canvas, offsetX, panelTop, height, DPMM, 0.5f, paintLongLine);
				}else{
						drawVerticalPiontLine(canvas, offsetX, panelTop, height, DPMM, 1, paintLongLine);
				}
			}
			offsetX+= space_x;
			countNum++;
		}
		
		float offsetY = panelTop;
		countNum = 0;
		while(offsetY<=panelBottom){//横线
			if(offsetY>0){
				if(countNum%line_space==0){
						drawHorizontalPiontLine(canvas, panelLeft, offsetY, width, DPMM, 0.5f, paintLongLine);
				}else{
//					drawHorizontalPiontLine(canvas, panelLeft, offsetY, width, DPMM, 1, paintLongLine);
				}
			}
			offsetY+= space_y;
			countNum++;
		}
		// 为了避免误差，增加四面实线边框
		float lineSize = DPMM * 0.5f;
		paint.reset();
//		paint.setColor(color);
//		paint.setStrokeWidth(lineSize);
//		paint.setStyle(Style.STROKE);
//		canvas.drawRect(panelLeft, panelTop, panelRight, panelBottom, paint);
//		paint.reset();
	}
	
	
	private static void drawHorizontalPiontLine(Canvas canvas, float left,float top, float width,float DPMM,float pointSpaceMm, Paint paint){
		
		float space = DPMM * pointSpaceMm;
		float offsetX = left;
		float right = left+width;
		ArrayList<Float> listX = new ArrayList<Float>();
		while(offsetX<right){
			listX.add(offsetX);
			offsetX+= space;
		}
		int size = listX.size();
		if(size<=0)return;
		float[] pts = new float[size*2];
		for (int i = 0; i < size; i++) {
			int indexX = i*2;
			int indexY = indexX + 1;
			float getX = listX.get(i);
			pts[indexX] = getX;
			pts[indexY] = top;
		}
		canvas.drawPoints(pts, paint);
	}
	
	private static void drawVerticalPiontLine(Canvas canvas, float left,float top, float height,float DPMM,float pointSpaceMm, Paint paint){
		
		float space = DPMM * pointSpaceMm;
		float offsetY = top;
		float bottom = top+height;
		ArrayList<Float> list = new ArrayList<Float>();
		while(offsetY<bottom){
			list.add(offsetY);
			offsetY+= space;
		}
		int size = list.size();
		if(size<=0)return;
		float[] pts = new float[size*2];
		for (int i = 0; i < size; i++) {
			int indexX = i*2;
			int indexY = indexX + 1;
			float get = list.get(i);
			pts[indexX] = left;
			pts[indexY] = get;
		}
		canvas.drawPoints(pts, paint);
	}
	
	
	/**
	 * 演示
	 * */
	public static Bitmap test(int[] ecg,int ecgSampleHz){
		EcgRender render = new EcgRender(150);
		render.measureTime = "2017-05-05 17:36:14";
		render.name = "测试";
		render.hr = "98";
		render.gender = "男";
		render.age = "50";
		
//		A4(210, 297),
		Bitmap bitmap = render.createBitmap(297, 210);
		
		float speed = 25.0f;//  mm/s
		float gain = 10.0f;// mm/mv
		render.drawEcg2Bitmap(bitmap, ecg, ecgSampleHz, speed, gain);
		return bitmap;
	}
}
