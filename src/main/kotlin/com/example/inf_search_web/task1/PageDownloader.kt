package task1

import Constants
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PageDownloader(
    val url: String,
    val pagesLimit : Int
) {

    fun execute() {
        val output = File(Constants.resultPath)
        output.mkdirs()
        output.listFiles()?.forEach { it.deleteRecursively() }
        val indexFile = File(output, "index.txt")
        indexFile.createNewFile()
        val pagesPackage = File(output, "выкачка").apply { mkdirs() }

        val indexBuilder = StringBuilder()
        crawl(url, pagesLimit)
            .forEachIndexed { index, page ->
                File(pagesPackage, "выкачка $index.html").run {
                    createNewFile()
                    writeText(page.pageText)
                }
                indexBuilder.appendLine("$index: ${page.url}")
            }
        FileOutputStream(indexFile, true).bufferedWriter()
            .use { it.append(indexBuilder.toString()) }
    }

    private fun crawl(url: String, pagesLimit: Int): List<Page> {
        val visitedPages = mutableSetOf<String>()
        val foundUrls = LinkedList<String>()
        val resultPages: MutableList<Page> = mutableListOf()
        foundUrls.add(url)
        while (resultPages.size < pagesLimit) {
            val urlToCrawl = foundUrls.first()
            if (!visitedPages.contains(urlToCrawl)) {
                val result = crawlUrl(urlToCrawl, visitedPages.size)
                if (result != null) {
                    resultPages.add(result.page)
                    foundUrls.addAll(result.foundPageUrls)
                }
                visitedPages.add(urlToCrawl)
            }
            foundUrls.removeAll {
                it == urlToCrawl
            }
        }

        println("Downloaded completed successful")
        return resultPages
    }

    private fun crawlUrl(url: String, number: Int): PageCrawlResult? {
        try {
            val connection = Jsoup.connect(url)
            val htmlDocument = connection.get()

            val response = connection.response()
            if (response.statusCode() != 200 ||
                !response.contentType().contains("text/html") ||
                htmlDocument.text().length < 500
            ) {
                return null
            }
            println("Downloaded $number page:$url")

            val foundUrls = htmlDocument.select("a[href]")
                .map {
                    it.absUrl("href").split('#')[0]
                }

            return PageCrawlResult(
                foundUrls,
                Page(url, htmlDocument.html())
            )

        } catch (e: IOException) {
            println(e)
            return null
        }
    }
}