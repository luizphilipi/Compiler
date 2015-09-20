package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class UndefinedFunctionException extends CompileException {
	private static final long serialVersionUID = 3750636922219104196L;

	private final String functionName;

	public UndefinedFunctionException(Token functionNameToken) {
		super(functionNameToken);
		functionName = functionNameToken.getText();
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " call to undefined function: <"
				+ functionName + ">";
	}
}
