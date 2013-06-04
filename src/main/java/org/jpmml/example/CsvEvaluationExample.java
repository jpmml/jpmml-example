/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.beust.jcommander.Parameter;

public class CsvEvaluationExample extends Example {

	@Parameter (
		names = {"--model"},
		description = "The PMML file",
		required = true
	)
	private File model = null;

	@Parameter (
		names = {"--input"},
		description = "Input CSV file. If missing, data records will be read from System.in"
	)
	private File input = null;

	@Parameter (
		names = {"--output"},
		description = "Output CSV file. If missing, data records will be written to System.out"
	)
	private File output = null;

	@Parameter (
		names = {"--separator"},
		description = "CSV cell separator character"
	)
	private String separator = null;


	static
	public void main(String... args) throws Exception {
		execute(CsvEvaluationExample.class, args);
	}

	@Override
	@SuppressWarnings (
		value = {"unused"}
	)
	public void execute() throws Exception {
		PMML pmml = IOUtil.unmarshal(this.model);

		PMMLManager pmmlManager = new PMMLManager(pmml);

		ModelManager<?> modelManager = pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		Evaluator evaluator = (Evaluator)modelManager;

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

		CsvUtil.Table table = CsvUtil.readTable(this.input, this.separator);

		List<Map<FieldName, Object>> rows = prepare(table, evaluator);

		header:
		{
			List<String> headerRow = table.get(0);

			for(FieldName predictedField : predictedFields){
				headerRow.add(predictedField.getValue());
			}

			for(FieldName outputField : outputFields){
				headerRow.add(outputField.getValue());
			}
		}

		long start = System.currentTimeMillis();

		body:
		for(int line = 1; line < table.size(); line++){
			List<String> bodyRow = table.get(line);

			Map<FieldName, ?> parameters = rows.get(line - 1);

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

		long end = System.currentTimeMillis();

		System.err.println("Evaluation completed in " + (end - start) + " ms.");

		CsvUtil.writeTable(table, this.output);
	}

	@SuppressWarnings (
		value = {"unused"}
	)
	static
	private List<Map<FieldName, Object>> prepare(List<List<String>> table, Evaluator evaluator){
		List<Map<FieldName, Object>> result = new ArrayList<Map<FieldName, Object>>();

		List<FieldName> inputFields = new ArrayList<FieldName>();

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

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
		}

		body:
		for(int line = 1; line < table.size(); line++){
			List<String> bodyRow = table.get(line);

			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

			for(int i = 0; i < inputFields.size(); i++){
				String bodyCell = bodyRow.get(i);

				FieldName inputField = inputFields.get(i);
				if(inputField == null){
					continue;
				}

				if(CsvUtil.isMissing(bodyCell)){
					bodyCell = null;
				}

				parameters.put(inputField, evaluator.prepare(inputField, bodyCell));
			}

			result.add(parameters);
		}

		return result;
	}
}