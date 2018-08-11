package com.gemini.energy.service.crunch

import com.gemini.energy.domain.entity.Computable
import com.gemini.energy.service.DataHolder
import com.google.gson.JsonArray
import io.reactivex.functions.Function
import timber.log.Timber
import java.util.*

class EnergyPostState {

    class Mapper : Function<JsonArray, DataHolder> {
        lateinit var postStateFields: MutableList<String>
        lateinit var computable: Computable<*>
        lateinit var cost: (Double) -> Double

        override fun apply(response: JsonArray): DataHolder {

            Timber.d("##### Post-State Energy Calculation - (${thread()}) #####")
            Timber.d("### Efficient Alternate Count - [${response.count()}] - ###")

            val jsonElements = response.map { it.asJsonObject.get("data") }
            val dataHolderPostState = initDataHolder()
            val costCollector = mutableListOf<Double>()

            jsonElements.forEach { element ->
                val postRow = mutableMapOf<String, String>()
                postStateFields.forEach { key ->
                    var value = ""
                    if (element.asJsonObject.has(key)) {
                        value = element.asJsonObject.get(key).asString
                    }
                    postRow[key] = value
                }

                val postDailyEnergyUsed = element.asJsonObject.get("daily_energy_use").asDouble
                val cost = cost(postDailyEnergyUsed)
                postRow["__electric_cost"] = cost.toString()

                costCollector.add(cost)
                dataHolderPostState.rows?.add(postRow)
                computable.energyPostState?.add(postRow)
            }

            Timber.d("## Data Holder - POST STATE  - (${thread()}) ##")
            Timber.d(dataHolderPostState.toString())

            val costMinimum = costCollector.min()
            val efficientAlternative = dataHolderPostState.rows?.filter {
                it.getValue("__electric_cost").toDouble() == costMinimum
            }

            computable.energyPostStateLeastCost = efficientAlternative ?: mutableListOf()
            Timber.d("Minimum Cost : [$costMinimum]")
            Timber.d("Efficient Alternative : ${computable.energyPostStateLeastCost}")

            return dataHolderPostState
        }

        private fun initDataHolder(): DataHolder {
            val dataHolderPostState = DataHolder()
            dataHolderPostState.header = postStateFields
            dataHolderPostState.header?.add("__electric_cost")

            dataHolderPostState.computable = computable
            dataHolderPostState.fileName = "${Date().time}_post_state.csv"

            return dataHolderPostState
        }

        private fun thread() = Thread.currentThread().name

    }

}