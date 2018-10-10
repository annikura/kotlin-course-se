package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

class TestSource {
    @Test
    fun emptyDocument() {
        val out = ByteArrayOutputStream()
        document { }.render(out, "")
        assertEquals("""
            \begin{document}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun simpleUsePackage() {
        val out = ByteArrayOutputStream()
        document { usepackage("pkg") }.render(out, "")
        assertEquals("""
            \usepackage{pkg}
            \begin{document}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun usePackageWithParameters() {
        val out = ByteArrayOutputStream()
        document { usepackage("pkg", "arg1", "arg2") }.render(out, "")
        assertEquals("""
            \usepackage[arg1, arg2]{pkg}
            \begin{document}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun documentClass() {
        val out = ByteArrayOutputStream()
        document { documentClass("class") }.render(out, "")
        assertEquals("""
            \documentclass{class}
            \begin{document}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun documentClassAndUsepackage() {
        val out = ByteArrayOutputStream()
        document {
            documentClass("class")
            usepackage("pkg", "p")
        }.render(out, "")
        assertEquals("""
            \documentclass{class}
            \usepackage[p]{pkg}
            \begin{document}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun documentWithText() {
        val out = ByteArrayOutputStream()
        document {
            +"Some text that should be here"
        }.render(out, "")
        assertEquals("""
            \begin{document}
                Some text that should be here
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun math() {
        val out = ByteArrayOutputStream()
        document {
            math { +"a + b / c" }
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{math}
                    a + b / c
                \end{math}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun mathMixedWithText() {
        val out = ByteArrayOutputStream()
        document {
            +"Look at this formula:"
            math { +"a + b / c" }
            +"This formula is amazing"
        }.render(out, "")
        assertEquals("""
            \begin{document}
                Look at this formula:
                \begin{math}
                    a + b / c
                \end{math}
                This formula is amazing
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun centerAlignment() {
        val out = ByteArrayOutputStream()
        document {
            alignment("center") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{center}
                \end{center}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun leftAlignment() {
        val out = ByteArrayOutputStream()
        document {
            alignment("flushleft") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{flushleft}
                \end{flushleft}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun rightAlignment() {
        val out = ByteArrayOutputStream()
        document {
            alignment("flushright") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{flushright}
                \end{flushright}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test(expected = TexCompilationException::class)
    fun wrongAlignment() {
        document {
            alignment("wow") {}
        }
    }

    @Test
    fun simpleFrame() {
        val out = ByteArrayOutputStream()
        document {
            frame("Frame!") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{frame}
                    \frametitle{Frame!}
                \end{frame}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun frameWithVarargs() {
        val out = ByteArrayOutputStream()
        document {
            frame("F", "kotlin" to "language") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{frame}[kotlin=language]
                    \frametitle{F}
                \end{frame}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun emptyItemize() {
        val out = ByteArrayOutputStream()
        document {
            itemize {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{itemize}
                \end{itemize}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun emptyEnumerate() {
        val out = ByteArrayOutputStream()
        document {
            enumerate() {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{enumerate}
                \end{enumerate}
            \end{document}

        """.trimIndent(), out.toString())
    }
    @Test
    fun simpleItemize() {
        val out = ByteArrayOutputStream()
        document {
            itemize {
                item { +"First" }
                item { +"Second" }
            }
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{itemize}
                    \item
                        First
                    \item
                        Second
                \end{itemize}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun simpleEnumerate() {
        val out = ByteArrayOutputStream()
        document {
            enumerate {
                item { +"First" }
                item { +"Second" }
            }
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{enumerate}
                    \item
                        First
                    \item
                        Second
                \end{enumerate}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun parametrizedItemize() {
        val out = ByteArrayOutputStream()
        document {
            itemize("arg", "x" to "y") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{itemize}[arg, x=y]
                \end{itemize}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun parametrizedEnumerate() {
        val out = ByteArrayOutputStream()
        document {
            enumerate("x" to "y", "arg") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{enumerate}[x=y, arg]
                \end{enumerate}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun simpleCustomTag() {
        val out = ByteArrayOutputStream()
        document {
            customTag("myTag") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{myTag}
                \end{myTag}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun customTag() {
        val out = ByteArrayOutputStream()
        document {
            customTag("myTag", "arg", "a" to "b") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{myTag}[arg, a=b]
                \end{myTag}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun hardTest() {
        val out = ByteArrayOutputStream()
        document {
            itemize {
                item { }
                item {
                    enumerate {
                        item { }
                        item { }
                        item { }
                    }
                }
                item { }
            }
            alignment("center") {
                itemize {
                    item {
                        itemize {
                            item { }
                        }
                    }
                    item { math { +"AAAAA" } }
                    item { }
                }
            }
            frame(frameTitle = "Dunno") {}
        }.render(out, "")
        assertEquals("""
            \begin{document}
                \begin{itemize}
                    \item
                    \item
                        \begin{enumerate}
                            \item
                            \item
                            \item
                        \end{enumerate}
                    \item
                \end{itemize}
                \begin{center}
                    \begin{itemize}
                        \item
                            \begin{itemize}
                                \item
                            \end{itemize}
                        \item
                            \begin{math}
                                AAAAA
                            \end{math}
                        \item
                    \end{itemize}
                \end{center}
                \begin{frame}
                    \frametitle{Dunno}
                \end{frame}
            \end{document}

        """.trimIndent(), out.toString())
    }

    @Test
    fun sampleTest() {
        val out = ByteArrayOutputStream()
        val rows = listOf(1, 2, 3)
        document {
            documentClass("beamer")
            usepackage("babel", "russian" /* varargs */)
            frame("frametitle", "arg1" to "arg2") {
                itemize {
                    for (row in rows) {
                        item { + "$row text" }
                    }
                }

                // begin{pyglist}[language=kotlin]...\end{pyglist}
                customTag("pyglist", "language" to "kotlin") {
                    +"""
               |val a = 1
               |
            """.trimMargin("|")
                }
            }
        }.render(out, "")
        assertEquals("""
            \documentclass{beamer}
            \usepackage[russian]{babel}
            \begin{document}
                \begin{frame}[arg1=arg2]
                    \frametitle{frametitle}
                    \begin{itemize}
                        \item
                            1 text
                        \item
                            2 text
                        \item
                            3 text
                    \end{itemize}
                    \begin{pyglist}[language=kotlin]
                        val a = 1

                    \end{pyglist}
                \end{frame}
            \end{document}

        """.trimIndent(), out.toString())
    }
}