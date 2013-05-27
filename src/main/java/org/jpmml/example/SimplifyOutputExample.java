/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class SimplifyOutputExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 2){
			System.err.println("Usage: java " + SimplifyOutputExample.class.getName() + " <Source file> <Destination file>");

			System.exit(-1);
		}

		File srcFile = new File(args[0]);
		File destFile = new File(args[1]);

		PMML pmml = IOUtil.unmarshal(srcFile);

		simplify(pmml);

		IOUtil.marshal(pmml, destFile);
	}

	static
	private void simplify(PMML pmml){
		PMMLManager pmmlManager = new PMMLManager(pmml);

		Model model = pmmlManager.getModel(null);

		Output output = model.getOutput();
		if(output != null){
			simplify(output);
		}
	}

	/**
	 * @see OutputUtil
	 */
	static
	private void simplify(Output output){
		Collection<OutputField> outputFields = output.getOutputFields();

		Iterator<OutputField> it = outputFields.iterator();
		while(it.hasNext()){
			OutputField outputField = it.next();

			ResultFeatureType resultFeature = outputField.getFeature();
			switch(resultFeature){
				case PREDICTED_VALUE:
				case TRANSFORMED_VALUE:
				case PROBABILITY:
					break;
				default:
					it.remove();
					break;
			}
		}
	}
}