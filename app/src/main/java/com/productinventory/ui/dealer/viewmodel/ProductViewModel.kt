package com.productinventory.ui.dealer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.ProductRepository
import com.productinventory.ui.user.model.product.ProductList
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _productAddUpdateResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val productAddUpdateResponse: LiveData<Resource<String>> get() = _productAddUpdateResponse

    private val _productDataResponse: MutableLiveData<Resource<ArrayList<ProductModel>>> =
        MutableLiveData()
    val productDataResponse: LiveData<Resource<ArrayList<ProductModel>>> get() = _productDataResponse

    private val _productDeleteResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val productDeleteResponse: LiveData<Resource<String>> get() = _productDeleteResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun addUpdateBookingData(product: ProductModel, isForUpdate: Boolean) {

        _productAddUpdateResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {
            if (isForUpdate) {
                updateProductData(product)
            } else {
                addProductData(product)
            }
        } else {
            _productAddUpdateResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Adding booking info in firebase
     */
    private fun addProductData(product: ProductModel) {
        _productAddUpdateResponse.value = Resource.loading(null)
        val ref = productRepository.getProductAddRepository()
        product.id = ref.id
        product.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_DEALER_UID to product.dealeruid,
            Constants.FIELD_PRODUCT_BARCODE to product.id.substring(product.id.length - 4, product.id.length),
            /*Constants.FIELD_PRODUCT_BARCODE to product.id.substring(product.id.length - 4, product.id.length).uppercase(
                Locale.getDefault()),*/
            Constants.FIELD_PRODUCT_ID to product.id,
            Constants.FIELD_PRODUCT_CATEGORY_ID to product.categoryId,
            Constants.FIELD_PRODUCT_CATEGORY to product.category,
            Constants.FIELD_PRODUCT_FULL_DESC to product.fullDescription,
            Constants.FIELD_PRODUCT_IMG to product.image,
            Constants.FIELD_PRODUCT_IMG_LIST to product.imageList,
            Constants.FIELD_PRODUCT_NAME to product.name,
            Constants.FIELD_PRODUCT_DEALER_ID to product.dealerId,
            Constants.FIELD_PRODUCT_PRICE to product.price,
            Constants.FIELD_PRODUCT_POPULAR to product.ispopularproduct,
            Constants.FIELD_PRODUCT_CREATE_DATE to product.createdAt,
        )

        ref.set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                _productAddUpdateResponse.postValue(
                    Resource.success(
                        Constants.MSG_PRODUCT_ADD_SUCCESSFUL,
                    )
                )
            }
        }.addOnFailureListener {
            _productAddUpdateResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }

    /**
     * Updating booking info in firebase
     */
    private fun updateProductData(product: ProductModel) {

        _productAddUpdateResponse.value = Resource.loading(null)
        product.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        product.dealeruid.let { data1.put(Constants.FIELD_DEALER_UID, it) }
        product.barcode.let { data1.put(Constants.FIELD_PRODUCT_BARCODE, it) }
        product.id.let { data1.put(Constants.FIELD_PRODUCT_ID, it) }
        product.categoryId.let { data1.put(Constants.FIELD_PRODUCT_CATEGORY_ID, it) }
        product.category.let { data1.put(Constants.FIELD_PRODUCT_CATEGORY, it) }
        product.fullDescription.let { data1.put(Constants.FIELD_PRODUCT_FULL_DESC, it) }
        product.image.let { data1.put(Constants.FIELD_PRODUCT_IMG, it) }
        product.imageList.let { data1.put(Constants.FIELD_PRODUCT_IMG_LIST, it) }
        product.name.let { data1.put(Constants.FIELD_PRODUCT_NAME, it) }
        product.price.let { data1.put(Constants.FIELD_PRODUCT_PRICE, it) }
        product.dealerId.let { data1.put(Constants.FIELD_PRODUCT_DEALER_ID, it) }
        product.ispopularproduct.let { data1.put(Constants.FIELD_PRODUCT_POPULAR, it) }
        product.createdAt.let { data1.put(Constants.FIELD_PRODUCT_CREATE_DATE, it as Timestamp) }

        productRepository.getProductUpdateRepository(product).update(data1).addOnCompleteListener {
            if (it.isSuccessful) {
                _productAddUpdateResponse.postValue(
                    Resource.success(
                        Constants.MSG_PRODUCT_UPDATE_SUCCESSFUL,
                    )
                )
            }
        }.addOnFailureListener {

            _productAddUpdateResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }

    fun getProductByDealerCollection(dealerId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _productDataResponse.value = Resource.loading(null)
            productRepository.getProductByDealerCollection(dealerId).get().addOnSuccessListener {
                val mList = ProductList.getProductArrayList(it)
                _productDataResponse.postValue(Resource.success(mList))
            }
        } else _productDataResponse.postValue(
            Resource.error(
                Constants.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * delete Product slot
     */

    fun deleteProduct(productId: String) {

        if (networkHelper.isNetworkConnected()) {
            _productDeleteResponse.value = Resource.loading(null)
            val ref = productRepository.deleteProduct()

            ref.document(productId).delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _productDeleteResponse.value =
                            Resource.success(Constants.MSG_PRODUCT_DELETE_SUCCESSFUL)
                    }
                }
                .addOnFailureListener {
                    _productDeleteResponse.value = Resource.error(it.localizedMessage, null)
                }
        } else {
            _productDeleteResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }
}
