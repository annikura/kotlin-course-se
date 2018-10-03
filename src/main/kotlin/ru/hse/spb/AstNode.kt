package ru.hse.spb

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

data class Expression private constructor(
        private val expressionType: ExpressionType,
        private val literal: Int? = null,
        private val firstExp: Expression? = null,
        private val operator: BinaryOperator? = null,
        private val secondExpression: Expression? = null,
        private val variable: Variable? = null,
        private val functionCall: FunctionCall? = null) : Executable {
    companion object {
        private enum class ExpressionType {
            LITERAL,
            BINARY_EXPRESSION,
            VARIABLE,
            FUNCTION_CALL
        }
    }

    override fun exec(context: Context): Int {
        return when(expressionType) {
            ExpressionType.LITERAL -> literal
            ExpressionType.FUNCTION_CALL -> functionCall?.exec(context)
            ExpressionType.VARIABLE -> variable?.exec(context)
            ExpressionType.BINARY_EXPRESSION ->
                if (firstExp != null && secondExpression != null)
                    operator?.eval?.invoke(firstExp.exec(context), secondExpression.exec(context))
                else null
        } ?: error("Expression failure: expected fields appeared to be null. Expression type: $expressionType")
    }

    constructor(literal: Int) : this(ExpressionType.LITERAL, literal = literal)
    constructor(firstExp: Expression, operator:BinaryOperator, secondExpression: Expression)
            : this(
                ExpressionType.BINARY_EXPRESSION,
                firstExp = firstExp,
                operator = operator,
                secondExpression = secondExpression)
    constructor(variable: Variable)
            : this(ExpressionType.VARIABLE, variable = variable)
    constructor(functionCall: FunctionCall)
            : this(ExpressionType.FUNCTION_CALL, functionCall = functionCall)
}

data class FunctionCall(override val line: Int, private val identifier: String, private val args: List<Expression>) : Executable, ErrorProne {
    override fun exec(context: Context): Int {
        val values = args.map { exp: Expression -> exp.exec(context) }
        if (identifier == "println" && context.getFunction(identifier) == null) {
            context.println(values)
            return 0
        }
        val function = context.getFunction(identifier) ?: error("$line::Attempting to call unknown function: $identifier")
        return function.block.exec(
                function.createFunctionSubcontext(context, values)) ?: 0
    }
}

data class Function(override val line: Int, private val name: String, val block: Block, private val args: List<String>) : Executable, ErrorProne {
    override fun exec(context: Context): Int? {
        try {
            context.newFunction(name, this)
        } catch (e: IllegalStateException) {
            error("$line::${e.message}")
        }
        return null
    }

    fun createFunctionSubcontext(context: Context, values: List<Int>) : Context {
        if (values.size != args.size) {
            error("$line::Expected arguments: ${args.size}, actual: ${values.size}.")
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

data class Assignment(override val line: Int, private val identifier: String, private val expression: Expression) : Executable, ErrorProne {
    override fun exec(context: Context): Int? {
        val value = expression.exec(context)
        try {
            context.updateVariable(identifier, value)
        } catch (e: IllegalStateException) {
            error("$line::${e.message}")
        }
        return value
    }
}

data class VariableDeclaration(override val line: Int, val identifier: String, private val expression: Expression? = null) : Executable, ErrorProne {
    override fun exec(context: Context): Int {
        val value = expression?.exec(context) ?: 0
        try {
            context.newVariable(identifier, value)
        } catch (e: IllegalStateException) {
            error("$line::${e.message}")
        }
        return value
    }
}

data class Return(private val expression: Expression): Executable {
    override fun exec(context: Context): Int? {
        context.value = expression.exec(context)
        return context.value
    }
}

data class While(private val condition: Expression, private val block: Block) :Executable {
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
                ?: error("$line::Attempting to access variable which doesn't exist: $name")
    }
}

enum class BinaryOperator {
    MUL({ a, b -> a * b }),
    DIV({ a, b -> a / b }),
    MOD({ a, b -> a % b }),
    PLUS({ a, b -> a + b }),
    MINUS({ a, b -> a - b}),
    LESS_THAN({ a, b -> if (a < b) 1 else 0 }),
    GREATER_THAN({ a, b -> if (a > b) 1 else 0 }),
    LE({ a, b -> if (a <= b) 1 else 0 }),
    GE({ a, b -> if (a >= b) 1 else 0 }),
    EQ({ a, b -> if (a == b) 1 else 0 }),
    NEQ({ a, b -> if (a != b) 1 else 0 }),
    AND({ a, b -> if (a != 0 && b != 0) 1 else 0 }),
    OR({ a, b -> if (a != 0 || b != 0) 1 else 0 });

    val eval: (Int, Int) -> Int

    constructor(expression: (Int, Int) -> Int) {
        eval = expression
    }
}