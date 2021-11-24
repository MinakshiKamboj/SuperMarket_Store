package com.supermarket.store.model;

import com.google.gson.annotations.SerializedName;

public class CatlistItem{

	@SerializedName("catname")
	private String catname;

	@SerializedName("id")
	private String id;

	public String getCatname(){
		return catname;
	}

	public String getId(){
		return id;
	}
}