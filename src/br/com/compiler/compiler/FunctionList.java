package br.com.compiler.compiler;

import java.util.ArrayList;
import java.util.Collection;

public class FunctionList {

	private Collection<FunctionDefinition> definitions = new ArrayList<FunctionList.FunctionDefinition>();

	public boolean contains(String functionName, int parameterCount) {
		for (FunctionDefinition definition : definitions) {
			if (definition.functionName.equals(functionName)
					&& definition.parameterCount == parameterCount) {
				return true;
			}
		}
		return false;
	}

	public void add(String functionName, int parameterCount) {
		definitions.add(new FunctionDefinition(functionName, parameterCount));
	}

	private static class FunctionDefinition {
		private String functionName;
		private int parameterCount;

		public FunctionDefinition(String functionName, int parameterCount) {
			super();
			this.functionName = functionName;
			this.parameterCount = parameterCount;
		}

	}
}
