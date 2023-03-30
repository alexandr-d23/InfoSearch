package task3

import java.io.File
import java.io.FileOutputStream

class BooleanSearch {

    fun makeInvertedList() {
        val tableFile = File(Utils.createOrClearFile(Constants.invertedListPath), "inverted_list").apply {
            createNewFile()
        }
        val indexes = getInvertedIndex()
        FileOutputStream(tableFile, true).bufferedWriter().use { output ->
            indexes.forEach { (lemma, lemmaTokens) ->
                output.appendLine("$lemma: ${lemmaTokens.joinToString(" ")}")
            }
        }
    }

    private fun getInvertedIndex(): Map<String, Set<Int>> {
        val lemmasWithIndex = mutableMapOf<String, MutableSet<Int>>()
        val lemmasFile = Utils.createFile(Constants.lemmasPath)
        lemmasFile.listFiles()?.sortedBy {
            it.name
        }?.forEachIndexed() { i, file ->
            file.readLines().forEach {
                val s = it.split(":")
                if (s.isEmpty()) return@forEach
                val lemma = s[0]
                lemmasWithIndex[lemma]?.add(i) ?: run {
                    lemmasWithIndex[lemma] = mutableSetOf(i)
                }
            }
        }
        return lemmasWithIndex
    }
}