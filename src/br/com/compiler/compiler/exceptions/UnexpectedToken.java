package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class UnexpectedToken extends CompileException {

	private static final long serialVersionUID = 6087458119702959452L;

	private final String token;

	public UnexpectedToken(Token token) {
		super(token);
		this.token = token.getText();
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " unexpected token: <" + token + ">";
	}
}
