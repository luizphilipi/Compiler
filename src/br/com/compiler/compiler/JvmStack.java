package br.com.compiler.compiler;

import java.util.Deque;
import java.util.LinkedList;

public class JvmStack {

	private Deque<DataType> typesOnStack = new LinkedList<DataType>();

	public void push(DataType type) {
		typesOnStack.push(type);
	}

	public DataType pop() {
		return typesOnStack.pop();
	}

}
