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

		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

		CsvUtil.Table table = CsvUtil.readTable(this.input, this.separator);

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

		CsvUtil.writeTable(table, this.output);
	}
}