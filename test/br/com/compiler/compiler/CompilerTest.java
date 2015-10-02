package br.com.compiler.compiler;

import jasmin.ClassFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import br.com.compiler.compiler.exceptions.FunctionAlreadyDefinedException;
import br.com.compiler.compiler.exceptions.UndeclaredVariableException;
import br.com.compiler.compiler.exceptions.UndefinedFunctionException;
import br.com.compiler.compiler.exceptions.VariableAlreadyDefinedException;

public class CompilerTest {
	private Path tempDir;

	@BeforeMethod
	public void createTempDir() throws IOException {
		tempDir = Files.createTempDirectory("compilerTest");
	}

	@AfterMethod
	public void deleteTempDir() {
		deleteRecursive(tempDir.toFile());
	}

	private void deleteRecursive(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				deleteRecursive(child);
			}
		}
		if (!file.delete()) {
			throw new Error("Could not delete file <" + file + ">");
		}
	}

	@Test(dataProvider = "provide_code_expectedText")
	public void runningCode_outputsExpectedText(String description,
			String code, String expectedText) throws Exception {
		// execution
		String actualOuput = compileAndRun(code);

		// evaluation
		Assert.assertEquals(actualOuput, expectedText);
	}

	@Test(expectedExceptions = UndeclaredVariableException.class, expectedExceptionsMessageRegExp = "1:8 undeclared variable <x>")
	public void compilingCode_throwsUndeclaredVariableException_ifReadingUndefinedVariable()
			throws Exception {
		// execution
		compileAndRun("println(x);");

		// evaluation performed by expected exception
	}

	@Test(expectedExceptions = UndeclaredVariableException.class, expectedExceptionsMessageRegExp = "1:0 undeclared variable <x>")
	public void compilingCode_throwsUndeclaredVariableException_ifWritingUndefinedVariable()
			throws Exception {
		// execution
		compileAndRun("x = 5;");

		// evaluation performed by expected exception
	}

	@Test(expectedExceptions = VariableAlreadyDefinedException.class, expectedExceptionsMessageRegExp = "2:4 variable already defined: <x>")
	public void compilingCode_throwsVariableAlreadyDefinedException_whenDefiningAlreadyDefinedVariable()
			throws Exception {
		// execution
		compileAndRun("int x;" + System.lineSeparator() + "int x;");

		// evaluation performed by expected exception
	}

	@Test(expectedExceptions = UndefinedFunctionException.class, expectedExceptionsMessageRegExp = "1:8 call to undefined function: <someUndefinedFunction>")
	public void compilingCode_throwsUndefinedFunctionException_whenCallingUndefinedFunction()
			throws Exception {
		// execution
		compileAndRun("println(someUndefinedFunction());");

		// evaluation performed by expected exception
	}

	@Test(expectedExceptions = FunctionAlreadyDefinedException.class, expectedExceptionsMessageRegExp = "2:4 function already defined: <x>")
	public void compilingCode_throwsFunctionAlreadyDefinedException_whenDefiningFunctionTwice()
			throws Exception {
		// execution
		compileAndRun("int x() { return 42; }\n" + "int x() { return 42; }");

		// evaluation performed by expected exception
	}

	@DataProvider
	public Object[][] provide_code_expectedText() throws Exception {
		return new Object[][] {
				{ "plus", "println(1+2);", "3" + System.lineSeparator() },
				{ "chained plus", "println(1+2+42);",
						"45" + System.lineSeparator() },
				{
						"multiple statements",
						"println(1); println(2);",
						"1" + System.lineSeparator() + "2"
								+ System.lineSeparator() },
				{ "minus", "println(3-2);", "1" + System.lineSeparator() },
				{ "times", "println(2*3);", "6" + System.lineSeparator() },
				{ "divide", "println(6/2);", "3" + System.lineSeparator() },
				{ "divide and truncate", "println(7/2);",
						"3" + System.lineSeparator() },
				{ "divide and times", "println(8/2*4);",
						"16" + System.lineSeparator() },
				{ "plus and times", "println(3*3+2);",
						"11" + System.lineSeparator() },
				{ "minus and times", "println(9-2*3);",
						"3" + System.lineSeparator() },
				{ "int variable", "int foo; foo = 42; println(foo);",
						"42" + System.lineSeparator() },
				{ "add var and constant parameter",
						"int foo; foo = 42; println(foo+2);",
						"44" + System.lineSeparator() },
				{ "add two vars parameter",
						"int a; int b; a = 2; b = 5; println(a+b);",
						"7" + System.lineSeparator() },
				{
						"return only function",
						"int randomNumber() { return 4; } println(randomNumber());",
						"4" + System.lineSeparator() },
				{
						"simple function",
						"int randomNumber() {\n" + "int i;\n" + "i = 4;\n"
								+ "return i;\n" + "}\n"
								+ "println(randomNumber());",
						"4" + System.lineSeparator() },
				{
						"scopes",
						"int randomNumber() {\n" + "	int i;\n" + "	i = 4;\n"
								+ "	return i;\n" + "}\n" + "int i;\n"
								+ "i = 42;\n" + "println(randomNumber());\n"
								+ "println(i);",
						"4" + System.lineSeparator() + "42"
								+ System.lineSeparator() },
				{
						"int parameters",
						"int add(int a, int b) {\n" + "	return a+b;\n" + "}\n"
								+ "println(add(5,8));",
						"13" + System.lineSeparator() },
				{
						"overloading",
						"int x() { return 0; }\n"
								+ "int x(int a) { return a; }\n"
								+ "println(x());\n" + "println(x(42));",
						"0" + System.lineSeparator() + "42"
								+ System.lineSeparator() },
				{
						"if int false",
						"if (0) {\n" + "	println(81);\n" + "} else {\n"
								+ "	println(42);\n" + "}",
						"42" + System.lineSeparator() },
				example("branch/if_int_true", "81" + System.lineSeparator()),
				example("string/print_string", "teste"),
				example("r_float/print_float",
						"15.561" + System.lineSeparator()),
				example("r_float/int_plus_float",
						"8.8" + System.lineSeparator()),

				example("r_float/minus_float",
						"2.0" + System.lineSeparator()),
						
				example("r_float/div_float",
						"2.5" + System.lineSeparator()),
						
				example("r_float/mul_float",
						"18.15" + System.lineSeparator()),	
					
				{ "lower than true", "println(1 < 2);",
						"true" + System.lineSeparator() },
				{ "lower than false", "println(2 < 2);",
						"false" + System.lineSeparator() },
				{ "lower than or equal true", "println(2 <= 2);",
						"true" + System.lineSeparator() },
				{ "lower than or equal false", "println(3 <= 2);",
						"false" + System.lineSeparator() },
				{ "greater than true", "println(3 > 2);",
						"true" + System.lineSeparator() },
				{ "greater than false", "println(2 > 2);",
						"false" + System.lineSeparator() },
				{ "greater than or equal true", "println(2 >= 2);",
						"true" + System.lineSeparator() },
				{ "greater than or equal false", "println(1 >= 2);",
						"false" + System.lineSeparator() },

				{ "and true", "println(1 && 1);",
						"true" + System.lineSeparator() },

				{ "and left false", "println(0 && 1);",
						"false" + System.lineSeparator() },
				{ "and right false", "println(1 && 0);",
						"false" + System.lineSeparator() },
				example("operators/and-skip-right",
						"0" + System.lineSeparator() + "false"
								+ System.lineSeparator()),


				{ "or false", "println(0 || 0);",
						"false" + System.lineSeparator() },
						
				{ "or left true", "println(1 || 0);",
						"true" + System.lineSeparator() },
				{ "or right true", "println(0 || 1);",
						"true" + System.lineSeparator() },
				example("operators/or-skip-right", "1" + System.lineSeparator()
						+ "true" + System.lineSeparator()),

				{ "print", "print(42);", "42" },
				example("unary/unary-plus-plus", "1"),
				// example("unary/unary-plus-plus-inline", "1"),
				example("unary/unary-minus-minus", "-1"),
				// example("unary/unary-minus-minus-inline", "-1"),

				example("forStatement/for_stat", "01234"),

				example("whileStatement/while_stat", "34"),

				example("function/floatFunction", "13.5") };
	}

	private static String[] example(String name, String expectedResult)
			throws Exception {
		try (InputStream in = CompilerTest.class
				.getResourceAsStream("/examples/" + name + ".txt")) {
			if (in == null) {
				throw new IllegalArgumentException("No such example <" + name
						+ ">");
			}
			Scanner scanner = new Scanner(in, "UTF-8");
			scanner.useDelimiter("\\A");
			String code = scanner.next();
			scanner.close();
			return new String[] { name, code, expectedResult };
		}
	}

	private String compileAndRun(String code) throws Exception {
		code = Main.compile(new ANTLRInputStream(code));
		System.out.println(code);
		System.out
				.println("******************************************************************************************");
		ClassFile classFile = new ClassFile();
		classFile.readJasmin(new StringReader(code), "", false);
		Path outputPath = tempDir.resolve(classFile.getClassName() + ".class");
		try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
			classFile.write(outputStream);
		}
		return runJavaClass(tempDir, classFile.getClassName());
	}

	private String runJavaClass(Path dir, String className) throws Exception {
		Process process = Runtime.getRuntime().exec(
				new String[] { "java", "-cp", dir.toString(), className });
		try (InputStream in = process.getInputStream()) {
			return new Scanner(in).useDelimiter("\\A").next();
		}
	}
}
