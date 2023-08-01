package com.productinventory.ui.user.model.category

import java.io.Serializable

data class CategoriesModel(
    var id: String = "",
    var name: String = "",
) : Serializable {
    companion object {
        const val serialVersionUID: Long = 123
    }
}
