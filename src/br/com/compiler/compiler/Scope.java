package br.com.compiler.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.Token;

import br.com.compiler.compiler.exceptions.UndeclaredVariableException;

public class Scope {

	private Map<Variable, Integer> variables = new HashMap<Variable, Integer>();

	public Scope() {
		super();
	}

	public Scope(Scope scope) {
		super();
		for (Entry<Variable, Integer> entry : scope.variables.entrySet()) {
			this.variables.put(entry.getKey(), entry.getValue());
		}
	}

	public Map<Variable, Integer> getVariables() {
		return variables;
	}

	public void setVariables(Map<Variable, Integer> variables) {
		this.variables = variables;
	}

	public boolean containsVariable(String variableName) {
		return variables.containsKey(new Variable(null, variableName));
	}

	public void addVariable(DataType dataType, String name) {
		variables.put(new Variable(dataType, name), variables.size());
	}
	
	public int requiredVariableIndex(Token varNameToken) {
		Integer varIndex = variables.get(new Variable(null, varNameToken
				.getText()));
		if (varIndex == null) {
			throw new UndeclaredVariableException(varNameToken);
		}
		return varIndex;
	}

	public Variable findVariable(Token varNameToken) {
		for (Variable variable : variables.keySet()) {
			if (variable.getName().equals(varNameToken.getText())) {
				return variable;
			}
		}
		throw new UndeclaredVariableException(varNameToken);
	}
}
