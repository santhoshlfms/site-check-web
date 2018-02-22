package com.paypal.sitecheckweb.dto;

import java.util.ArrayList;

public class SiteDto {
	
	
	private String siteName;
	private ArrayList<TagsData> tags;
	private ArrayList<String> chinaWebLinks;

	
	public SiteDto(String siteName, ArrayList<TagsData> tags, ArrayList<String> chinaWebLinks) {
		super();
		this.siteName = siteName;
		this.tags = tags;
		this.setChinaWebLinks(chinaWebLinks);
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


	public ArrayList<String> getChinaWebLinks() {
		return chinaWebLinks;
	}


	public void setChinaWebLinks(ArrayList<String> chinaWebLinks) {
		this.chinaWebLinks = chinaWebLinks;
	}
	
	
	
	
	
	
	
	
	
}
