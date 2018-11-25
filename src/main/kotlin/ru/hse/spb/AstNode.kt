package ru.hse.spb

import java.lang.Exception

interface AstNode

interface Executable : AstNode {
    fun exec(context: Context): Int?
}

interface ErrorProne : AstNode {
    val line: Int
}

data class File(val block: Block) : Executable {
    override fun exec(context: Context): Int? {
        return block.exec(context)
    }
}

data class Block(val statements: List<Statement>) : Executable {
    override fun exec(context: Context): Int? {
        val newContext = context.createSubcontext()
        for (statement in statements) {
            statement.exec(newContext)
            if (newContext.value != null)
                return newContext.value
        }
        return null
    }
}

data class Statement(val executable: Executable) : Executable {
    override fun exec(context: Context): Int? {
        return executable.exec(context)
    }
}

data class Expression private constructor(private val container: ExpressionContainer) : Executable {
    companion object {
        private data class ExpressionContainer(
                val expressionType: ExpressionType,
                val literal: Int? = null,
                val firstExp: Expression? = null,
                val operator: BinaryOperator? = null,
                val secondExpression: Expression? = null,
                val variable: Variable? = null,
                val functionCall: FunctionCall? = null)

        enum class ExpressionType {
            LITERAL,
            BINARY_EXPRESSION,
            VARIABLE,
            FUNCTION_CALL
        }

        fun createLiteralExpression(literal: Int) =
                Expression(ExpressionContainer(ExpressionType.LITERAL, literal = literal))
        fun createBinaryExpression(firstExp: Expression, operator: BinaryOperator, secondExpression: Expression) =
                Expression(ExpressionContainer(
                        ExpressionType.BINARY_EXPRESSION,
                        firstExp = firstExp,
                        operator = operator,
                        secondExpression = secondExpression))
        fun createVariableExpression(variable: Variable) =
                Expression(ExpressionContainer(ExpressionType.VARIABLE, variable = variable))
        fun createFunctionCallExpression(functionCall: FunctionCall) =
                Expression(ExpressionContainer(ExpressionType.FUNCTION_CALL, functionCall = functionCall))
    }

    override fun exec(context: Context): Int {
        return when (container.expressionType) {
            ExpressionType.LITERAL -> container.literal
            ExpressionType.FUNCTION_CALL -> container.functionCall?.exec(context)
            ExpressionType.VARIABLE -> container.variable?.exec(context)
            ExpressionType.BINARY_EXPRESSION ->
                if (container.firstExp != null && container.secondExpression != null)
                    container.operator?.eval?.invoke(
                            container.firstExp.exec(context),
                            container.secondExpression.exec(context))
                else null
        } ?: error(
                "Expression failure: expected fields appeared to be null. Expression type: ${container.expressionType}")
    }
}

data class FunctionCall(
    override val line: Int,
    private val identifier: String,
    private val args: List<Expression>
) : Executable, ErrorProne {
    override fun exec(context: Context): Int {
        val values = args.map { exp: Expression -> exp.exec(context) }
        if (identifier == "println" && context.getFunction(identifier) == null) {
            context.println(values)
            return 0
        }
        val function = context.getFunction(identifier)
                ?: throw ExecutionException("$line::Attempting to call unknown function: $identifier")
        return function.block.exec(
                function.createFunctionSubcontext(context, values)) ?: 0
    }
}

data class Function(
    override val line: Int,
    private val name: String,
    val block: Block,
    private val args: List<String>
) : Executable, ErrorProne {
    override fun exec(context: Context): Int? {
        try {
            context.newFunction(name, this)
        } catch (e: ContextException) {
            throw ExecutionException("$line::${e.message}")
        }
        return null
    }

    fun createFunctionSubcontext(context: Context, values: List<Int>): Context {
        if (values.size != args.size) {
            throw ExecutionException("$line::Expected arguments: ${args.size}, actual: ${values.size}.")
        }
        val newContext = context.createSubcontext()
        for (i in 0 until args.size) {
            newContext.newVariable(args[i], values[i])
        }
        return newContext
    }
}

data class If(private val condition: Expression, private val ifBlock: Block, private val elseBlock: Block?) : Executable {
    override fun exec(context: Context): Int? {
        val result = if (condition.exec(context) != 0) ifBlock.exec(context) else elseBlock?.exec(context)
        if (result != null) {
            context.value = result
        }
        return result
    }
}

data class Assignment(
    override val line: Int,
    private val identifier: String,
    private val expression: Expression
) : Executable, ErrorProne {
    override fun exec(context: Context): Int? {
        val value = expression.exec(context)
        try {
            context.updateVariable(identifier, value)
        } catch (e: ContextException) {
            throw ExecutionException("$line::${e.message}")
        }
        return value
    }
}

data class VariableDeclaration(
    override val line: Int,
    val identifier: String,
    private val expression: Expression? = null
) : Executable, ErrorProne {
    override fun exec(context: Context): Int {
        val value = expression?.exec(context) ?: 0
        try {
            context.newVariable(identifier, value)
        } catch (e: ContextException) {
            throw ExecutionException("$line::${e.message}")
        }
        return value
    }
}

data class Return(private val expression: Expression) : Executable {
    override fun exec(context: Context): Int? {
        context.value = expression.exec(context)
        return context.value
    }
}

data class While(private val condition: Expression, private val block: Block) : Executable {
    override fun exec(context: Context): Int? {
        while (condition.exec(context) != 0) {
            val result = block.exec(context)
            if (result != null) {
                context.value = result
                return result
            }
        }
        return null
    }
}

data class Variable(override val line: Int, private val name: String) : Executable, ErrorProne {
    override fun exec(context: Context): Int {
        return context.getVariable(name)
                ?: throw ExecutionException("$line::Attempting to access variable which doesn't exist: $name")
    }
}

fun Boolean.asInt() = if (this) 1 else 0

enum class BinaryOperator(val eval: (Int, Int) -> Int) {
    MUL({ a, b -> a * b }),
    DIV({ a, b -> a / b }),
    MOD({ a, b -> a % b }),
    PLUS({ a, b -> a + b }),
    MINUS({ a, b -> a - b }),
    LESS_THAN({ a, b -> (a < b).asInt() }),
    GREATER_THAN({ a, b -> (a > b).asInt() }),
    LE({ a, b -> (a <= b).asInt() }),
    GE({ a, b -> (a >= b).asInt() }),
    EQ({ a, b -> (a == b).asInt() }),
    NEQ({ a, b -> (a != b).asInt() }),
    AND({ a, b -> (a != 0 && b != 0).asInt() }),
    OR({ a, b -> (a != 0 || b != 0).asInt() });
}

class ExecutionException(override var message: String) : Exception(message)