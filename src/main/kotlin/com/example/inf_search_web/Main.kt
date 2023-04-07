import task1.PageDownloader
import task2.Tokenization
import task3.BooleanSearch
import task4.TfIdfCalculator
import task5.VectorSearch

fun main() {
    val url = "https://habr.com/ru/"
    PageDownloader(url, 100).execute()
    Tokenization().execute()
    BooleanSearch().makeInvertedList()
    TfIdfCalculator().execute()
    VectorSearch().execute("Содержимое")
}
