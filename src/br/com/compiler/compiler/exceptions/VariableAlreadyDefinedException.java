package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class VariableAlreadyDefinedException extends CompileException {

	private static final long serialVersionUID = 1648933348249665859L;

	private final String varName;

	public VariableAlreadyDefinedException(Token varNameToken) {
		super(varNameToken);
		varName = varNameToken.getText();
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " variable already defined: <" + varName
				+ ">";
	}

}
