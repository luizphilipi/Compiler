package br.com.compiler.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import br.com.compiler.compiler.exceptions.UndeclaredVariableException;
import br.com.compiler.compiler.exceptions.UndefinedFunctionException;
import br.com.compiler.compiler.exceptions.UnexpectedToken;
import br.com.compiler.compiler.exceptions.VariableAlreadyDefinedException;
import br.com.compiler.parser.DemoBaseVisitor;
import br.com.compiler.parser.DemoParser.AndContext;
import br.com.compiler.parser.DemoParser.AssignmentContext;
import br.com.compiler.parser.DemoParser.BranchContext;
import br.com.compiler.parser.DemoParser.ConstructorDeclarationContext;
import br.com.compiler.parser.DemoParser.DivContext;
import br.com.compiler.parser.DemoParser.FloatContext;
import br.com.compiler.parser.DemoParser.ForStatContext;
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
import br.com.compiler.parser.DemoParser.UnaryContext;
import br.com.compiler.parser.DemoParser.VarDeclarationContext;
import br.com.compiler.parser.DemoParser.VariableContext;
import br.com.compiler.parser.DemoParser.WhileStatementContext;

public class MyVisitor extends DemoBaseVisitor<String> {

	private Map<Variable, Integer> variables = new HashMap<Variable, Integer>();
	private JvmStack jvmStack = new JvmStack();
	private final FunctionList definedFunctions;
	private int branchCounter = 0;
	private int compareCount = 0;
	private int andCounter = 0;
	private int orCounter = 0;
	private int forCounter = 0;
	private int whileCounter = 0;

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
		return "getstatic java/lang/System/out Ljava/io/PrintStream;\n"
				+ argumentInstruction + "\n"
				+ "invokevirtual java/io/PrintStream/println("
				+ type.getJvmType() + ")V\n";
	}

	@Override
	public String visitPrint(PrintContext ctx) {
		String argumentInstruction = visit(ctx.argument);
		DataType type = jvmStack.pop();
		return "getstatic java/lang/System/out Ljava/io/PrintStream;\n"
				+ argumentInstruction + "\n"
				+ "invokevirtual java/io/PrintStream/print("
				+ type.getJvmType() + ")V\n";
	}

	@Override
	public String visitUnary(UnaryContext ctx) {
		int index = requiredVariableIndex(ctx.varName);
		String load = "";
		// load = "iload " + index;
		// jvmStack.push(DataType.INT);
		switch (ctx.operation.getText()) {
		case "++":
			return "iinc " + index + " 1\n" + load;
		case "--":
			return "iinc " + index + " -1\n" + load;
		default:
			throw new UnexpectedToken(ctx.operation);
		}
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
		//String instructions = visitChildren(ctx) + "\n" + "isub";
		String instructions = visitChildren(ctx) + "\n";
		DataType pop = jvmStack.pop();
		DataType pop2 = jvmStack.pop();
		
		String op = "";
		if (pop == DataType.FLOAT || pop2 == DataType.FLOAT) {
			jvmStack.push(DataType.FLOAT);
			op = "fsub";
		} else {
			jvmStack.push(DataType.INT);
			op = "isub";
		}
		return instructions + op;
	}

	@Override
	public String visitDiv(DivContext ctx) {
		//String instructions = visitChildren(ctx) + "\n" + "idiv";
		String instructions = visitChildren(ctx) + "\n";
		DataType pop = jvmStack.pop();
		DataType pop2 = jvmStack.pop();
		String op = "";
		if (pop == DataType.FLOAT || pop2 == DataType.FLOAT) {
			jvmStack.push(DataType.FLOAT);
			op = "fdiv";
		} else{
			jvmStack.push(DataType.INT);
			op = "idiv";
		}
		return instructions + op;
	}

	@Override
	public String visitMult(MultContext ctx) {
		//String instructions = visitChildren(ctx) + "\n" + "imul";
		String instructions = visitChildren(ctx) + "\n";
		DataType pop = jvmStack.pop();
		DataType pop2 = jvmStack.pop();
		String op = "";
		if (pop == DataType.FLOAT || pop2 == DataType.FLOAT) {
			jvmStack.push(DataType.FLOAT);
			op = "fmul";
		} else{
			jvmStack.push(DataType.INT);
			op = "imul";
		}
		return instructions + op;
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
		case "==":
			jumpInstruction = "if_acmpeq";
			break;
		case "!=":
			jumpInstruction = "if_acmpne";
			break;
		default:
			throw new IllegalArgumentException("Unknown operator: "
					+ ctx.operation.getText());
		}
		String instructions = visitChildren(ctx) + "\n" + jumpInstruction
				+ " onTrue" + compareNum + "\n" + "ldc 0\n" + "goto onFalse"
				+ compareNum + "\n" + "onTrue" + compareNum + ":\n" + "ldc 1\n"
				+ "onFalse" + compareNum + ":\n";

		jvmStack.pop();
		jvmStack.pop();
		jvmStack.push(DataType.BOOLEAN);
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
		jvmStack.push(DataType.BOOLEAN);

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
		jvmStack.push(DataType.BOOLEAN);

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
		if (variables.containsKey(new Variable(null, ctx.varName.getText()))) {
			throw new VariableAlreadyDefinedException(ctx.varName);
		}
		variables.put(new Variable(DataType.identifyVariableDataType(ctx.type),
				ctx.varName.getText()), variables.size());
		String instructions = "";
		if (ctx.expr != null) {
			instructions = visit(ctx.expr) + "\n" + "istore "
					+ requiredVariableIndex(ctx.varName) + "\n";
			jvmStack.pop();
		}
		return instructions;
	}

	@Override
	public String visitAssignment(AssignmentContext ctx) {
		String instructions = visit(ctx.expr) + "\n" + "istore "
				+ requiredVariableIndex(ctx.varName) + "\n";
		jvmStack.pop();
		return instructions;
	}

	@Override
	public String visitVariable(VariableContext ctx) {
		DataType dataType = findVariable(ctx.varName).getDataType();
		jvmStack.push(dataType);
		return dataType.getInstructionPrefix() + "load "
				+ requiredVariableIndex(ctx.varName);
	}

	@Override
	public String visitFunctionCall(FunctionCallContext ctx) {
		String instructions = "";
		String argumentsInstructions = visit(ctx.arguments);
		if (argumentsInstructions != null) {
			instructions += argumentsInstructions + "\n";
		}
		int numberOfParameters = ctx.arguments.expressions.size();
		List<DataType> types = new ArrayList<DataType>();
		StringBuilder parameterTypeSequence = new StringBuilder();
		for (int i = 0; i < numberOfParameters; i++) {
			DataType dataType = jvmStack.pop();
			types.add(dataType);
			parameterTypeSequence.append(dataType.getJvmType());
		}
		Collections.reverse(types);
		FunctionDefinition functionDefinition = definedFunctions
				.findFunctionDefinition(ctx.funcName.getText(), types);
		if (functionDefinition == null) {
			throw new UndefinedFunctionException(ctx.funcName);
		}
		instructions += "invokestatic HelloWorld/" + ctx.funcName.getText()
				+ "(";
		instructions += parameterTypeSequence.reverse().toString();
		instructions += ")" + functionDefinition.getReturnType().getJvmType();
		jvmStack.push(functionDefinition.getReturnType());
		return instructions;
	}

	@Override
	public String visitFunctionDefinition(FunctionDefinitionContext ctx) {
		Map<Variable, Integer> oldVariables = variables;
		JvmStack oldJvmStack = jvmStack;
		variables = new HashMap<Variable, Integer>();
		jvmStack = new JvmStack();
		visit(ctx.params);
		DataType returnType = DataType.identifyVariableDataType(ctx.returnType);
		String statementInstructions = visit(ctx.statements);
		String result = ".method public static " + ctx.funcName.getText() + "(";
		result += identifyParamTypes(ctx.params.declarations);
		result += ")"
				+ returnType.getJvmType()
				+ "\n"
				+ ".limit locals 100\n"
				+ ".limit stack 100\n"
				+ (statementInstructions == null ? "" : statementInstructions
						+ "\n") + visit(ctx.returnValue) + "\n"
				+ returnType.getInstructionPrefix() + "return\n"
				+ ".end method\n";
		jvmStack.pop();
		variables = oldVariables;
		jvmStack = oldJvmStack;
		return result;
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
				+ ".limit stack 100\n" + ".limit locals 100\n" + "\n"
				+ mainCode + "\n" + "return\n" + "\n" + ".end method";
	}

	@Override
	public String visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
		String constructor = ".method public <init>(";
		constructor += identifyParamTypes(ctx.params.declarations);
		constructor += ")V\n" + "aload_0\n"
				+ "invokenonvirtual java/lang/Object/<init>()V\n";

		String body = visit(ctx.statements);
		return constructor + body + "return\n" + ".end method";
	}

	@Override
	public String visitWhileStatement(WhileStatementContext ctx) {
		String conditionInstructions = visit(ctx.condition);
		String whileTrueInstructions = visit(ctx.whileTrue);
		int whileNum = whileCounter;
		whileCounter++;
		return "\n" + "whileStart" + whileNum + ":\n\n" + conditionInstructions
				+ "\n" + "ifeq endWhile" + whileNum + "\n" + "\n"
				+ whileTrueInstructions + "\n" + "goto whileStart" + whileNum
				+ "\n" + "endWhile" + whileNum + ":\n";
	}

	@Override
	public String visitBranch(BranchContext ctx) {
		String conditionInstructions = visit(ctx.condition);
		jvmStack.pop();
		int branchNum = branchCounter;
		branchCounter++;
		String onTrueInstructions = visit(ctx.onTrue);
		if (ctx.onFalse == null) {
			return conditionInstructions + "\n" + "ifeq endIf" + branchNum
					+ "\n" + onTrueInstructions + "endIf" + branchNum + ":";
		} else {
			String onFalseInstructions = visit(ctx.onFalse);

			return conditionInstructions + "\n" + "ifne ifTrue" + branchNum
					+ "\n" + onFalseInstructions + "\n" + "goto endIf"
					+ branchNum + "\n" + "ifTrue" + branchNum + ":\n"
					+ onTrueInstructions + "\n" + "endIf" + branchNum + ":\n";
		}
	}

	@Override
	public String visitForStat(ForStatContext ctx) {
		String instructions = visit(ctx.declaration);
		int forNum = forCounter;
		forCounter++;
		instructions += "\nforStart" + forNum + ":";
		instructions += "\n" + visit(ctx.expr);
		instructions += "\nifeq endFor" + forNum;
		instructions += "\n" + visit(ctx.forBlock);
		instructions += "\n" + visit(ctx.assign);
		instructions += "\ngoto forStart" + forNum;
		instructions += "\nendFor" + forNum + ":";
		return instructions;
	}

	private int requiredVariableIndex(Token varNameToken) {
		Integer varIndex = variables.get(new Variable(null, varNameToken
				.getText()));
		if (varIndex == null) {
			throw new UndeclaredVariableException(varNameToken);
		}
		return varIndex;
	}

	private Variable findVariable(Token varNameToken) {
		for (Variable variable : variables.keySet()) {
			if (variable.getName().equals(varNameToken.getText())) {
				return variable;
			}
		}
		throw new UndeclaredVariableException(varNameToken);
	}

	private String identifyParamTypes(
			List<VarDeclarationContext> varDeclarationContexts) {
		StringBuilder result = new StringBuilder();
		for (VarDeclarationContext varDeclarationContext : varDeclarationContexts) {
			result.append(DataType.identifyVariableDataType(
					varDeclarationContext.type).getJvmType());
		}
		return result.toString();
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
