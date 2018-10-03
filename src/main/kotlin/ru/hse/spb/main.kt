package ru.hse.spb

import org.antlr.v4.runtime.*
import ru.hse.spb.parser.FunLanguageLexer
import ru.hse.spb.parser.FunLanguageParser

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Filename was expected as an argument, but ${args.size} arguments were found.")
        return
    }
    println(parseFunLanguageFile(CharStreams.fromFileName(args[0]).toString()).exec(Context(System.`out`)))
}

class LexerErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        throw ParsingException("$line::lexer error")
    }
}

class ParserErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        throw ParsingException("$line::parser error")
    }
}

class ParsingException(override var message: String) : Exception(message)

fun parseFunLanguageFile(funCode: String): File {
    val funLanguageLexer = FunLanguageLexer(CharStreams.fromString(funCode))
    val funLanguageParser = FunLanguageParser(BufferedTokenStream(funLanguageLexer))
    funLanguageLexer.removeErrorListeners()
    funLanguageParser.removeErrorListeners()
    funLanguageLexer.addErrorListener(LexerErrorListener())
    funLanguageParser.addErrorListener(ParserErrorListener())

    return funLanguageParser.file().accept(FunLanguageVisitor()) as File
}