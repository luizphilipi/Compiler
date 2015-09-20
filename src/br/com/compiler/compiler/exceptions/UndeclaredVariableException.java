package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class UndeclaredVariableException extends CompileException {

	private static final long serialVersionUID = -1234410573900856074L;

	private final String varName;

	public UndeclaredVariableException(Token varNameToken) {
		super(varNameToken);
		varName = varNameToken.getText();
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " undeclared variable <" + varName + ">";
	}

}
