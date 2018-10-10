package ru.hse.spb

import java.io.OutputStream
import java.lang.Exception

infix fun String.to(str: String): String {
    return "$this=$str"
}

interface Element {
    fun render(out: OutputStream, indent: String)
}

interface Header : Element
interface Code : Element

@DslMarker
annotation class TexTagMarker

class TextElement(val text: String) : Code {
    override fun render(out: OutputStream, indent: String) {
        out.write("$indent$text\n".toByteArray())
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

@TexTagMarker
abstract class Tag(open val name: String) : Element {
    val indentStep
        get() = "    "

    val children = arrayListOf<Code>()
    val parameters = arrayListOf<String>()

    protected fun <T : Code> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    protected fun renderParameters(): String {
        if (parameters.size == 0)
            return ""
        return parameters.joinToString(separator = ", ", prefix = "[", postfix = "]")
    }

    fun customTag(
        name: String,
        vararg properties: String,
        init: BeginEndCommand.() -> Unit
    ) = initTag(CustomTag(name, *properties), init)
    fun frame(frameTitle: String, vararg properties: String, init: BeginEndCommand.() -> Unit) {
        val frameBlock = Frame(frameTitle, *properties)
        initTag(frameBlock, init)
    }

    fun itemize(vararg properties: String, init: Itemize.() -> Unit) = initTag(Itemize(*properties), init)

    fun enumerate(vararg properties: String, init: Enumerate.() -> Unit) = initTag(Enumerate(*properties), init)

    fun alignment(type: String, init: Alignment.() -> Unit) {
        try {
            initTag(Alignment(Alignment.Type.valueOf(type.toUpperCase())), init)
        } catch (e: IllegalArgumentException) {
            throw TexCompilationException("Unknown alignment mode: $type")
        }
    }

    fun math(init: Math.() -> Unit) = initTag(Math(), init)
}

abstract class InlineCommand(override val name: String, private val argument: String) : Tag(name) {
    override fun render(out: OutputStream, indent: String) {
        out.write("$indent\\$name${renderParameters()}{$argument}\n".toByteArray())
    }
}

class FrameTitle(title: String) : InlineCommand("frametitle", title), Code

abstract class MultilineCommand(override val name: String) : TagWithText(name) {
    protected fun renderElements(out: OutputStream, indent: String, elements: ArrayList<out Element>) {
        elements.forEach { it.render(out, indent) }
    }
}

abstract class BeginEndCommand(override val name: String, vararg properties: String) : MultilineCommand(name) {
    override fun render(out: OutputStream, indent: String) {
        out.write("$indent\\begin{$name}${renderParameters()}\n".toByteArray())
        renderElements(out, indent + indentStep, children)
        out.write("$indent\\end{$name}\n".toByteArray())
    }
    init {
        properties.forEach { parameters.add(it) }
    }
}

abstract class SimpleBlockCommand(override val name: String, vararg properties: String) : MultilineCommand(name) {
    override fun render(out: OutputStream, indent: String) {
        out.write("$indent\\$name${renderParameters()}\n".toByteArray())
        renderElements(out, indent + indentStep, children)
    }
    init {
        properties.forEach { parameters.add(it) }
    }
}

@TexTagMarker
class CustomTag(override val name: String, vararg properties: String) : BeginEndCommand(name, *properties), Code

@TexTagMarker
class Frame(frameTitle: String, vararg properties: String) : BeginEndCommand("frame", *properties), Code {
    init {
        children.add(FrameTitle(frameTitle))
    }
}

@TexTagMarker
class Alignment(type: Type) : BeginEndCommand(type.str), Code {
    enum class Type(val str: String) {
        FLUSHLEFT("flushleft"),
        FLUSHRIGHT("flushright"),
        CENTER("center")
    }
}

@TexTagMarker
class Math : BeginEndCommand("math"), Code

@TexTagMarker
abstract class ListCommand(name: String, private vararg val properties: String) : BeginEndCommand(name, *properties) {
    fun item(init: Item.() -> Unit) = initTag(Item(*properties), init)
}

@TexTagMarker
class Itemize(vararg properties: String) : ListCommand("itemize", *properties), Code

@TexTagMarker
class Enumerate(vararg properties: String) : ListCommand("enumerate", *properties), Code

@TexTagMarker
class Item(vararg properties: String) : SimpleBlockCommand("item", *properties), Code

@TexTagMarker
class UsePackage(pkg: String, vararg properties: String) : InlineCommand("usepackage", pkg), Header {
    init {
        properties.forEach { parameters.add(it) }
    }
}

@TexTagMarker
class DocumentClass(type: String) : InlineCommand("documentclass", type), Header

@TexTagMarker
class Document : BeginEndCommand("document") {
    private val headers = arrayListOf<Header>()

    override fun render(out: OutputStream, indent: String) {
        renderElements(out, indent, headers)
        super.render(out, indent)
    }

    fun usepackage(pkg: String, vararg properties: String) = initHeader(UsePackage(pkg, *properties))
    fun documentClass(type: String) = initHeader(DocumentClass(type))

    private fun initHeader(hdr: Header) {
        headers.add(hdr)
    }
}

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}

class TexCompilationException(override val message: String?) : Exception(message)