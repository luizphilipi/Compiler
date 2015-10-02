package br.com.compiler.compiler;

import java.util.ArrayList;
import java.util.List;

import br.com.compiler.compiler.exceptions.UnknownVariableType;
import br.com.compiler.parser.DemoParser.PrimitiveTypeContext;
import br.com.compiler.parser.DemoParser.VarDeclarationContext;

public enum DataType {
	BOOLEAN("Z", "i"), INT("I", "i"), FLOAT("F", "f"), STRING("Ljava/lang/String;", "a");

	private final String jvmType;
	private final String instructionPrefix;

	private DataType(String jvmType, String instructionPrefix) {
		this.jvmType = jvmType;
		this.instructionPrefix = instructionPrefix;
	}

	public String getJvmType() {
		return this.jvmType;
	}

	public String getInstructionPrefix() {
		return instructionPrefix;
	}

	public static DataType identifyVariableDataType(PrimitiveTypeContext ctx) {
		switch (ctx.getText()) {
		case "int":
			return DataType.INT;
		case "string":
			return DataType.STRING;
		case "float":
			return DataType.FLOAT;
		case "boolean":
			return DataType.BOOLEAN;
		}
		throw new UnknownVariableType(ctx.type);
	}

	public static List<DataType> identifyVariableDataType(
			List<VarDeclarationContext> varDeclarationContextList) {
		List<DataType> types = new ArrayList<DataType>();
		if (varDeclarationContextList != null) {
			for (VarDeclarationContext varDeclarationContext : varDeclarationContextList) {
				types.add(identifyVariableDataType(varDeclarationContext.type));
			}
		}
		return types;
	}

}
