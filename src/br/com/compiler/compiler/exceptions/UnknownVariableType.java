package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class UnknownVariableType extends CompileException {

	private static final long serialVersionUID = 6087458119702959452L;

	private final String type;

	public UnknownVariableType(Token typeToken) {
		super(typeToken);
		type = typeToken.getText();
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " unknown primitive type: <"
				+ type + ">";
	}
}
