package task4

import Constants
import Utils
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*

class TfIdfCalculator {

    private val enExclude =
        FileReader(File("src/main/kotlin/com/example/inf_search_web/task2/stopwords/en")).readLines()
    private val ruExclude =
        FileReader(File("src/main/kotlin/com/example/inf_search_web/task2/stopwords/ru")).readLines()

    fun execute() {
        val lemmasFile = Utils.createOrClearFile(Constants.tfIdfLemmasPath)
        val tokensFile = Utils.createOrClearFile(Constants.tfIdfTokensPath)

        val pages = getPagesWithTf()
        val invertedTokens = getInvertedTokens(pages)
        val invertedLemmas = getInvertedLemmas(pages)

        pages.forEachIndexed { i, page ->
            val terminsIdf = calculateIdf(
                page.termins.keys,
                invertedTokens,
                pages.size
            )
            val lemmasIdf = calculateIdf(
                page.lemmasTf.keys,
                invertedLemmas,
                pages.size
            )
            val resultTokenFile =
                File(tokensFile, "tf-idf.$i.txt").apply { createNewFile() }
            FileOutputStream(resultTokenFile, true).bufferedWriter().use { output ->
                page.termins.keys.forEach { token ->
                    output.appendLine(
                        "$token ${terminsIdf[token]} ${page.terminsTf[token]!! * terminsIdf[token]!!}"
                    )
                }
            }
            val resultLemmasFile =
                File(lemmasFile, "tf-idf.$i.txt").apply { createNewFile() }
            FileOutputStream(resultLemmasFile, true).bufferedWriter().use { output ->
                page.lemmasTf.forEach { lemma, tf ->
                    output.appendLine(
                        "$lemma ${lemmasIdf[lemma]} ${tf * lemmasIdf[lemma]!!}"
                    )
                }
            }
        }
    }

    private fun getDownloadedPages(): File {
        return File(Constants.resultPath + '/' + Constants.downloadedHtmlPackage)
    }

    private fun parseTextFromFileWithoutHtml(file: File): String {
        return Jsoup.parse(file.bufferedReader().use { it.readText() }).body().text()
    }

    private fun getPagesWithTf(): List<PageTf> {
        val terminsCount = mutableListOf<PageTf>()
        val files = getDownloadedPages().listFiles().sortedBy {
            it.name
        }
        files.forEachIndexed { i, file ->
            val textWithoutHtml = parseTextFromFileWithoutHtml(file)

            /**
             * Разбиваем текст на токены, убирая ненужные символы и слова, содержащие числа
             */
            val termins = textWithoutHtml.lowercase(Locale.getDefault())
                .replace(Regex("[^А-Яа-яA-Za-z ]"), " ")
                .split(" ")
                .filter {
                    !enExclude.contains(it) &&
                            !ruExclude.contains(it) &&
                            !it.contains(Regex("[0-9]+")) &&
                            it.isNotEmpty()
                }
            val map = mutableMapOf<String, Int>()
            termins.forEach { termin ->
                map[termin]?.let {
                    map[termin] = it + 1
                } ?: run {
                    map[termin] = 1
                }
            }
            terminsCount.add(PageTf(map))
        }
        return terminsCount
    }

    private fun getInvertedTokens(pages: List<PageTf>): Map<String, Set<Int>> {
        val tokensWithIndex = mutableMapOf<String, MutableSet<Int>>()
        pages.forEachIndexed { i, page ->
            page.termins.forEach {
                val lemma = it.key
                tokensWithIndex[lemma]?.add(i) ?: run {
                    tokensWithIndex[lemma] = mutableSetOf(i)
                }
            }
        }
        return tokensWithIndex
    }

    private fun getInvertedLemmas(pages: List<PageTf>): Map<String, Set<Int>> {
        val lemmasWithIndex = mutableMapOf<String, MutableSet<Int>>()
        pages.forEachIndexed { i, page ->
            page.lemmasTf.forEach {
                val lemma = it.key
                lemmasWithIndex[lemma]?.add(i) ?: run {
                    lemmasWithIndex[lemma] = mutableSetOf(i)
                }
            }
        }
        return lemmasWithIndex
    }

    private fun calculateIdf(
        tokens: Set<String>,
        invertedWords: Map<String, Set<Int>>,
        documentsCount: Int
    ): Map<String, Double> {
        val result = tokens.map {
            val idf = Math.log(documentsCount.toDouble() / invertedWords[it]!!.size)
            Pair(it, idf)
        }
        return mutableMapOf<String, Double>().apply {
            putAll(result)
        }
    }

}