package br.com.compiler.compiler;

import java.util.List;

public class FunctionDefinition {
	private final String functionName;
	private final List<DataType> parameters;
	private final DataType returnType;

	public FunctionDefinition(String functionName, List<DataType> parameters,
			DataType returnType) {
		super();
		this.functionName = functionName;
		this.parameters = parameters;
		this.returnType = returnType;
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<DataType> getParameters() {
		return parameters;
	}

	public DataType getReturnType() {
		return returnType;
	}

}