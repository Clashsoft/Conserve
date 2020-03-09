package com.gemini.energy.presentation.form.model

class GEnergyFormModel {
    var geminiForm: List<GFormBlock>? = null
}

class GFormBlock {
    var id: Int? = null
    var index: Int? = null
    var section: String? = null
    var elements: List<GElements>? = null
}

class GElements {
    var id: Int? = null
    var index: Int? = null
    var param: String? = null
    var dataType: String? = null
    var validation: String? = null
    var hint: String? = null
    var defaultValues: String? = null
}
