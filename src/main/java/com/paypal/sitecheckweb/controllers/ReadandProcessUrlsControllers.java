package com.paypal.sitecheckweb.controllers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http2.ConnectionException;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

	private static final Logger log = Logger.getLogger(ReadandProcessUrlsControllers.class);
	// max level of depth to cwarl
	int MAX_DEPTH = 2;

	@RequestMapping(value = "/read-urls", method = RequestMethod.POST)
	public ResultsDto readUrls(@RequestBody InputDataDto data, HttpServletResponse response)
			throws IOException, ConnectionException, URISyntaxException {
		String url = data.getUrls();
		List<String> tagList = new ArrayList(Arrays.asList(data.getTags().split(",[ ]*")));
		ResultsDto result = crawlData(url, tagList);
		return result;
	}

	public ResultsDto crawlData(String urlList, List<String> tagList) throws IOException, URISyntaxException {

		Document doc = null;
		List<String> listOfInvalidUrl = new ArrayList<>();

		HashMap<String, ArrayList<TagsData>> csvData = new HashMap<String, ArrayList<TagsData>>();

		// 1. check if url has protocol, if not add http:// as default
		String urlWithProtocol = checkForProtocol(urlList.trim());
		// get all links from the site
		ArrayList<String> googleLinks = null;
		HashSet<String> totalValidLinks = new HashSet<>();
		HashSet<String> links = getPageLinks(urlWithProtocol, 0, urlWithProtocol, totalValidLinks);

		ArrayList<String> chinaWebSite = findChineseWebsites(links);
		
		log.info("URL :" + urlWithProtocol);
		log.info("VALID URLS :" + links.size());
		
		ArrayList<TagsData> tagsData = new ArrayList<>();
		String url = null;

		// 2. connect via jsoup and crawl data
		try {
			Iterator<String> it = links.iterator();
			while (it.hasNext()) {
				if (tagList.size() == 0) {
					break;
				}
				url = it.next();
				doc = Jsoup.connect(url).timeout(10 * 2000).userAgent("Mozilla").get();
				// 3. url is valid and connected with jsoup now find the tags in dom
				Elements findPayPalKeyWord = doc.select("body");
				Elements linksOnPage = doc.select("script");
				// 4. find google analytics
				googleLinks = checkForAnalytics(url, linksOnPage);

				String convertedNodes = findPayPalKeyWord.toString();
				convertedNodes = convertedNodes.toLowerCase();

				// 4. finding if the above string has mentioned tags !
				List<String> itemToRemove = new ArrayList<String>();

				for (String tag : tagList) {
					tag = tag.toLowerCase();
					boolean isFound = convertedNodes.indexOf(tag) > -1;
					if (isFound) {
						log.info(" ** Found tag - " + tag + " in :" + url);
						itemToRemove.add(tag);
						TagsData tagdata = new TagsData(tag, isFound, url);
						tagsData.add(tagdata);

					} else {
						log.info("Not able to find " + tag + " in :" + url);
					}

				}

				tagList.removeAll(itemToRemove);
			}

			if (tagList.size() > 0) {
				for (String tag : tagList) {
					TagsData tagdata = new TagsData(tag, false, "");
					tagsData.add(tagdata);
				}

			}

			if (googleLinks.size() > 0) {
				TagsData tagdata = new TagsData("Google API", true, Arrays.toString(googleLinks.toArray()));
				tagsData.add(tagdata);

			}

			csvData.put(urlWithProtocol, tagsData);

		} catch (Exception e) {
			if (urlWithProtocol.equals(url)) {
				listOfInvalidUrl.add(urlWithProtocol);
			}

			// e.printStackTrace();
			log.info(urlWithProtocol + " Not accessed == " + e.getMessage());
			// e.printStackTrace();
		}

		ArrayList<SiteDto> listSiteDto = new ArrayList<>();
		for (Map.Entry item : csvData.entrySet()) {
			String _url = (String) item.getKey();
			@SuppressWarnings("unchecked")
			ArrayList<TagsData> foundtagsData = (ArrayList<TagsData>) item.getValue();
			SiteDto siteDto = new SiteDto(_url, foundtagsData, chinaWebSite);
			listSiteDto.add(siteDto);

		}
		// csvPrinter.flush();
		ResultsDto dto = new ResultsDto(listOfInvalidUrl, listSiteDto);
		return dto;

	}

	private void writeToCsv(BufferedWriter writer, CSVPrinter csvPrinter, String url, String tagName, String tagValue)
			throws IOException {
		csvPrinter.printRecord(url, tagName, tagValue);
	}

	public ArrayList<String> findChineseWebsites(HashSet<String> links) throws URISyntaxException {
		ArrayList<String> chinaWebSite = new ArrayList<>();
		Iterator<String> iterator = links.iterator();
		while (iterator.hasNext()) {
			String URL = iterator.next();
			if(URL!=null) {
				URI uri = new URI(URL);
				String hostName = uri.getHost();
				hostName = hostName.startsWith("www.") ? hostName.substring(4) : hostName;
				if (URL.toLowerCase().contains(".zh") || URL.toLowerCase().contains(".ch")
						|| URL.toLowerCase().contains("zh.") || URL.toLowerCase().contains("ch.")) {
					chinaWebSite.add(URL);
				}
			}
		}
		return chinaWebSite;
	}

	public HashSet<String> getPageLinks(String URL, int depth, String parentUrl, HashSet<String> links) {
		if (URL.length() > 0) {
			int indexOfParam = StringUtils.ordinalIndexOf(URL, "/", 4);
			if(indexOfParam > 0 ) {
				URL = URL.substring(0,indexOfParam);
			}
		
			URI uri = null;
			try {
				URL = processURL(URL);
				uri = new URI(parentUrl);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
			String hostName = uri.getHost();
			hostName = hostName.startsWith("www.") ? hostName.substring(4) : hostName;

			if ((!links.contains(URL) && (depth < MAX_DEPTH)) && URL.contains(hostName)) {
				// System.out.println(">> Depth: " + depth + " [" + URL + "]");

				try {
						links.add(URL);
						if (URL != null && URL.length() > 0) {
							Document document = Jsoup.connect(URL).timeout(10 * 2000).userAgent("Mozilla").get();
							Elements linksOnPage = document.select("a[href]");
	
							depth++;
							for (Element page : linksOnPage) {
								if(links.size() < 20) {
									getPageLinks(page.attr("abs:href"), depth, parentUrl, links);
								}
									
							}
						}

				} catch (Exception e) {
					log.error("For '" + URL + "': " + e.getMessage());
				}
			}
		}
		return links;
	}

	public ArrayList<String> checkForAnalytics(String scrawlerLink, Elements linksOnPage) {
		ArrayList<String> googleLinks = new ArrayList<>();
		for (Element link : linksOnPage) {
			if (link.toString().contains("google")) {
				googleLinks.add(scrawlerLink);
				log.error("FOUND GA : " + scrawlerLink);
				break;
			}
		}
		return googleLinks;
	}

	private String checkForProtocol(String url) {
		// Regex to validate if the url has protocol i.e http/https/ftp
		String regexProtocol = "^(http|https|ftp)://.*$";
		if (url.matches(regexProtocol)) {
			return url;
		} else {
			return "https://" + url;
		}
	}

	public String processURL(String theURL) {
		int endPos;
		if (theURL.indexOf("?") > 0) {
			endPos = theURL.indexOf("?");
		} else if (theURL.indexOf("#") > 0) {
			endPos = theURL.indexOf("#");
		} else {
			endPos = theURL.length();
		}
		return theURL.substring(0, endPos);
	}

	/*
	 * @RequestMapping(value="/download-csv", method=RequestMethod.POST) public
	 * ResponseEntity<Resource> download() throws IOException { File file = new
	 * File(outputPath); Path path = Paths.get(file.getAbsolutePath());
	 * ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
	 * HttpHeaders headers = new HttpHeaders();
	 * headers.add(HttpHeaders.CONTENT_DISPOSITION,
	 * "attachment; filename=output.csv"); return ResponseEntity.ok()
	 * .headers(headers) .contentLength(file.length())
	 * .contentType(MediaType.parseMediaType("application/octet-stream"))
	 * .body(resource); }
	 */

}
