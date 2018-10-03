package ru.hse.spb

import java.io.OutputStream
import java.lang.Exception

class Context {
    private val parentContext: Context?
    private val variables = mutableMapOf<String, Int>()
    private val functions = mutableMapOf<String, Function>()
    private val stream: OutputStream
    var value: Int? = null
        set(value) =
            if (field != null)
                throw ContextException("Attempting to reset value while context is complete.")
            else field = value

    constructor(stream: OutputStream) {
        parentContext = null
        this.stream = stream
    }

    constructor(parent: Context) {
        parentContext = parent
        stream = parent.stream
    }

    fun getVariable(identifier: String): Int? {
        return variables[identifier] ?: parentContext?.getVariable(identifier)
    }

    fun getFunction(identifier: String): Function? {
        return functions[identifier] ?: parentContext?.getFunction(identifier)
    }

    fun newVariable(identifier: String, value: Int = 0) {
        if (variables.containsKey(identifier)) {
            throw ContextException("Attempting to redeclare existing variable.")
        }
        variables[identifier] = value
    }

    fun newFunction(identifier: String, function: Function) {
        if (functions.containsKey(identifier)) {
            throw ContextException("Attempting to redeclare existing function.")
        }
        functions[identifier] = function
    }

    fun updateVariable(identifier: String, value: Int) {
        if (variables.containsKey(identifier)) {
            variables[identifier] = value
        } else {
            parentContext?.updateVariable(identifier, value)
                    ?: throw ContextException("Attempting to access variable out of scope.")
        }
    }

    fun createSubcontext(): Context {
        return Context(this)
    }

    fun println(out: List<Int>) {
        for (value in out) {
            stream.write("$value ".toByteArray())
        }
        stream.write("\n".toByteArray())
    }
}

class ContextException(override var message: String) : Exception(message)