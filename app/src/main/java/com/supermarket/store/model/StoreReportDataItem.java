package com.supermarket.store.model;

import com.google.gson.annotations.SerializedName;

public class StoreReportDataItem{

	@SerializedName("imgurl")
	private Object imgurl;

	@SerializedName("report_data")
	private String reportData;

	@SerializedName("title")
	private String title;

	public Object getImgurl(){
		return imgurl;
	}

	public String getReportData(){
		return reportData;
	}

	public String getTitle(){
		return title;
	}
}