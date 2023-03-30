package com.example.inf_search_web.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import task5.VectorSearch

@Controller
class InfSearchController {

    val search = VectorSearch()

    @GetMapping("/")
    fun home(model: Model): String {
        return "search"
    }

    @PostMapping("/search")
    fun search(@RequestParam("searchText") text: String, model: ModelMap): String {
        println(text)
        val res = search.execute(text)
        model.addAttribute("search", text)
        model.put("results", res)
//        model.addAttribute("results", items)
        return "search"
    }
}