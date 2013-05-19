/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class CsvEvaluationExample {

	static
	public void main(String[] args) throws Exception {

		if(args.length < 1 || args.length > 3){
			System.err.println("Usage: java " + CsvEvaluationExample.class.getName() + " <PMML file> <CSV input file>? <CSV output file>?");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		File inputFile = (args.length > 1 ? new File(args[1]) : null);
		File outputFile = (args.length > 2 ? new File(args[2]) : null);

		evaluate(pmml, inputFile, outputFile);
	}

	@SuppressWarnings (
		value = {"unused"}
	)
	static
	private void evaluate(PMML pmml, File inputFile, File outputFile) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

		CsvUtil.Table table = CsvUtil.readTable(inputFile);

		List<FieldName> inputFields = new ArrayList<FieldName>();

		header:
		{
			List<String> headerRow = table.get(0);

			for(int i = 0; i < headerRow.size(); i++){
				String headerCell = headerRow.get(i);

				FieldName inputField = new FieldName(headerCell);

				// Check that the column is present in PMML data dictionary
				DataField dataField = evaluator.getDataField(inputField);
				if(dataField != null){

					if(!activeFields.contains(inputField)){
						System.err.println("Not an active field: " + inputField.getValue());

						if(predictedFields.contains(inputField) || outputFields.contains(inputField)){
							inputField = null;
						}
					}
				} else

				{
					inputField = null;
				}

				inputFields.add(inputField);
			}

			for(FieldName predictedField : predictedFields){
				headerRow.add(predictedField.getValue());
			}

			for(FieldName outputField : outputFields){
				headerRow.add(outputField.getValue());
			}
		}

		body:
		for(int line = 1; line < table.size(); line++){
			List<String> bodyRow = table.get(line);

			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

			for(int i = 0; i < inputFields.size(); i++){
				String bodyCell = bodyRow.get(i);

				if(CsvUtil.isMissing(bodyCell)){
					bodyCell = null;
				}

				FieldName inputField = inputFields.get(i);
				if(inputField == null){
					continue;
				}

				parameters.put(inputField, evaluator.prepare(inputField, bodyCell));
			}

			Map<FieldName, ?> result = evaluator.evaluate(parameters);

			for(FieldName predictedField : predictedFields){
				Object predictedValue = EvaluatorUtil.decode(result.get(predictedField));

				bodyRow.add(String.valueOf(predictedValue));
			}

			for(FieldName outputField : outputFields){
				Object outputValue = EvaluatorUtil.decode(result.get(outputField));

				bodyRow.add(String.valueOf(outputValue));
			}
		}

		CsvUtil.writeTable(table, outputFile);
	}
}