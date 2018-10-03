package ru.hse.spb

import org.antlr.v4.runtime.*
import ru.hse.spb.parser.FunLanguageLexer
import ru.hse.spb.parser.FunLanguageParser

fun main(args: Array<String>) {
    println(parseFunLanguageFile("println(5)").exec(Context(System.`out`)))
}

class LexerErrorListener : BaseErrorListener() {
    override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?, line: Int,
            charPositionInLine: Int, msg: String?,
            e: RecognitionException?) {
        error("$line::lexer error")
    }
}

class ParserErrorListener : BaseErrorListener() {
    override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?) {
        error("$line::parser error")
    }
}

fun parseFunLanguageFile(funCode: String) : File {
    val funLanguageLexer = FunLanguageLexer(CharStreams.fromString(funCode))
    val funLanguageParser = FunLanguageParser(BufferedTokenStream(funLanguageLexer))
    funLanguageLexer.removeErrorListeners()
    funLanguageParser.removeErrorListeners()
    funLanguageLexer.addErrorListener(LexerErrorListener())
    funLanguageParser.addErrorListener(ParserErrorListener())

    return funLanguageParser.file().accept(FunLanguageVisitor()) as File
}