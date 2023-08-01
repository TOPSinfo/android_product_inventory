package com.productinventory.ui.user.model.product

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object ProductList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getProductArrayList(querySnapshot: QuerySnapshot): ArrayList<ProductModel> {
        val cmsArrayList = ArrayList<ProductModel>()
        for (doc in querySnapshot.documents) {
            val productModel = ProductModel()

            doc.get(Constants.FIELD_PRODUCT_ID)?.let {
                productModel.id = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_UID)?.let {
                productModel.dealeruid = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_BARCODE)?.let {
                productModel.barcode = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_CATEGORY_ID)?.let {
                productModel.categoryId = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_CATEGORY)?.let {
                productModel.category = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_DESC)?.let {
                productModel.fullDescription = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_IMG)?.let {
                productModel.image = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_NAME)?.let {
                productModel.name = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_FULL_DESC)?.let {
                productModel.fullDescription = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_PRICE)?.let {
                productModel.price = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_DEALER_ID)?.let {
                productModel.dealerId = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_POPULAR)?.let {
                productModel.ispopularproduct = it as Boolean
            }
            doc.get(Constants.FIELD_PRODUCT_IMG_LIST)?.let {
                productModel.imageList = it as ArrayList<String>
            }
            doc.get(Constants.FIELD_PRODUCT_CREATE_DATE)?.let {
                productModel.createdAt = it as Timestamp
            }
            cmsArrayList.add(productModel)
        }
        return cmsArrayList
    }

    fun getProductData(doc: DocumentSnapshot): ArrayList<ProductModel> {
        val cmsArrayList = ArrayList<ProductModel>()

        val productModel = ProductModel()

        doc.get(Constants.FIELD_DEALER_UID)?.let {
            productModel.dealeruid = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_ID)?.let {
            productModel.id = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_BARCODE)?.let {
            productModel.barcode = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_CATEGORY_ID)?.let {
            productModel.categoryId = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_CATEGORY)?.let {
            productModel.category = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_IMG)?.let {
            productModel.image = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_NAME)?.let {
            productModel.name = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_FULL_DESC)?.let {
            productModel.fullDescription = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_DEALER_ID)?.let {
            productModel.dealerId = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_PRICE)?.let {
            productModel.price = it.toString()
        }
        doc.get(Constants.FIELD_PRODUCT_POPULAR)?.let {
            productModel.ispopularproduct = it as Boolean
        }
        doc.get(Constants.FIELD_PRODUCT_CREATE_DATE)?.let {
            productModel.createdAt = it as Timestamp
        }
        doc.get(Constants.FIELD_PRODUCT_IMG_LIST)?.let {
            productModel.imageList = it as ArrayList<String>
        }
        productModel.id = doc.id
        cmsArrayList.add(productModel)

        return cmsArrayList
    }

    fun getProductDetail(querySnapshot: QuerySnapshot): ProductModel {

        val productModel = ProductModel()

        for (doc in querySnapshot.documents) {

            doc.get(Constants.FIELD_DEALER_UID)?.let {
                productModel.dealeruid = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_ID)?.let {
                productModel.id = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_BARCODE)?.let {
                productModel.barcode = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_CATEGORY_ID)?.let {
                productModel.categoryId = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_CATEGORY)?.let {
                productModel.category = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_IMG)?.let {
                productModel.image = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_NAME)?.let {
                productModel.name = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_FULL_DESC)?.let {
                productModel.fullDescription = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_IMG_LIST)?.let {
                productModel.imageList = it as ArrayList<String>
            }
            doc.get(Constants.FIELD_PRODUCT_CREATE_DATE)?.let {
                productModel.createdAt = it as Timestamp
            }
            doc.get(Constants.FIELD_PRODUCT_DEALER_ID)?.let {
                productModel.dealerId = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_POPULAR)?.let {
                productModel.ispopularproduct = it as Boolean
            }
            doc.get(Constants.FIELD_PRODUCT_PRICE)?.let {
                productModel.price = it.toString()
            }
        }
        return productModel
    }
}
