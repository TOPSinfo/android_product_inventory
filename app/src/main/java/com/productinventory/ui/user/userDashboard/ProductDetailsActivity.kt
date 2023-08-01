package com.productinventory.ui.user.userDashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.gson.Gson
import com.productinventory.R
import com.productinventory.databinding.FragmentProductDetailsBinding
import com.productinventory.ui.user.fragment.ViewPagerFeedFragment
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var binding: FragmentProductDetailsBinding
    private lateinit var context: Context
    private var productDetailsModel: ProductModel = ProductModel()
    private var images: ArrayList<String> = ArrayList()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // no action
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@ProductDetailsActivity

        productDetailsModel = Gson().fromJson(
            intent.getStringExtra(Constants.INTENT_PRODUCT_DATA).also { it!! },
            ProductModel::class.java
        )

        setUi(productDetailsModel)

        binding.imgBack.setOnClickListener { finish() }
        binding.btnBookNow.setOnClickListener {
            startForResult.launch(
                Intent(context, EventBookingActivity::class.java)
                    .putExtra(Constants.INTENT_ISEDIT, false)
                    .putExtra(Constants.INTENT_MODEL, productDetailsModel)
            )
        }
    }

    /** set data
     **/
    @SuppressLint("SetTextI18n")
    private fun setUi(productDetailsModel: ProductModel) {

        binding.txtProductName.text = productDetailsModel.name
        binding.tvDealerHeader.text = productDetailsModel.category
        binding.txtFullDesc.text = productDetailsModel.fullDescription
        binding.txtShortDesc.text = getString(R.string.rupee) + " " + productDetailsModel.price

        images.addAll(productDetailsModel.imageList)

        val pagerAdapter = ViewPagerAdapter(supportFragmentManager, images)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.currentItem = 0

        binding.indicatorView.apply {
            setSliderColor(
                context!!.getColor(R.color.swipe_img_indicatior),
                context!!.getColor(R.color.user_theme)
            )
            setSliderWidth(resources.getDimension(R.dimen._10sdp))
            setSliderHeight(resources.getDimension(R.dimen._4sdp))
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setupWithViewPager(binding.viewPager)
        }
    }

    /** set viewpager
     **/
    internal class ViewPagerAdapter(fm: FragmentManager?, var images: ArrayList<String>) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            var fragment: Fragment? = null
            if (position < images.size) {
                fragment = ViewPagerFeedFragment.newInstance(images[position])
            }
            return fragment!!
        }

        override fun getCount(): Int {
            return images.size
        }
    }
}
