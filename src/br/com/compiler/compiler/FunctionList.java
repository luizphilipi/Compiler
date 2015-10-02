package br.com.compiler.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FunctionList {

	private Collection<FunctionDefinition> definitions = new ArrayList<FunctionDefinition>();

	public FunctionDefinition findFunctionDefinition(String functionName, List<DataType> parameters) {
		for (FunctionDefinition definition : definitions) {
			if (definition.getFunctionName().equals(functionName)
					&& definition.getParameters().equals(parameters)) {
				return definition;
			}
		}
		return null;
	}

	public void add(String functionName, List<DataType> parameters,
			DataType returnType) {
		definitions.add(new FunctionDefinition(functionName, parameters,
				returnType));
	}
}
