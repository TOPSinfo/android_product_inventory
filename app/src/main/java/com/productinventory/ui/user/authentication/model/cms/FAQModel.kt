package com.productinventory.ui.user.authentication.model.cms

import java.io.Serializable

data class FAQModel(
    var id: String = "",
    var title: String = "",
    var answer: String = "",
) : Serializable{
    companion object {
        const val serialVersionUID: Long = 123
    }
}
