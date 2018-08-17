package com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

@JsonObject
public class HistoryItemBean {
	@JsonField
	private String codeImgPath = "";
	@JsonField

	private String fileName = "";
	@JsonField
	private ArrayList<String> codeFormat = new ArrayList<>();
	@JsonField
	private ArrayList<String> codeText = new ArrayList<>();
	@JsonField
	private ArrayList<RectPoint[]> rectCoord = new ArrayList<>();
	@JsonField
	private long decodeTime = 0;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getDecodeTime() {
		return decodeTime;
	}

	public void setDecodeTime(long decodeTime) {
		this.decodeTime = decodeTime;
	}

	public ArrayList<RectPoint[]> getRectCoord() {
		return rectCoord;
	}

	public void setRectCoord(ArrayList<RectPoint[]> rectCoord) {
		this.rectCoord = rectCoord;
	}

	public String getCodeImgPath() {
		return codeImgPath;
	}

	public void setCodeImgPath(String codeImgPath) {
		this.codeImgPath = codeImgPath;
	}

	public ArrayList<String> getCodeFormat() {
		return codeFormat;
	}

	public void setCodeFormat(ArrayList<String> codeFormat) {
		this.codeFormat = codeFormat;
	}

	public ArrayList<String> getCodeText() {
		return codeText;
	}

	public void setCodeText(ArrayList<String> codeText) {
		this.codeText = codeText;
	}
}
