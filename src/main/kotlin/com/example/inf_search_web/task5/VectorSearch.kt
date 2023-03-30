package task5

import Constants
import com.example.inf_search_web.task5.ResultUrl
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class VectorSearch {

    fun execute(textToSearch: String): List<ResultUrl> {
        val lemmas = File(Constants.tfIdfLemmasPath).listFiles()?.sortedBy {
            it.name
        }
        val lemmasTfIdf: Map<Int, Map<String, Double>> = mutableMapOf<Int, Map<String, Double>>().apply {
            putAll(
                lemmas?.mapIndexed { i, file ->
                    Pair(Integer.parseInt(file.name.split(".")[1]), mutableMapOf<String, Double>().apply {
                        putAll(file.readLines().map {
                            val s = it.split(" ")
                            Pair(s[0], s[2].toDouble())
                        })
                    })
                } ?: listOf()
            )
        }
        val lemmasSet = mutableSetOf<String>()
        lemmasTfIdf.forEach { entry ->
            entry.value.forEach {
                lemmasSet.add(it.key)
            }
        }

        val lemmaToListIndex = hashMapOf<String, Int>().apply {
            putAll(
                lemmasSet.mapIndexed { index, s ->
                    Pair(s, index)
                }
            )
        }

        val matrix = lemmasTfIdf.map {
            val list = ArrayList<Double>(Collections.nCopies(lemmasSet.size, 0.0))
            it.value.forEach { (lemma, tfId) ->
                list[lemmaToListIndex[lemma]!!] = tfId
            }
            Pair(it.key, list)
        }

        val search = Utils.getLemmasFromTokens(textToSearch.split(" ").toList())
        println("Result lemmas in search text : $search")
        val searchVector = ArrayList<Double>(Collections.nCopies(lemmasSet.size, 0.0))
        var hasValue = false
        search.forEach {
            lemmaToListIndex[it.key]?.let { index ->
                searchVector[index] = 1.0
                hasValue = true
            }
        }
        if(!hasValue) return listOf()

        val indexFile = mutableMapOf<Int, String>().apply {
            putAll(File(Constants.resultPath + "/" + "index.txt").readLines().map { s ->
                Pair(Integer.parseInt(s.substring(0, s.indexOf(":"))), s.substring(s.indexOf(":")+1))
            })
        }

        val cosinSimilarity = matrix.map { vector ->
            val product = vector.second.mapIndexed { index, d ->
                d * searchVector[index]
            }.sum()
            val lengthVectorPage = sqrt(vector.second.map {
                it * it
            }.sum())
            val lenghtVectorSearch = sqrt(searchVector.map {
                it * it
            }.sum())
            Pair(indexFile[vector.first], product / (lenghtVectorSearch * lengthVectorPage))
        }.filter {
            it.second != 0.0
        }.toMutableList()

        cosinSimilarity.sortByDescending {
            it.second
        }

        println(cosinSimilarity)
        return cosinSimilarity.map {
            ResultUrl(it.first!!, it.second)
        }
    }
}