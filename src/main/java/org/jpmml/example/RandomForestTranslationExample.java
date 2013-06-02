/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.beust.jcommander.Parameter;

public class RandomForestTranslationExample extends Example {

	@Parameter (
		names = {"--model"},
		description = "The PMML file with a Random forest model",
		required = true
	)
	private File model = null;


	static
	public void main(String... args) throws Exception {
		execute(RandomForestTranslationExample.class, args);
	}

	@Override
	public void execute() throws Exception {
		PMML pmml = IOUtil.unmarshal(this.model);

		MiningModelManager miningModelManager = new MiningModelManager(pmml);

		List<Segment> segments = miningModelManager.getSegments();
		for(Segment segment : segments){
			TreeModelManager treeModelManager = new TreeModelManager(pmml, (TreeModel)segment.getModel());

			System.out.println("String segment_" + segment.getId() + "(){");

			TreeModelTranslationExample.format(treeModelManager.getOrCreateRoot(), "\t");

			System.out.println("}");
		}
	}
}