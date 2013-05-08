/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.example;

public class EvaluationUtil {

	private EvaluationUtil(){
	}

	static
	public boolean isMissing(String string){
		return string == null || ("").equals(string);
	}
}