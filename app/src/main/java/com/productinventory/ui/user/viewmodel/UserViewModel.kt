package com.productinventory.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.UserRepository
import com.productinventory.ui.user.model.category.CategoriesList
import com.productinventory.ui.user.model.category.CategoriesModel
import com.productinventory.ui.user.model.product.ProductList
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.model.wishlist.WishList
import com.productinventory.ui.user.model.wishlist.WishListModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper,
) : ViewModel() {

    private val _productWishDataResponse: MutableLiveData<Resource<ArrayList<WishListModel>>> = MutableLiveData()
    val productWishDataResponse: LiveData<Resource<ArrayList<WishListModel>>>
        get() = _productWishDataResponse

    private val _productDataResponse: MutableLiveData<Resource<ArrayList<ProductModel>>> = MutableLiveData()
    val productDataResponse: LiveData<Resource<ArrayList<ProductModel>>> get() = _productDataResponse

    private val _categoriesDataResponse: MutableLiveData<Resource<ArrayList<CategoriesModel>>> = MutableLiveData()
    val categoriesDataResponse: LiveData<Resource<ArrayList<CategoriesModel>>> get() = _categoriesDataResponse

    private val _getProductDetailResponse: MutableLiveData<Resource<ProductModel>> = MutableLiveData()
    val getProductDetailResponse: LiveData<Resource<ProductModel>> get() = _getProductDetailResponse

    private val _popularProductListResponse: MutableLiveData<Resource<ArrayList<ProductModel>>> = MutableLiveData()
    val getPopularProductListResponse: LiveData<Resource<ArrayList<ProductModel>>> get() = _popularProductListResponse

    private val _arrivalProductListResponse: MutableLiveData<Resource<ArrayList<ProductModel>>> = MutableLiveData()
    val getArrivalProductListResponse: LiveData<Resource<ArrayList<ProductModel>>> get() = _arrivalProductListResponse

    private val _receiveGroupCallInvitationResponse: MutableLiveData<Resource<String>> = MutableLiveData()

    private val _productAddUpdateResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val productAddUpdateResponse: LiveData<Resource<String>> get() = _productAddUpdateResponse


    /** get list of product
     **/
    fun getProductData(id: String, dealerId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _productDataResponse.value = Resource.loading(null)
            userRepository.getProductCollection(id, dealerId).get().addOnSuccessListener {
                val mList = ProductList.getProductArrayList(it)
                _productDataResponse.postValue(Resource.success(mList))
            }
        } else _productDataResponse.postValue(Resource.error(Constants.MSG_NO_INTERNET_CONNECTION,
            null))
    }

    /** get list of product by search name
     **/
    fun getSearchData(categoryName: String, dealerId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {

            val query = FirebaseFirestore.getInstance().collection(Constants.TABLE_PRODUCTS)
                .whereEqualTo(Constants.FIELD_DEALER_ID, dealerId)
                .orderBy(Constants.FIELD_PRODUCT_NAME)

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _receiveGroupCallInvitationResponse.postValue(Resource.error("No Data",
                        "Failed to connect"))
                    return@addSnapshotListener
                }

                if (!snapshot!!.isEmpty) {
                    val mList: ArrayList<ProductModel> = ArrayList()
                    mList.clear()
                    for (dc in snapshot.documents) {
                        val str1: String = dc.getString(Constants.FIELD_PRODUCT_NAME)!!
                        if (str1.lowercase().filter { !it.isWhitespace() }
                                .contains(categoryName.lowercase().filter { !it.isWhitespace() },
                                    true)
                        ) {
                            mList.addAll(ProductList.getProductData(dc))
                        }
                    }
                    _productDataResponse.postValue(Resource.success(mList))
                }
            }
        } else {
            _receiveGroupCallInvitationResponse.postValue(Resource.error("",
                Constants.MSG_NO_INTERNET_CONNECTION))
        }
    }

    /** get list of Categories
     **/
    fun getCategoriesData() = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _categoriesDataResponse.value = Resource.loading(null)
            userRepository.getCategoriesCollection().get().addOnSuccessListener {
                val mList = CategoriesList.getCategoriesArrayList(it)
                _categoriesDataResponse.postValue(Resource.success(mList))
            }
        } else _categoriesDataResponse.postValue(Resource.error(Constants.MSG_NO_INTERNET_CONNECTION,
            null))
    }

    /**
     * Get all pending booking info  for normal user in firebase
     */
    fun getProductDetail(barcodeId: String) {

        if (networkHelper.isNetworkConnected()) {
            _getProductDetailResponse.value = Resource.loading(null)

            userRepository.getProductDetailsCollection(barcodeId).get().addOnSuccessListener {
                val productDetail = ProductList.getProductDetail(it)
                _getProductDetailResponse.postValue(Resource.success(productDetail))
            }
        } else {
            _getProductDetailResponse.postValue(Resource.error(Constants.MSG_NO_INTERNET_CONNECTION,
                null))
        }
    }

    /**
     * Get all pending booking info  for normal user in firebase
     */
    fun getProductDetailByDealerId(dealerId: String) {

        if (networkHelper.isNetworkConnected()) {
            _getProductDetailResponse.value = Resource.loading(null)

            userRepository.getProductDetailsByDealerCollection(dealerId).get()
                .addOnSuccessListener {
                    val productDetail = ProductList.getProductDetail(it)
                    _getProductDetailResponse.postValue(Resource.success(productDetail))
                }
        } else {
            _getProductDetailResponse.postValue(Resource.error(Constants.MSG_NO_INTERNET_CONNECTION,
                null))
        }
    }

    /**
     * Get New Popular ProductList
     **/
    fun getPopularProductList(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _popularProductListResponse.value = Resource.loading(null)
            val ref = userRepository.getPopularProduct(userId)

            ref.get().addOnSuccessListener {
                val list = ProductList.getProductArrayList(it)
                _popularProductListResponse.postValue(Resource.success(list))
            }.addOnFailureListener {
                _popularProductListResponse.value = Resource.error("", null)
            }
        } else {
            _popularProductListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION,
                null)
        }
    }

    /**
     * Get New Arrival ProductList
     **/
    fun getNewArrivalProductList(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _arrivalProductListResponse.value = Resource.loading(null)
            val ref = userRepository.getNewArrivalProduct(userId)

            ref.get().addOnSuccessListener {
                val list = ProductList.getProductArrayList(it)
                _arrivalProductListResponse.postValue(Resource.success(list))
            }.addOnFailureListener {
                _arrivalProductListResponse.value = Resource.error("", null)
            }
        } else {
            _arrivalProductListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION,
                null)
        }
    }

    /**
     * Adding user info in firebase
     */
    fun addWishData(product: ProductModel) {

        _productAddUpdateResponse.value = Resource.loading(null)
        val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

        val addUserDocument = userRepository.getWishDataRepository(userId)
        product.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_PRODUCT_BARCODE to product.id,
            Constants.FIELD_WISHIST_ID to addUserDocument.id,
            Constants.FIELD_PRODUCT_IDS to product.id,
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
        addUserDocument.set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                MyLog.e("TAG_ID", addUserDocument.id)
                _productAddUpdateResponse.postValue(Resource.success(addUserDocument.id))
            }
        }.addOnFailureListener {

            _productAddUpdateResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }

    /**
     * Get all transaction data of user
     */
    fun getAllWhishData(userId: String) {
        _productWishDataResponse.value = Resource.loading(null)
        userRepository.getAllWishDataByUser(userId).get().addOnSuccessListener {
            val mList = WishList.getProductArrayList(it)
            _productWishDataResponse.postValue(Resource.success(mList))
        }.addOnFailureListener {
            _productWishDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }

    fun deleteDocumentWishlist(productId: String) {

        val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
        val rootRef = FirebaseFirestore.getInstance()

        val itemsRef = rootRef.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_WISHLIST)
        val query: Query = itemsRef.whereEqualTo(Constants.FIELD_PRODUCT_IDS, productId)
        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result!!) {
                    itemsRef.document(document.id).delete()
                }
            } else {
                MyLog.e("DDDD", it.exception.toString())
            }
        }
    }
}
