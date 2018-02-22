package com.paypal.sitecheckweb.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.qos.logback.core.boolex.Matcher;

public class Test {
	 private HashSet<String> links;

	    public Test() {
	        links = new HashSet<String>();
	    }

	    public void getPageLinks(String URL) {
	        //4. Check if you have already crawled the URLs
	        //(we are intentionally not checking for duplicate content in this example)
	        if (!links.contains(URL)) {
	            try {
	                //4. (i) If not add it to the index
	                if (links.add(URL)) {
	                    System.out.println(URL);
	                }

	                //2. Fetch the HTML code
	                Document document = Jsoup.connect(URL).get();
	                //3. Parse the HTML to extract links to other URLs
	                Elements linksOnPage = document.select("a[href]");

	                //5. For each extracted URL... go back to Step 4.
	                for (Element page : linksOnPage) {
	                    getPageLinks(page.attr("abs:href"));
	                }
	            } catch (IOException e) {
	                System.err.println("For '" + URL + "': " + e.getMessage());
	            }
	        }
	    }

	    public static void main(String[] args) throws URISyntaxException, IOException {
	        //1. Pick a URL from the frontier
	    	URI uri = new URI("https://www.featuresneakerboutique.com/collections/chapter");
	    	String url = "https://www.featuresneakerboutique.com/";
	    	String i = "https://npmjs.com/package/@opskins/api";
	    	String n = uri.getHost();
	    	n = n.startsWith("www.") ? n.substring(4) : n;
	    	System.out.println(n);
	    	System.out.println(StringUtils.ordinalIndexOf(url, "/", 4));
	    	System.out.println(url.substring(0, 50));
	    	/*
	    	 Document doc =Jsoup.connect("https://opskins.com/kb/faq").timeout(10 * 2000).userAgent("Mozilla").get();
	    	 Elements scriptElements = doc.select("script");

			 ArrayList<String> gList = new ArrayList(Arrays.asList("ga","dc","analytics","gtag","ga_exp","gtm","conversion","loader"));
			 int counter = 0;
			 for(String g : gList) {
				 for(Element link : scriptElements) {
					 System.out.println(link); 
					 if(link.toString().contains(g)) {
						 System.out.println(link);
						 System.out.println("------------------- :" + g);
					 }
					  
				 }
			 }*/


	    	
	    	
	        //new Test().getPageLinks("http://www.paypal.com/");
	    }
}
