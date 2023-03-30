package task2

import Constants
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.io.File
import java.io.FileOutputStream
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.jsoup.Jsoup
import java.io.FileReader
import java.util.*

class Tokenization {

    private val enExclude =
        FileReader(File("src/main/kotlin/com/example/inf_search_web/task2/stopwords/en")).readLines()
    private val ruExclude =
        FileReader(File("src/main/kotlin/com/example/inf_search_web/task2/stopwords/ru")).readLines()

    fun execute() {
        val lemmasFile = Utils.createOrClearFile(Constants.lemmasPath)
        val tokensFile = Utils.createOrClearFile(Constants.tokensPath)

        val pages = getPagesWithTokens()
        pages.forEach { page ->
            writeTokens(page, tokensFile)
            getAndWriteLemmas(page, lemmasFile)
        }
        println("\nWrited tokens and lemmas")
    }

    private fun getDownloadedPages(): File {
        return File(Constants.resultPath + '/' + Constants.downloadedHtmlPackage)
    }

    private fun getPagesWithTokens(): List<PageWithTokens> {
        val result = mutableListOf<PageWithTokens>()
        getDownloadedPages().listFiles()?.sortedBy {
            it.name
        }?.forEach { file ->
            val textWithoutHtml = parseTextFromFileWithoutHtml(file)

            /**
             * Разбиваем текст на токены, убирая ненужные символы и слова, содержащие числа
             */
            val tokens = textWithoutHtml.lowercase(Locale.getDefault())
                .replace(Regex("[^А-Яа-яA-Za-z ]"), " ")
                .split(" ")
                .filter {
                    !enExclude.contains(it) &&
                            !ruExclude.contains(it) &&
                            !it.contains(Regex("[0-9]+")) &&
                            it.isNotEmpty()
                }
                .distinct()

            result.add(
                PageWithTokens(
                    title = file.name,
                    tokens = tokens
                )
            )
        }
        return result
    }



    private fun writeTokens(
        page: PageWithTokens,
        tokensFile: File
    ) {
        val fileName = "${page.title}.txt"
        val resultTokenFile =
            File(tokensFile, fileName).apply { createNewFile() }
        FileOutputStream(resultTokenFile, true).bufferedWriter().use { output ->
            page.tokens.forEach { token ->
                output.appendLine(token)
            }
        }
    }

    private fun getAndWriteLemmas(
        page: PageWithTokens,
        lemmasFile: File
    ) {
        val fileName = "${page.title}.txt"
        val resultLemmasFile =
            File(lemmasFile, fileName).apply { createNewFile() }

        val lemmas = Utils.getLemmasFromTokens(page.tokens)
        FileOutputStream(resultLemmasFile, true).bufferedWriter().use { output ->
            lemmas.forEach { (lemma, lemmaTokens) ->
                output.appendLine("$lemma: ${lemmaTokens.joinToString(" ")}")
            }
        }
    }

    private fun parseTextFromFileWithoutHtml(file: File): String {
        return Jsoup.parse(file.bufferedReader().use { it.readText() }).body().text()
    }

}