package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

public class CompileException extends RuntimeException {

	private static final long serialVersionUID = -1124747349797294058L;

	protected final int line;
	protected final int column;

	public CompileException(Token token) {
		line = token.getLine();
		column = token.getCharPositionInLine();
	}
}
