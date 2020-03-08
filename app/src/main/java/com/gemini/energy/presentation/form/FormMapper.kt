package com.gemini.energy.presentation.form

import android.content.Context
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.presentation.form.model.GElements
import com.gemini.energy.presentation.form.model.GEnergyFormModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class FormMapper(private val context: Context, private val rawId: Int?) {

    companion object {
        private const val TAG = "FormElementMapper"
    }

    fun decodeJSON(): GEnergyFormModel? {
        var formModel: GEnergyFormModel? = null

        if (rawId == -1) return null

        val json = context.resources.openRawResource(rawId!!)
                .bufferedReader().use { it.readText() }

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        val jsonAdapter = moshi.adapter<GEnergyFormModel>(GEnergyFormModel::class.java)

        try {
            formModel = jsonAdapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return formModel
    }

    fun mapSectionIdsToElements(model: GEnergyFormModel?) = Mapper(model).mapSIdToGElements
    fun mapSectionIdsToName(model: GEnergyFormModel?) = Mapper(model).mapSIdToSName
    fun mapIdToElements(model: GEnergyFormModel?) = Mapper(model).mapEIdToGElements
    fun mapElementIdToSectionName(model: GEnergyFormModel?) = Mapper(model).mapEIdToSName
    fun sortedElementIds(model: GEnergyFormModel?) = Sorter(Mapper(model).mapIndexSID).sortedIds
    fun sortedFormElementIds(model: GEnergyFormModel?) = Sorter(Mapper(model).mapIndexEID).sortedIds

    private class Sorter(private val mapIndex: HashMap<Int, Int>) {

        var sortedIds: List<Int> = listOf()
        private fun sortMap() {
            val sortedKeys = mapIndex.keys.sorted()
            sortedIds = sortedKeys.map { mapIndex.getValue(it) }
        }

        init { sortMap() }

    }


    private class Mapper(val model: GEnergyFormModel?) {

        var mapSIdToSName: HashMap<Int, String> = hashMapOf()
        var mapSIdToGElements: HashMap<Int, List<GElements>> = hashMapOf()
        var mapEIdToGElements: HashMap<Int, GElements> = hashMapOf()
        var mapEIdToSName: HashMap<Int, String> = hashMapOf()

        var mapIndexSID: HashMap<Int, Int> = hashMapOf()
        var mapIndexEID: HashMap<Int, Int> = hashMapOf()

        private fun mapSId() {
            val form = model?.geminiForm

            if (form == null) {
                Log.e(TAG, "Empty DTO Form")
                return
            }

            form.forEach { block ->

                val sectionId = block.id
                val index = block.index
                val gElements = block.elements
                val sectionName = block.section

                if (sectionId != null && index != null && gElements != null && sectionName != null) {

                    this.mapSIdToGElements[sectionId] = gElements
                    this.mapSIdToSName[sectionId] = sectionName
                    this.mapIndexSID[index] = sectionId

                    gElements.forEach { gElements ->

                        val elementId = gElements.id
                        val index = gElements.index

                        if (elementId != null && index != null) {
                            this.mapEIdToGElements[elementId] = gElements
                            this.mapIndexEID[index] = elementId
                            this.mapEIdToSName[elementId] = sectionName
                        }
                    }
                }
            }
        }

        init { mapSId() }
    }

}
