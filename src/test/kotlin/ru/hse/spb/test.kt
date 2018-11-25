package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

class TestSource {
    @Test
    fun parsing_variableDeclaration() {
        assertEquals(
                parseFunLanguageFile("var va"),
                File(Block(listOf(
                        Statement(
                                VariableDeclaration(1, "va", null))))))
    }

    @Test
    fun parsing_variableDeclarationWithExpression() {
        assertEquals(
                parseFunLanguageFile("var variable = 10"),
                File(Block(listOf(
                        Statement(
                                VariableDeclaration(
                                        1,
                                        "variable",
                                        Expression.createLiteralExpression(10)))))))
    }

    @Test
    fun parsing_assignment() {
        assertEquals(
                parseFunLanguageFile("a = 10"),
                File(Block(listOf(Statement(Assignment(1, "a",
                        Expression.createLiteralExpression(10)))))))
    }

    @Test
    fun parsing_return() {
        assertEquals(
                parseFunLanguageFile("return 1"),
                File(Block(listOf(Statement(Return(Expression.createLiteralExpression(1)))))))
    }

    @Test
    fun parsing_function() {
        assertEquals(
                parseFunLanguageFile(
                "fun f(arg, barg) {}"),
                File(Block(listOf(Statement(
                        Function(1, "f", Block(emptyList()), listOf("arg", "barg")))))))
    }

    @Test
    fun parsing_functionCall() {
        assertEquals(
                parseFunLanguageFile("func(a, b)"),
                File(Block(listOf(Statement(
                        Expression.createFunctionCallExpression(FunctionCall(1, "func",
                                listOf(
                                        Expression.createVariableExpression(Variable(1, "a")),
                                        Expression.createVariableExpression(Variable(1, "b"))))))))))
    }

    @Test
    fun parsing_simpleIf() {
        assertEquals(
                parseFunLanguageFile("if (1) {}"),
                File(Block(listOf(Statement(If(
                        Expression.createLiteralExpression(1),
                        Block(emptyList()),
                        null))))))
    }

    @Test
    fun parsing_ifWithElse() {
        assertEquals(
                parseFunLanguageFile("if (a) {} else {}"),
                File(Block(listOf(Statement(
                        If(Expression.createVariableExpression(Variable(1, "a")),
                                /*  if block  */ Block(emptyList()),
                                /* else block */ Block(emptyList())))))))
    }

    @Test
    fun parsing_whileStatements() {
        assertEquals(
                parseFunLanguageFile("while (a) {} "),
                File(Block(listOf(Statement(
                        While(Expression.createVariableExpression(Variable(1, "a")),
                                /* while block */ Block(emptyList())))))))
    }

    @Test
    fun parsing_binaryExpression() {
        assertEquals(
                parseFunLanguageFile("a + b"),
                File(Block(listOf(Statement(
                        Expression.createBinaryExpression(
                                Expression.createVariableExpression(Variable(1, "a")),
                                BinaryOperator.PLUS,
                                Expression.createVariableExpression(Variable(1, "b"))))))))
    }

    @Test
    fun parsing_variableExpression() {
        assertEquals(
                parseFunLanguageFile("a"),
                File(Block(listOf(Statement(
                        Expression.createVariableExpression(Variable(1, "a")))))))
    }

    @Test
    fun parsing_functionCallExpression() {
        assertEquals(
                parseFunLanguageFile("a + 2"),
                File(Block(listOf(Statement(
                        Expression.createBinaryExpression(
                                Expression.createVariableExpression(Variable(1, "a")),
                                BinaryOperator.PLUS,
                                Expression.createLiteralExpression(2)))))))
    }

    @Test
    fun parsing_expressionInBrackets() {
        assertEquals(
                parseFunLanguageFile("(1)"),
                File(Block(listOf(Statement(
                        Expression.createLiteralExpression(1))))))
    }

    @Test
    fun parsing_literalExpression() {
        assertEquals(
                parseFunLanguageFile("1"),
                File(Block(listOf(Statement(
                        Expression.createLiteralExpression(1))))))
    }

    @Test(expected = ParsingException::class)
    fun parserError() {
        parseFunLanguageFile("fun f(a + b) {}")
    }

    @Test(expected = ParsingException::class)
    fun lexerError() {
        parseFunLanguageFile(";'//.")
    }

    @Test
    fun executing_emptyProgram() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("").exec(Context(stringStream))
        assertEquals("", stringStream.toString())
    }

    @Test
    fun executing_variableDefinition() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("var x = 5\nprintln(x)").exec(Context(stringStream))
        assertEquals("5 \n", stringStream.toString())
    }

    @Test
    fun executing_assignment() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("var x = 5\nx = 10\nprintln(x)").exec(Context(stringStream))
        assertEquals("10 \n", stringStream.toString())
    }

    @Test
    fun executing_literalExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(115)").exec(Context(stringStream))
        assertEquals("115 \n", stringStream.toString())
    }

    @Test
    fun executing_binaryExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(11 + 5)").exec(Context(stringStream))
        assertEquals("16 \n", stringStream.toString())
    }

    @Test
    fun executing_functionCall() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("fun f(x) {println(x)}\n f(10)").exec(Context(stringStream))
        assertEquals("10 \n", stringStream.toString())
    }

    @Test
    fun executing_functionCallExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("fun f(x) {return 42 - x}\n println(f(10) * 2)").exec(Context(stringStream))
        assertEquals("64 \n", stringStream.toString())
    }

    @Test
    fun executing_simpleIf() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("if (5 > 3) {println(11)}").exec(Context(stringStream))
        assertEquals("11 \n", stringStream.toString())
    }

    @Test
    fun executing_ifWithElse() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("if (5 <= 3) {println(11)} else {println(9)}").exec(Context(stringStream))
        assertEquals("9 \n", stringStream.toString())
    }

    @Test
    fun executing_ifWithReturnNotTriggered() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("if (5 <= 3) { return 5 }\nprintln(8)").exec(Context(stringStream))
        assertEquals("8 \n", stringStream.toString())
    }

    @Test
    fun executing_ifWithReturnTriggered() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("if (3 < 5) { return 5 }\nprintln(8)").exec(Context(stringStream))
        assertEquals("", stringStream.toString())
    }

    @Test
    fun executing_while() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("var a = 2\nwhile(a) {println(a)\na = a - 1}").exec(Context(stringStream))
        assertEquals("2 \n1 \n", stringStream.toString())
    }

    @Test
    fun executing_whileWithReturn() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("var a = 2\nwhile(a >= 0) {println(a)\na = a - 1 return 0}").exec(Context(stringStream))
        assertEquals("2 \n", stringStream.toString())
    }

    @Test
    fun executing_blockWithReturn() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(1)\nreturn 5\nprintln(8)").exec(Context(stringStream))
        assertEquals("1 \n", stringStream.toString())
    }

    @Test
    fun executing_andExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(1 && 8)").exec(Context(stringStream))
        assertEquals("1 \n", stringStream.toString())
    }

    @Test
    fun executing_orExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(0 || 8)").exec(Context(stringStream))
        assertEquals("1 \n", stringStream.toString())
    }

    @Test
    fun executing_eqExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(0 == 8)").exec(Context(stringStream))
        assertEquals("0 \n", stringStream.toString())
    }

    @Test
    fun executing_neqExpression() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("println(0 != 8)").exec(Context(stringStream))
        assertEquals("1 \n", stringStream.toString())
    }

    @Test(expected = ExecutionException::class)
    fun executing_unknownVariable() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("a").exec(Context(stringStream))
    }

    @Test(expected = ExecutionException::class)
    fun executing_unknownFunction() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("a()").exec(Context(stringStream))
    }

    @Test(expected = ExecutionException::class)
    fun executing_assignUnknownVariable() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("a = 5").exec(Context(stringStream))
    }

    @Test(expected = ExecutionException::class)
    fun executing_redeclareExistingVariable() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("var a\nvar a").exec(Context(stringStream))
    }

    @Test(expected = ExecutionException::class)
    fun executing_redeclareExistingFunction() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("fun a() {}\nfun a() {}").exec(Context(stringStream))
    }

    @Test(expected = ExecutionException::class)
    fun executing_functionCallArgumentsNumberMismatch() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("fun a(x, y) {}\n a(1, 2, 3)").exec(Context(stringStream))
    }

    @Test
    fun executing_functionNameShadowing() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("fun a() { fun a() {println(10)} \n a()} a()").exec(Context(stringStream))
        assertEquals("10 \n", stringStream.toString())
    }

    @Test
    fun executing_variableNameShadowing() {
        val stringStream = ByteArrayOutputStream()
        parseFunLanguageFile("var x = 10\n fun a() { var x = 6 \n println(x)} a()\n println(x)")
                .exec(Context(stringStream))
        assertEquals("6 \n10 \n", stringStream.toString())
    }

    @Test
    fun execution_simpleTest1() {
        val stringStream = ByteArrayOutputStream()

        parseFunLanguageFile("""
            var a = 10
            var b = 20
            if (a > b) {
                println(1)
            } else {
                println(0)
            }
        """).exec(Context(stringStream))
        assertEquals("0 \n", stringStream.toString())
    }

    @Test
    fun execution_simpleTest2() {
        val stringStream = ByteArrayOutputStream()

        parseFunLanguageFile("""
            fun fib(n) {
                if (n <= 1) {
                    return 1
                }
                return fib(n - 1) + fib(n - 2)
            }

            var i = 1
            while (i <= 5) {
                println(i, fib(i))
                i = i + 1
            }
        """).exec(Context(stringStream))
        assertEquals("1 1 \n2 2 \n3 3 \n4 5 \n5 8 \n", stringStream.toString())
    }

    @Test
    fun execution_simpleTest3() {
        val stringStream = ByteArrayOutputStream()

        parseFunLanguageFile("""
            fun foo(n) {
                fun bar(m) {
                    return m + n
                }

                return bar(1)
            }

            println(foo(41)) // prints 42
        """).exec(Context(stringStream))
        assertEquals("42 \n", stringStream.toString())
    }
}