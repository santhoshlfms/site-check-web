package com.paypal.sitecheckweb.dto;

public class GoogleAnalticsDto {
	
	private String url;
	private String analticsUrl;
	
	
	
	public GoogleAnalticsDto(String url, String analticsUrl) {
		super();
		this.url = url;
		this.analticsUrl = analticsUrl;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAnalticsUrl() {
		return analticsUrl;
	}
	public void setAnalticsUrl(String analticsUrl) {
		this.analticsUrl = analticsUrl;
	}
	
	

}
