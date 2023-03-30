import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import java.io.File

object Utils {
    fun createOrClearFile(path: String): File {
        return File(path).apply {
            mkdirs()
            listFiles()?.forEach { it.deleteRecursively() }
        }
    }

    fun createFile(path: String): File {
        return File(path).apply {
            mkdirs()
        }
    }

    fun getLemmasFromTokens(
        tokens: List<String>
    ): Map<String, Set<String>> {
        val ru = RussianAnalyzer()
        val en = EnglishAnalyzer()
        val enTokens = tokens.filter {
            it.matches(Regex("[a-zA-Z]+"))
        }
        val ruTokens = tokens.filter {
            it.matches(Regex("[а-яА-Я]+"))
        }
        return mutableMapOf<String, Set<String>>().apply {
            putAll(tokensToLemmas(ru, ruTokens, "ru"))
            putAll(tokensToLemmas(en, enTokens, "en"))
        }
    }
    private fun tokensToLemmas(
        analyzer: Analyzer,
        tokens: List<String>,
        name: String
    ): Map<String, Set<String>> {
        val lemmas = mutableMapOf<String, MutableSet<String>>()
        val stream = analyzer.tokenStream(name, tokens.joinToString(" "))
        stream.reset()
        stream.use {
            var count = 0
            while (stream.incrementToken()) {
                val token = tokens[count]
                val lemma = stream.getAttribute(CharTermAttribute::class.java).toString()
                lemmas[lemma]?.add(token) ?: run {
                    lemmas[lemma] = mutableSetOf(token)
                }
                count++
            }
        }
        return lemmas
    }
}