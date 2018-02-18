package com.paypal.sitecheckweb.utils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.validator.routines.UrlValidator;

public class Test {
	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		UrlValidator url  = new UrlValidator();
		System.out.println(url.isValid("http://papal.com"));
		String regex = "^(http|https|ftp)://.*$";
		System.out.println("http://papal.com".matches(regex));
		//System.out.println(u.toURI());
	}
}
