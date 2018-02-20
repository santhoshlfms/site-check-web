package com.paypal.sitecheckweb.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.coyote.http2.ConnectionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.sitecheckweb.dto.InputDataDto;
import com.paypal.sitecheckweb.dto.ResultsDto;
import com.paypal.sitecheckweb.dto.SiteDto;
import com.paypal.sitecheckweb.dto.TagsData;



@RestController
public class ReadandProcessUrlsControllers {

	private String outputPath = "C:\\Users\\sannelson\\Downloads\\site-check-web\\site-check-web\\src\\main\\resources\\output\\output.csv";
	
	@RequestMapping(value="/read-urls", method=RequestMethod.POST)
	public ResultsDto readUrls(@RequestBody InputDataDto data, HttpServletResponse response) throws IOException, ConnectionException {
		String url = data.getUrls();
		List<String> tagList = Arrays.asList(data.getTags().split(",[ ]*"));
		ResultsDto result = crawlData(url, tagList);
		return result;
	}
	
	@RequestMapping(value="/download-csv", method=RequestMethod.POST)
	public ResponseEntity<Resource> download() throws IOException {
		File file = new File(outputPath);
		Path path = Paths.get(file.getAbsolutePath());
	    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
	    HttpHeaders headers = new HttpHeaders(); 
	    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.csv");
	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(file.length())
	            .contentType(MediaType.parseMediaType("application/octet-stream"))
	            .body(resource);
	}
	
	
	public ResultsDto crawlData(String urlList, List<String> tagList) throws IOException {
		Document doc = null;
		List<String> listOfInvalidUrl = new ArrayList<>();
	
		HashMap<String, ArrayList<TagsData>> csvData = new HashMap<String, ArrayList<TagsData>>(); 
	
		//for(String url : urlList) {
			//1. check if url has protocol, if not add http:// as default
			String urlWithProtocol = checkForProtocol(urlList.trim());
			//System.out.println("1 :" +urlWithProtocol);
			//2. connect via jsoup and crawl data
			try {
				doc = Jsoup.connect(urlWithProtocol).timeout(10 * 2000).userAgent("Mozilla").get();
				//3. url is valid and connected with jsoup now find the tags in dom
				Elements findPayPalKeyWord = doc.select("body");
				String convertedNodes = findPayPalKeyWord.toString();
				convertedNodes = convertedNodes.toLowerCase();
				
				//4. finding if the above string has mentioned tags !
				ArrayList<TagsData> tagsData = new ArrayList<>();
				for(String tag: tagList) {
					tag = tag.toLowerCase();
					TagsData tagdata = new TagsData(tag, convertedNodes.indexOf(tag) > -1);
					tagsData.add(tagdata);
				}
				
				csvData.put(urlWithProtocol, tagsData);
				System.out.println(urlWithProtocol + " accessed");
				
			} catch (Exception e) {
				listOfInvalidUrl.add(urlWithProtocol);
				System.out.println( urlWithProtocol + " Not accessed == " + e.getMessage());
				//e.printStackTrace();
			}
		//}
		//5. write the hashmap to csv file and download it --- Writing valid URL's First 
	
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath)); 
		// header for valid urls
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("SITE URL", "TAG NAME", "IS TAG FOUND"));
		ArrayList<SiteDto> listSiteDto = new ArrayList<>();
		for(Map.Entry item : csvData.entrySet()) {
			String url = (String) item.getKey();
			@SuppressWarnings("unchecked")
			ArrayList<TagsData> tagsData = (ArrayList<TagsData>) item.getValue();
	        SiteDto siteDto = new SiteDto(url, tagsData);
	        listSiteDto.add(siteDto);
			//writeToCsv(writer, csvPrinter, url, " ", " ");
			if(tagsData.size() > 0) {
				for(TagsData tag: tagsData) {
					//writeToCsv(writer, csvPrinter, "", tag.getTagName(), tag.value());
				}
			}
			
		}
		//csvPrinter.flush();            
        ResultsDto dto = new ResultsDto(listOfInvalidUrl, listSiteDto);
        return dto;
		
	}
	
	private void writeToCsv(BufferedWriter writer, CSVPrinter csvPrinter, String url, String tagName, String tagValue) throws IOException {
		csvPrinter.printRecord(url, tagName, tagValue);
	}
	
	private String checkForProtocol(String url) {
		//Regex to validate if the url has protocol i.e http/https/ftp
		 String regexProtocol = "^(http|https|ftp)://.*$";
		 if(url.matches(regexProtocol)) {
			 return url;
		 }else {
			 return "https://"+url;
		 }
	}
	
}
