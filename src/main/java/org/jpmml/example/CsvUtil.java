/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.example;

import java.util.*;

public class CsvUtil {

	private CsvUtil(){
	}

	static
	public List<String> parseLine(String line, String separator){
		List<String> result = new ArrayList<String>();

		String[] cells = line.split(separator);
		for(String cell : cells){

			// Remove quotation marks, if any
			cell = stripQuotes(cell, "\"");
			cell = stripQuotes(cell, "\'");

			// Standardize decimal marks to Full Stop (US)
			if(!(",").equals(separator)){
				cell = cell.replace(',', '.');
			}

			result.add(cell);
		}

		return result;
	}

	static
	private String stripQuotes(String string, String quote){

		if(string.startsWith(quote) && string.endsWith(quote)){
			string = string.substring(quote.length(), string.length() - quote.length());
		}

		return string;
	}

	static
	public boolean isMissing(String string){
		return string == null || ("").equals(string);
	}
}