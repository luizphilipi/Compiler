package br.com.compiler.compiler;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import br.com.compiler.compiler.exceptions.UndeclaredVariableException;
import br.com.compiler.compiler.exceptions.UndefinedFunctionException;
import br.com.compiler.compiler.exceptions.VariableAlreadyDefinedException;
import br.com.compiler.parser.DemoBaseVisitor;
import br.com.compiler.parser.DemoParser.AndContext;
import br.com.compiler.parser.DemoParser.AssignmentContext;
import br.com.compiler.parser.DemoParser.BranchContext;
import br.com.compiler.parser.DemoParser.DivContext;
import br.com.compiler.parser.DemoParser.FloatContext;
import br.com.compiler.parser.DemoParser.FunctionCallContext;
import br.com.compiler.parser.DemoParser.FunctionDefinitionContext;
import br.com.compiler.parser.DemoParser.MainStatementContext;
import br.com.compiler.parser.DemoParser.MinusContext;
import br.com.compiler.parser.DemoParser.MultContext;
import br.com.compiler.parser.DemoParser.NumberContext;
import br.com.compiler.parser.DemoParser.OrContext;
import br.com.compiler.parser.DemoParser.PlusContext;
import br.com.compiler.parser.DemoParser.PrintContext;
import br.com.compiler.parser.DemoParser.PrintlnContext;
import br.com.compiler.parser.DemoParser.ProgramContext;
import br.com.compiler.parser.DemoParser.RelationalContext;
import br.com.compiler.parser.DemoParser.StringContext;
import br.com.compiler.parser.DemoParser.VarDeclarationContext;
import br.com.compiler.parser.DemoParser.VariableContext;

public class MyVisitor extends DemoBaseVisitor<String> {

	private Map<String, Integer> variables = new HashMap<String, Integer>();
	private JvmStack jvmStack = new JvmStack();
	private final FunctionList definedFunctions;
	private int branchCounter = 0;
	private int compareCount = 0;
	private int andCounter = 0;
	private int orCounter = 0;

	public MyVisitor(FunctionList definedFunctions) {
		if (definedFunctions == null) {
			throw new NullPointerException("definedFunction");
		}
		this.definedFunctions = definedFunctions;
	}

	@Override
	public String visitPrintln(PrintlnContext ctx) {
		String argumentInstruction = visit(ctx.argument);
		DataType type = jvmStack.pop();
		return "	getstatic java/lang/System/out Ljava/io/PrintStream;\n"
				+ argumentInstruction + "\n"
				+ "	invokevirtual java/io/PrintStream/println("
				+ type.getJvmType() + ")V\n";
	}

	@Override
	public String visitPrint(PrintContext ctx) {
		String argumentInstruction = visit(ctx.argument);
		DataType type = jvmStack.pop();
		return "	getstatic java/lang/System/out Ljava/io/PrintStream;\n"
				+ argumentInstruction + "\n"
				+ "	invokevirtual java/io/PrintStream/print("
				+ type.getJvmType() + ")V\n";
	}

	@Override
	public String visitPlus(PlusContext ctx) {
		String instructions = visitChildren(ctx) + "\n";
		DataType pop = jvmStack.pop();
		DataType pop2 = jvmStack.pop();
		String op = "";
		if (pop == DataType.FLOAT || pop2 == DataType.FLOAT) {
			jvmStack.push(DataType.FLOAT);
			op = "fadd";
		} else {
			jvmStack.push(DataType.INT);
			op = "iadd";
		}
		return instructions + op;
	}

	@Override
	public String visitMinus(MinusContext ctx) {
		String instructions = visitChildren(ctx) + "\n" + "isub";
		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.INT);
		return instructions;
	}

	@Override
	public String visitDiv(DivContext ctx) {
		String instructions = visitChildren(ctx) + "\n" + "idiv";
		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.INT);
		return instructions;
	}

	@Override
	public String visitMult(MultContext ctx) {
		String instructions = visitChildren(ctx) + "\n" + "imul";
		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.INT);
		return instructions;
	}

	@Override
	public String visitRelational(RelationalContext ctx) {
		int compareNum = compareCount;
		++compareCount;
		String jumpInstruction;
		switch (ctx.operation.getText()) {
		case "<":
			jumpInstruction = "if_icmplt";
			break;
		case "<=":
			jumpInstruction = "if_icmple";
			break;
		case ">":
			jumpInstruction = "if_icmpgt";
			break;
		case ">=":
			jumpInstruction = "if_icmpge";
			break;
		default:
			throw new IllegalArgumentException("Unknown operator: "
					+ ctx.operation.getText());
		}
		String instructions = visitChildren(ctx) + "\n" + jumpInstruction
				+ " onTrue" + compareNum + "\n" + "ldc 0\n" + "goto onFalse"
				+ compareNum + "\n" + "onTrue" + compareNum + ":\n" + "ldc 1\n"
				+ "onFalse" + compareNum + ":";

		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.INT);
		return instructions;
	}

	@Override
	public String visitAnd(AndContext ctx) {
		String left = visit(ctx.left);
		String right = visit(ctx.right);
		int andNum = andCounter;
		++andCounter;

		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.INT);

		return left + "\n" + "ifeq onAndFalse" + andNum + "\n" + right + "\n"
				+ "ifeq onAndFalse" + andNum + "\n" + "ldc 1\n" + "goto andEnd"
				+ andNum + "\n" + "onAndFalse" + andNum + ":\n" + "ldc 0\n"
				+ "andEnd" + andNum + ":";
	}

	@Override
	public String visitOr(OrContext ctx) {
		String left = visit(ctx.left);
		String right = visit(ctx.right);
		int orNum = orCounter;
		++orCounter;

		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.INT);

		return left + "\n" + "ifne onOrTrue" + orNum + "\n" + right + "\n"
				+ "ifne onOrTrue" + orNum + "\n" + "ldc 0\n" + "goto orEnd"
				+ orNum + "\n" + "onOrTrue" + orNum + ":\n" + "ldc 1\n"
				+ "orEnd" + orNum + ":";
	}

	@Override
	public String visitNumber(NumberContext ctx) {
		jvmStack.push(DataType.INT);
		return "ldc " + ctx.number.getText();
	}

	@Override
	public String visitFloat(FloatContext ctx) {
		jvmStack.push(DataType.FLOAT);
		return "ldc " + ctx.r_float.getText();
	}

	@Override
	public String visitString(StringContext ctx) {
		jvmStack.push(DataType.STRING);
		return "ldc " + ctx.string.getText();
	}

	@Override
	public String visitVarDeclaration(VarDeclarationContext ctx) {
		if (variables.containsKey(ctx.varName.getText())) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		variables.put(ctx.varName.getText(), variables.size());
		return "";
	}

	@Override
	public String visitAssignment(AssignmentContext ctx) {
		String instructions = visit(ctx.expr) + "\n" + "istore "
				+ requiredVariableIndex(ctx.varName);
		jvmStack.pop();
		return instructions;
	}

	@Override
	public String visitVariable(VariableContext ctx) {
		jvmStack.push(DataType.INT);
		return "iload " + requiredVariableIndex(ctx.varName);
	}

	@Override
	public String visitFunctionCall(FunctionCallContext ctx) {
		int numberOfParameters = ctx.arguments.expressions.size();
		if (!definedFunctions.contains(ctx.funcName.getText(),
				numberOfParameters)) {
			throw new UndefinedFunctionException(ctx.funcName);
		}
		String instructions = "";
		String argumentsInstructions = visit(ctx.arguments);
		if (argumentsInstructions != null) {
			instructions += argumentsInstructions + "\n";
		}
		instructions += "invokestatic HelloWorld/" + ctx.funcName.getText()
				+ "(";
		instructions += stringRepeat("I", numberOfParameters);
		instructions += ")I";
		for (int i = 0; i < numberOfParameters; i++) {
			jvmStack.pop();
		}
		jvmStack.push(DataType.INT);
		return instructions;
	}

	@Override
	public String visitFunctionDefinition(FunctionDefinitionContext ctx) {
		Map<String, Integer> oldVariables = variables;
		JvmStack oldJvmStack = jvmStack;
		variables = new HashMap<String, Integer>();
		jvmStack = new JvmStack();
		visit(ctx.params);
		String statementInstructions = visit(ctx.statements);
		String result = ".method public static " + ctx.funcName.getText() + "(";
		int numberOfParameters = ctx.params.declarations.size();
		result += stringRepeat("I", numberOfParameters);
		result += ")I\n"
				+ ".limit locals 100\n"
				+ ".limit stack 100\n"
				+ (statementInstructions == null ? "" : statementInstructions
						+ "\n") + visit(ctx.returnValue) + "\n" + "ireturn\n"
				+ ".end method";
		jvmStack.pop();
		variables = oldVariables;
		jvmStack = oldJvmStack;
		return result;
	}

	private String stringRepeat(String string, int count) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < count; i++) {
			result.append(string);
		}
		return result.toString();
	}

	@Override
	public String visitProgram(ProgramContext ctx) {
		String mainCode = "";
		String functions = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);
			String instructions = visit(child);
			if (child instanceof MainStatementContext) {
				mainCode += instructions + "\n";
			} else {
				functions += instructions + "\n";
			}
		}
		return functions + ".method public static main([Ljava/lang/String;)V\n"
				+ "	.limit stack 100\n" + "	.limit locals 100\n" + "\n"
				+ mainCode + "\n" + "return\n" + "\n" + ".end method";
	}

	@Override
	public String visitBranch(BranchContext ctx) {
		String conditionInstructions = visit(ctx.condition);
		jvmStack.pop();
		String onTrueInstructions = visit(ctx.onTrue);
		String onFalseInstructions = visit(ctx.onFalse);
		int branchNum = branchCounter;
		branchCounter++;

		return conditionInstructions + "\n" + "ifne ifTrue" + branchNum + "\n"
				+ onFalseInstructions + "\n" + "goto endIf" + branchNum + "\n"
				+ "ifTrue" + branchNum + ":\n" + onTrueInstructions + "\n"
				+ "endIf" + branchNum + ":\n";
	}

	private int requiredVariableIndex(Token varNameToken) {
		Integer varIndex = variables.get(varNameToken.getText());
		if (varIndex == null) {
			throw new UndeclaredVariableException(varNameToken);
		}
		return varIndex;
	}

	@Override
	protected String aggregateResult(String aggregate, String nextResult) {
		if (aggregate == null) {
			return nextResult;
		}
		if (nextResult == null) {
			return aggregate;
		}
		return aggregate + "\n" + nextResult;
	}
}
