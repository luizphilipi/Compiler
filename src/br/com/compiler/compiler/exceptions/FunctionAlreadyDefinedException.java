package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class FunctionAlreadyDefinedException extends CompileException {

	private static final long serialVersionUID = 6087458119702959452L;

	private final String functionName;

	public FunctionAlreadyDefinedException(Token functionNameToken) {
		super(functionNameToken);
		functionName = functionNameToken.getText();
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " function already defined: <"
				+ functionName + ">";
	}
}
