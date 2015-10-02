package br.com.compiler.compiler;

import org.antlr.v4.runtime.tree.ParseTree;

import br.com.compiler.compiler.exceptions.FunctionAlreadyDefinedException;
import br.com.compiler.parser.DemoBaseVisitor;
import br.com.compiler.parser.DemoParser.FunctionDefinitionContext;

public class FunctionDefinitionFinder {

	public static FunctionList findFunctions(ParseTree tree) {
		final FunctionList definedFunctions = new FunctionList();

		new DemoBaseVisitor<Void>() {
			@Override
			public Void visitFunctionDefinition(FunctionDefinitionContext ctx) {
				String functionName = ctx.funcName.getText();
				if (definedFunctions.findFunctionDefinition(functionName, DataType
						.identifyVariableDataType(ctx.params.declarations)) != null) {
					throw new FunctionAlreadyDefinedException(ctx.funcName);
				}
				definedFunctions.add(functionName, DataType
						.identifyVariableDataType(ctx.params.declarations), DataType.identifyVariableDataType(ctx.returnType));
				return null;
			}
		}.visit(tree);
		return definedFunctions;
	}

}
