package com.paypal.sitecheckweb;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.paypal.sitecheckweb.controllers.ReadandProcessUrlsControllers;
import com.paypal.sitecheckweb.dto.ResultsDto;
import com.paypal.sitecheckweb.dto.SiteDto;
import com.paypal.sitecheckweb.dto.TagsData;

public class ReadandProcessUrlsControllersTest {
	
	private ReadandProcessUrlsControllers controller = new ReadandProcessUrlsControllers();
	private List<String> tags;
	
	@Before
	public void setUp() {
		tags = new ArrayList<String>();
		tags.add("paypal");
		tags.add("alipay");
		tags.add("chaina");
	}
	
	@Test
	public void crawlData() throws Exception {
		String url = "http://oneness287.com";
		ResultsDto result = controller.crawlData(url, tags);
		assertTrue(result.getListOfInvalidUrl().isEmpty());
		SiteDto siteDto = result.getSiteDto().get(0);
		assertEquals(siteDto.getSiteName(), url);
		assertTrue(found(siteDto.getTags(), "paypal"));
		assertFalse(found(siteDto.getTags(), "alipay"));
		assertFalse(found(siteDto.getTags(), "chaina"));
	}

	private boolean found(List<TagsData> tags, String tag) {
		for (TagsData tagsData : tags) {
			if(tagsData.getTagName().equals(tag)) {
				return tagsData.isPresent();
			}
		}
		return false;
	}
	

}
