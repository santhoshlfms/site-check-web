package com.paypal.sitecheckweb.dto;

public class TagsData {
	
	private String tagName;
	private boolean isPresent;
	
	
	
	public TagsData(String tagName, boolean isPresent) {
		super();
		this.tagName = tagName;
		this.isPresent = isPresent;
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
	
}
