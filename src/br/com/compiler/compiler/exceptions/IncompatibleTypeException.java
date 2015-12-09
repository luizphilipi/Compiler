package br.com.compiler.compiler.exceptions;

import org.antlr.v4.runtime.Token;

import br.com.compiler.compiler.DataType;

public class IncompatibleTypeException extends CompileException {

	private static final long serialVersionUID = 6087458119702959452L;

	private final String variableName;
	private final DataType leftHand;
	private final DataType rightHand;
	
	
	public IncompatibleTypeException(Token variableNameToken, DataType leftHand, DataType rightHand) {
		super(variableNameToken);
		this.variableName = variableNameToken.getText();
		this.leftHand = leftHand;
		this.rightHand = rightHand;
	}

	@Override
	public String getMessage() {
		return line + ":" + column + " incompatible type of variable: <"
				+ variableName + ">, found " + rightHand + " but expected " + leftHand;
	}
}
