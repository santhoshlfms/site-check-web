package com.paypal.sitecheckweb.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultsDto {
	
	private List<String> listOfInvalidUrl;
	private List<SiteDto> siteDto;
	
	
	
	public ResultsDto(List<String> listOfInvalidUrl, List<SiteDto> siteDto) {
		super();
		this.listOfInvalidUrl = listOfInvalidUrl;
		this.siteDto = siteDto;
	}
	
	public ResultsDto(String invalidUrl) {
		this.listOfInvalidUrl = Arrays.asList(invalidUrl);
		this.siteDto = new ArrayList<>();
	}
	
	public ResultsDto( SiteDto siteDto) {
		this.listOfInvalidUrl = new ArrayList<>();
		this.siteDto = Arrays.asList(siteDto);
	}
	
	public List<String> getListOfInvalidUrl() {
		return listOfInvalidUrl;
	}
	public void setListOfInvalidUrl(List<String> listOfInvalidUrl) {
		this.listOfInvalidUrl = listOfInvalidUrl;
	}
	public List<SiteDto> getSiteDto() {
		return siteDto;
	}
	public void setSiteDto(List<SiteDto> siteDto) {
		this.siteDto = siteDto;
	}
	
		
	
}
