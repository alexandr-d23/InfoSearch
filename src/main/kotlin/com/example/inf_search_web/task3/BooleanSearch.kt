package task3

import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*

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

    fun searchByOperations() {
        val words = mutableMapOf<String, List<String>>().also { map ->
            FileReader(File("src/main/kotlin/result/task3/inverted_list")).readLines()
                .forEach {
                    val s = it.split(":")
                    val key = s[0]
                    val list = s[1].split(" ").filter { st -> st.isNotEmpty() }
                    map.put(key, list)
                }
        }
        println("Булевы операции: \"&\", \"|\", \"-\"! Все должно быть только через пробел!")

        val sc = Scanner(System.`in`)
        val string = sc.nextLine()
        println(preparing(string, words))
    }

    fun preparing(condition: String, words: MutableMap<String, List<String>>): MutableList<List<String>> {

        var wordsSet = mutableListOf<List<String>>()
        var userSearch: List<String> = condition.split(" ").map {
            if (it == "&" || it == "|" || it == "-") {
                it
            } else {
                Utils.getLemmasFromTokens(listOf(it)).firstNotNullOf {
                    it.key
                }
            }
        }

        userSearch.forEach {
            if (words.contains(it)) {
                wordsSet.add(words.get(it)!!)
            } else if (it == "&" || it == "|" || it == "-") {
                wordsSet.add(listOf(it))
            } else {
                wordsSet.add(listOf())
            }
        }

        var a1: List<String>
        var a2: List<String>
        var i = 1


        while (i <= wordsSet.size - 2) {

            if (wordsSet.size >= 3) {

                a1 = wordsSet[i - 1]
                a2 = wordsSet[i + 1]

                if (wordsSet[i].contains("-")) {
                    wordsSet[i] = not(a1, a2)
                    wordsSet.removeAt(i + 1)
                    wordsSet.removeAt(i - 1)
                    i--
                } else if (wordsSet[i].contains("&")) {
                    wordsSet[i] = and(a1, a2)
                    wordsSet.removeAt(i + 1)
                    wordsSet.removeAt(i - 1)
                    i--
                } else if (wordsSet[i].contains("|")) {
                    wordsSet[i] = or(a1, a2)
                    wordsSet.removeAt(i + 1)
                    wordsSet.removeAt(i - 1)
                    i--
                }
            }
            i++
        }

        return wordsSet
    }


    fun and(a1: List<String>, a2: List<String>): List<String> {

        var list = mutableListOf<String>()

        a1.forEach {
            if (a2.contains(it))
                list.add(it)
        }
        return list
    }

    fun or(a1: List<String>, a2: List<String>): List<String> {

        var list = mutableListOf<String>()

        a1.forEach {
            if (!list.contains(it)) {
                list.add(it)
            }
        }
        a2.forEach {
            if (!list.contains(it)) {
                list.add(it)
            }
        }
        return list
    }

    fun not(a1: List<String>, a2: List<String>): List<String> {

        var list = mutableListOf<String>()

        a1.forEach {
            if (!a2.contains(it)) {
                list.add(it)
            }
        }
        return list
    }


}