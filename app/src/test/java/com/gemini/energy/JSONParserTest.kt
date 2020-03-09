package com.gemini.energy

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

class JSONParserTest {

    @Test
    @Throws(Exception::class)
    fun readJSON() {
        val json = this.javaClass.getResourceAsStream("sample.json")
                .bufferedReader().use { it.readText() }

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        val jsonAdapter = moshi.adapter(Array<Product>::class.java)
        val products = jsonAdapter.fromJson(json)!!

        for (p in products) {
            println("${p.section}")

            p.elements?.forEach { it ->
                println("${it.price} -- ${it.productname}")
            }
        }
    }


    class Product {
        var section: String? = null
        var elements: List<Elements>? = null
    }

    class Elements {
        var productname: String? = null
        var price: String? = null
    }
}
