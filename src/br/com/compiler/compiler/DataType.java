package br.com.compiler.compiler;

public enum DataType {
	INT("I"), FLOAT("F"), STRING("Ljava/lang/String;");

	private final String jvmType;

	private DataType(String jvmType) {
		this.jvmType = jvmType;
	}

	public String getJvmType() {
		return this.jvmType;
	}

}
