package ru.hse.spb

import ru.hse.spb.parser.FunLanguageBaseVisitor
import ru.hse.spb.parser.FunLanguageParser

class FunLanguageVisitor : FunLanguageBaseVisitor<AstNode>() {
    override fun visitFile(ctx: FunLanguageParser.FileContext): File {
        return File(ctx.block().accept(this) as Block)
    }

    override fun visitBlock(ctx: FunLanguageParser.BlockContext): Block {
        return Block(ctx.statement().map { statementContext -> statementContext.accept(this) as Statement })
    }

    override fun visitFunction(ctx: FunLanguageParser.FunctionContext): Function {
        return Function(
                ctx.start.line,
                ctx.id.text,
                ctx.funBlock.accept(this) as Block,
                ctx.params.IDENTIFIER().map { id -> id.text })
    }

    override fun visitFunctionCall(ctx: FunLanguageParser.FunctionCallContext): FunctionCall {
        return FunctionCall(
                ctx.start.line,
                ctx.id.text,
                ctx.arguments().expression().map { arg -> arg.accept(this) as Expression })
    }

    override fun visitBlockWithBraces(ctx: FunLanguageParser.BlockWithBracesContext): Block {
        return ctx.block().accept(this) as Block
    }

    override fun visitAssignment(ctx: FunLanguageParser.AssignmentContext): Assignment {
        return Assignment(ctx.start.line, ctx.IDENTIFIER().text, ctx.exp.accept(this) as Expression)
    }

    override fun visitExpression(ctx: FunLanguageParser.ExpressionContext): Expression =
            when {
                ctx.exp != null  -> ctx.exp.accept(this) as Expression
                ctx.firstExp != null && ctx.secondExp != null
                                 -> Expression.createBinaryExpression(
                        ctx.firstExp.accept(this) as Expression,
                        getOperator(ctx.op.text),
                        ctx.secondExp.accept(this) as Expression)
                ctx.func != null -> Expression.createFunctionCallExpression(ctx.func.accept(this) as FunctionCall)
                ctx.num != null  -> Expression.createLiteralExpression(ctx.num.text.toInt())
                ctx.id != null   -> Expression.createVariableExpression(Variable(ctx.id.line, ctx.id.text))
                else             -> error("${ctx.start.line}Unknown expression type")
            }


    override fun visitIfStatement(ctx: FunLanguageParser.IfStatementContext): If {
        return If(
                ctx.cond.accept(this) as Expression,
                ctx.ifBlock.accept(this) as Block,
                ctx.elseBlock?.accept(this) as Block?)
    }

    override fun visitReturnStatement(ctx: FunLanguageParser.ReturnStatementContext): Return {
        return Return(ctx.exp.accept(this) as Expression)
    }

    override fun visitStatement(ctx: FunLanguageParser.StatementContext) =
            when {
                ctx.assign != null -> Statement(ctx.assign.accept(this) as Assignment)
                ctx.branch != null -> Statement(ctx.branch.accept(this) as If)
                ctx.`var`  != null -> Statement(ctx.`var`.accept(this) as VariableDeclaration)
                ctx.exp    != null -> Statement(ctx.exp.accept(this) as Expression)
                ctx.func   != null -> Statement(ctx.function().accept(this) as Function)
                ctx.loop   != null -> Statement(ctx.loop.accept(this) as While)
                ctx.ret    != null -> Statement(ctx.ret.accept(this) as Return)
                else               -> error("${ctx.start.line}::Unknown statement type")
            }

    override fun visitVariableDeclaration(ctx: FunLanguageParser.VariableDeclarationContext): VariableDeclaration {
        return VariableDeclaration(
                ctx.start.line,
                ctx.id.text,
                ctx.exp?.accept(this) as Expression?)
    }

    override fun visitWhileStatement(ctx: FunLanguageParser.WhileStatementContext): While {
        return While(
                ctx.cond.accept(this) as Expression,
                ctx.whileBlock.accept(this) as Block)
    }

    companion object {
        private fun getOperator(operator: String): BinaryOperator {
            return when (operator) {
                "+" -> BinaryOperator.PLUS
                "-" -> BinaryOperator.MINUS
                "&&" -> BinaryOperator.AND
                "||" -> BinaryOperator.OR
                "==" -> BinaryOperator.EQ
                "!=" -> BinaryOperator.NEQ
                "<" -> BinaryOperator.LESS_THAN
                ">" -> BinaryOperator.GREATER_THAN
                "<=" -> BinaryOperator.LE
                ">=" -> BinaryOperator.GE
                "*" -> BinaryOperator.MUL
                "/" -> BinaryOperator.DIV
                "%" -> BinaryOperator.MOD
                else -> error("Unknown operator: $operator")
            }
        }
    }
}