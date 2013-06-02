/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.beust.jcommander.Parameter;

public class SimplifyOutputExample extends Example {

	@Parameter (
		names = {"--input"},
		description = "Input PMML file",
		required = true
	)
	private File input = null;

	@Parameter (
		names = {"--output"},
		description = "Output PMML file",
		required = true
	)
	private File output = null;


	static
	public void main(String... args) throws Exception {
		execute(SimplifyOutputExample.class, args);
	}

	@Override
	public void execute() throws Exception {
		PMML pmml = IOUtil.unmarshal(this.input);

		simplify(pmml);

		IOUtil.marshal(pmml, this.output);
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