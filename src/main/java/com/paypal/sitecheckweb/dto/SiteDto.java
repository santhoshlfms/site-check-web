package com.paypal.sitecheckweb.dto;

import java.util.ArrayList;

public class SiteDto {
	
	
	private String siteName;
	private ArrayList<TagsData> tags;
	
	
	public SiteDto(String siteName, ArrayList<TagsData> tags) {
		super();
		this.siteName = siteName;
		this.tags = tags;
	}
	
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public ArrayList<TagsData> getTags() {
		return tags;
	}
	public void setTags(ArrayList<TagsData> tags) {
		this.tags = tags;
	}
	
	
	
	
	
	
	
	
	
}
