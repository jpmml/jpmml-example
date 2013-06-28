/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class TreeModelManagerExample extends Example {

	static
	public void main(String... args) throws Exception {
		execute(TreeModelManagerExample.class, args);
	}

	@Override
	public void execute() throws Exception {
		PMML pmml = createGolfingModel();

		TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(pmml);

		Map<FieldName, ?> parameters = EvaluationExample.readParameters(treeModelEvaluator);

		Map<FieldName, ?> result = treeModelEvaluator.evaluate(parameters);

		System.out.println(result.get(treeModelEvaluator.getTarget()));
	}

	static
	private PMML createGolfingModel(){
		TreeModelManager treeModelManager = new TreeModelManager();

		TreeModel treeModel = treeModelManager.createClassificationModel();
		treeModel.setModelName("golfing");

		FieldName temperature = new FieldName("temperature");
		treeModelManager.addField(temperature, null, OpType.CONTINUOUS, DataType.DOUBLE, null);

		FieldName humidity = new FieldName("humidity");
		treeModelManager.addField(humidity, null, OpType.CONTINUOUS, DataType.DOUBLE, null);

		FieldName windy = new FieldName("windy");
		treeModelManager.addField(windy, null, OpType.CATEGORICAL, DataType.STRING, null);

		DataField windyData = treeModelManager.getDataField(windy);
		(windyData.getValues()).addAll(createValues("true", "false"));

		FieldName outlook = new FieldName("outlook");
		treeModelManager.addField(outlook, null, OpType.CATEGORICAL, DataType.STRING, null);

		DataField outlookData = treeModelManager.getDataField(outlook);
		(outlookData.getValues()).addAll(createValues("sunny", "overcast", "rain"));

		FieldName whatIdo = new FieldName("whatIdo");
		treeModelManager.addField(whatIdo, null, OpType.CATEGORICAL, DataType.STRING, FieldUsageType.PREDICTED);

		DataField whatIdoData = treeModelManager.getDataField(whatIdo);
		(whatIdoData.getValues()).addAll(createValues("will play", "may play", "no play"));

		Node n1 = treeModelManager.getRoot();
		n1.setId("1");
		n1.setScore("will play");

		//
		// Upper half of the tree
		//

		Predicate n2Predicate = createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "sunny");

		Node n2 = treeModelManager.addNode(n1, "2", n2Predicate);
		n2.setScore("will play");

		Predicate n3Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.SURROGATE,
			createSimplePredicate(temperature, SimplePredicate.Operator.LESS_THAN, "90"),
			createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_THAN, "50")
		);

		Node n3 = treeModelManager.addNode(n2, "3", n3Predicate);
		n3.setScore("will play");

		Predicate n4Predicate = createSimplePredicate(humidity, SimplePredicate.Operator.LESS_THAN, "80");

		Node n4 = treeModelManager.addNode(n3, "4", n4Predicate);
		n4.setScore("will play");

		Predicate n5Predicate = createSimplePredicate(humidity, SimplePredicate.Operator.GREATER_OR_EQUAL, "80");

		Node n5 = treeModelManager.addNode(n3, "5", n5Predicate);
		n5.setScore("no play");

		Predicate n6Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
			createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_OR_EQUAL, "90"),
			createSimplePredicate(temperature, SimplePredicate.Operator.LESS_OR_EQUAL, "50")
		);

		Node n6 = treeModelManager.addNode(n2, "6", n6Predicate);
		n6.setScore("no play");

		//
		// Lower half of the tree
		//

		Predicate n7Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "overcast"),
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "rain")
		);

		Node n7 = treeModelManager.addNode(n1, "7", n7Predicate);
		n7.setScore("may play");

		Predicate n8Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
			createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_THAN, "60"),
			createSimplePredicate(temperature, SimplePredicate.Operator.LESS_THAN, "100"),
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "overcast"),
			createSimplePredicate(humidity, SimplePredicate.Operator.LESS_THAN, "70"),
			createSimplePredicate(windy, SimplePredicate.Operator.EQUAL, "false")
		);

		Node n8 = treeModelManager.addNode(n7, "8", n8Predicate);
		n8.setScore("may play");

		Predicate n9Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
			createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "rain"),
			createSimplePredicate(humidity, SimplePredicate.Operator.LESS_THAN, "70")
		);

		Node n9 = treeModelManager.addNode(n7, "9", n9Predicate);
		n9.setScore("no play");

		return treeModelManager.getPmml();
	}

	static
	private List<Value> createValues(String... strings){
		List<Value> values = new ArrayList<Value>();

		for(String string : strings){
			values.add(new Value(string));
		}

		return values;
	}

	static
	private SimplePredicate createSimplePredicate(FieldName name, SimplePredicate.Operator operator, String value){
		SimplePredicate simplePredicate = new SimplePredicate(name, operator);
		simplePredicate.setValue(value);

		return simplePredicate;
	}

	static
	private CompoundPredicate createCompoundPredicate(CompoundPredicate.BooleanOperator operator, Predicate... predicates){
		CompoundPredicate compoundPredicate = new CompoundPredicate(operator);

		List<Predicate> content = compoundPredicate.getContent();
		content.addAll(Arrays.asList(predicates));

		return compoundPredicate;
	}
}