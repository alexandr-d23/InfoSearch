package task4

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

data class PageTf(
    val termins: Map<String, Int>//термин - сколько раз встречается
) {

    val terminsTf = calculateTerminsTf()
    val lemmasTf = calculateLemmasTf()

    private fun calculateTerminsTf(): Map<String, Double> {
        val sum = termins.map {
            it.value
        }.sum()
        return termins.map {
            Pair(it.key, it.value.toDouble() / sum)
        }.toMap()
    }

    private fun calculateLemmasTf(): Map<String, Double> {
        val lemmas = Utils.getLemmasFromTokens(termins.map { it.key })
        return lemmas.map { lemma ->
            Pair(lemma.key, lemma.value.sumOf { terminsTf[it]!! })
        }.toMap()
    }
}