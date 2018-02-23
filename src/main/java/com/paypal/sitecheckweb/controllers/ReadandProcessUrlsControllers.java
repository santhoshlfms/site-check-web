package com.paypal.sitecheckweb.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
		List<String> tagList = new ArrayList<String>(Arrays.asList(data.getTags().split(",[ ]*")));
		ResultsDto result = crawlData(url, tagList);
		log.info("RESPONSE SENT");
		return result;
	}

	public ResultsDto crawlData(String inputUrl, List<String> tags) throws IOException, URISyntaxException {

		SiteDto siteDto = new SiteDto(inputUrl);
		HashSet<String> links = new HashSet<>();

		String urlWithProtocol = checkForProtocol(inputUrl.trim());
		log.info("URL :" + urlWithProtocol);

		links = getPageLinks(urlWithProtocol, 0, urlWithProtocol, links);
		if (null == links) {
			return new ResultsDto(urlWithProtocol);
		}
		log.info("VALID URLS :" + links.size());

		siteDto.setChinaWebLinks(findChineseWebsites(links));
		siteDto.setTags(crawlLinksByTags(tags, links));
		return new ResultsDto(siteDto);

	}

	private ArrayList<TagsData> crawlLinksByTags(List<String> tags, HashSet<String> links)
			throws URISyntaxException, IOException {

		boolean googleLinkFound = false;
		ArrayList<TagsData> tagsData = new ArrayList<>();

		Iterator<String> it = links.iterator();
		while (it.hasNext()) {
			if (tags.isEmpty() && googleLinkFound) {
				break;
			}
			String url = it.next();
			googleLinkFound = crawlLinkByTags(tags, tagsData, url, googleLinkFound);
		}

		processTagsNotFound(tags, tagsData);

		return tagsData;

	}

	private boolean crawlLinkByTags(List<String> tags, ArrayList<TagsData> tagsData, String url,
			boolean googleLinkFound) {
		try {
			Document doc = Jsoup.connect(url).timeout(10 * 2000).userAgent("Mozilla").get();

			if (!googleLinkFound) {
				googleLinkFound = findGoogleLink(tagsData, url, doc);
			}

			if (!tags.isEmpty()) {
				List<String> tagsFound = findTags(tags, tagsData, url, doc);
				tags.removeAll(tagsFound);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return googleLinkFound;
	}

	private void processTagsNotFound(List<String> tags, ArrayList<TagsData> tagsData) {
		if (!tags.isEmpty()) {
			for (String tag : tags) {
				TagsData tagdata = new TagsData(tag, false, "");
				tagsData.add(tagdata);
			}
		}
	}

	private List<String> findTags(List<String> tags, ArrayList<TagsData> tagsData, String url, Document doc) {
		List<String> tagsFound = new ArrayList<String>();
		String body = doc.select("body").toString().toLowerCase();

		for (String tag : tags) {
			tag = tag.toLowerCase();
			boolean isFound = body.indexOf(tag) > -1;
			if (isFound) {
				log.info(" ** Found tag - " + tag + " in :" + url);
				tagsFound.add(tag);
				TagsData tagdata = new TagsData(tag, isFound, url);
				tagsData.add(tagdata);

			} else {
				log.info("Not able to find " + tag + " in :" + url);
			}

		}

		return tagsFound;
	}

	private boolean findGoogleLink(ArrayList<TagsData> tagsData, String url, Document doc) {
		Elements scripts = doc.select("script");
		String googleLink = getAnalyticsLink(url, scripts);
		if (null != googleLink) {
			TagsData tagdata = new TagsData("Google API", true, googleLink);
			tagsData.add(tagdata);
			return true;
		}
		return false;
	}

	private ArrayList<String> findChineseWebsites(HashSet<String> links) {
		ArrayList<String> chinaWebSite = new ArrayList<>();
		Iterator<String> iterator = links.iterator();
		while (iterator.hasNext()) {
			String URL = iterator.next();
			if (URL != null) {
				try {
					URI uri = new URI(URL);
					String hostName = uri.getHost();
					if (hostName != null) {
						hostName = hostName.startsWith("www.") ? hostName.substring(4) : hostName;
						if (URL.toLowerCase().contains(".zh") || URL.toLowerCase().contains(".ch")
								|| URL.toLowerCase().contains("zh.") || URL.toLowerCase().contains("ch.")) {
							chinaWebSite.add(URL);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return chinaWebSite;
	}

	private HashSet<String> getPageLinks(String URL, int depth, String parentUrl, HashSet<String> links) {
		if (URL.length() > 0) {
			int indexOfParam = StringUtils.ordinalIndexOf(URL, "/", 4);
			if (indexOfParam > 0) {
				URL = URL.substring(0, indexOfParam);
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
						Document document = Jsoup.connect(URL).timeout(20 * 1000).userAgent("Mozilla").get();
						Elements linksOnPage = document.select("a[href]");

						depth++;
						for (Element page : linksOnPage) {
							if (links.size() < 20) {
								getPageLinks(page.attr("abs:href"), depth, parentUrl, links);
							}

						}
					}

				} catch (Exception e) {
					log.error("For '" + URL + "': " + e.getMessage());
					if (depth == 0) {
						return null;
					}
				}
			}
		}
		return links;
	}

	private String getAnalyticsLink(String url, Elements scripts) {
		for (Element link : scripts) {
			if (link.toString().contains("google")) {
				return link.absUrl("src");
			}
		}
		return null;
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

	private String processURL(String theURL) {
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

}
