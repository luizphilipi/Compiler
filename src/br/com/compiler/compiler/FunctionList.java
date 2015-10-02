package br.com.compiler.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FunctionList {

	private Collection<FunctionDefinition> definitions = new ArrayList<FunctionList.FunctionDefinition>();

	public boolean contains(String functionName, List<DataType> parameters) {
		for (FunctionDefinition definition : definitions) {
			if (definition.functionName.equals(functionName)
					&& definition.parameters.equals(parameters)) {
				return true;
			}
		}
		return false;
	}

	public void add(String functionName, List<DataType> parameters) {
		definitions.add(new FunctionDefinition(functionName, parameters));
	}

	private static class FunctionDefinition {
		private String functionName;
		private List<DataType> parameters;

		public FunctionDefinition(String functionName, List<DataType> parameters) {
			super();
			this.functionName = functionName;
			this.parameters = parameters;
		}

	}
}
