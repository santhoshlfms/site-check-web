package com.paypal.sitecheckweb.dto;

public class TagsData {
	
	private String tagName;
	private boolean isPresent;
	private String url;
	
	
	
	public TagsData(String tagName, boolean isPresent, String url) {
		super();
		this.tagName = tagName;
		this.isPresent = isPresent;
		this.url = url;
	}
	
	
	
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public boolean isPresent() {
		return isPresent;
	}
	public void setPresent(boolean isPresent) {
		this.isPresent = isPresent;
	}
	
	public String value() {
		return this.isPresent ? tagName+ " is present" : tagName+" is not present";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
