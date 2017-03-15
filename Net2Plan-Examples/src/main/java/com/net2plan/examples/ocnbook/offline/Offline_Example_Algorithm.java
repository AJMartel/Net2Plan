package com.net2plan.examples.ocnbook.offline;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import java.util.List;
import java.util.Map;

public class Offline_Example_Algorithm implements IAlgorithm
{
	private InputParameter simpleParameter               = new InputParameter("simpleParameter", "Default value", "The user may enter the desired value in a string format.");
	private InputParameter booleanParameter              = new InputParameter("booleanParameter", "#boolean# true", "Represents a true/false parameter through the use of a checkbox.");
	private InputParameter selectParameter               = new InputParameter("selectParameter", "#select# First Second Third", "Allows the user to choose from a given array of choices.");
	private InputParameter pathParameter 				 = new InputParameter("pathParameter", "#path# Sample text", "Brings up a file selector in order to choose a directory");
	private InputParameter fileChooserParameter          = new InputParameter("fileChooserParameter", "#file# Sample text", "Brings up a file selector in order to choose a file");
	private InputParameter multipleFilesChooserParameter = new InputParameter("multipleFilesParameter", "#files# Sample text", "Brings up a file selector in order to choose multiple files." +
			" The files' paths are separated with the string '>'.");

	@Override
	public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
	{
		InputParameter.initializeAllInputParameterFieldsOfObject(this, algorithmParameters);

		System.out.printf("Simple parameter: " + simpleParameter.getString());
		System.out.println("Boolean parameter: " + Boolean.parseBoolean(booleanParameter.getString()));
		System.out.println("Select parameter: " + selectParameter.getString());
		System.out.println("Path chooser parameter: " + pathParameter.getString());
		System.out.println("File chooser parameter: " + fileChooserParameter.getString());

		final String[] multipleFilesPath = multipleFilesChooserParameter.getString().split(">");
		for(String path : multipleFilesPath) System.out.println("Multiple file chooser parameter: " + path);

		return "Ok";
	}

	@Override
	public String getDescription()
	{
		return "Example of different parameter types.";
	}

	@Override
	public List<Triple<String, String, String>> getParameters()
	{
		return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
	}
}
