package com.productinventory.ui.user.userDashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.gson.Gson
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityBarcodeScanBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.viewmodel.UserViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.showSnackBarToast

class ScanQRCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var context: Context
    private val viewModel: UserViewModel by viewModels()
    private var beepManager: BeepManager? = null
    private var torchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@ScanQRCodeActivity

        binding.barcodeView.setTorchListener(torchListener)
        binding.barcodeView.decodeSingle(callback)
        beepManager = BeepManager(this)

        setObserver()
        binding.imgCancel.setOnClickListener { finish() }
    }

    override fun onResume() {
        binding.barcodeView.resume()
        super.onResume()
    }

    override fun onPause() {
        binding.barcodeView.pause()
        torchOn = false
        super.onPause()
    }

    /**
     * torch on and off listner
     */
    private var torchListener = object : DecoratedBarcodeView.TorchListener {
        override fun onTorchOn() {
            torchOn = true
        }

        override fun onTorchOff() {
            torchOn = false
        }
    }

    /**
     * call back after scan QA code and get the result
     */
    private var callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text != null) {
                val shouldBeep: Boolean =
                    androidx.preference.PreferenceManager
                        .getDefaultSharedPreferences(context.applicationContext)
                        .getBoolean("beep", true)
                beepManager!!.isBeepEnabled = shouldBeep
                beepManager!!.isVibrateEnabled = false
                if (shouldBeep) {
                    beepManager!!.playBeepSoundAndVibrate()
                }
                MyLog.e("QrDoce",result.text)
                if (result.text.isNotBlank() && result.text.length == 4) {
                    // call API fetch data with match barcode
                    viewModel.getProductDetail(result.text.trim())
                } else {
                    binding.root.showSnackBarToast(getString(R.string.invalid_code))
                }
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint?>?) {
            // resultPoints
        }
    }

    /**
     * set observe  (API calling)mm/
     */
    private fun setObserver() {
        viewModel.getProductDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        startActivity(
                            Intent(context, ProductDetailsActivity::class.java).putExtra(
                                Constants.INTENT_PRODUCT_DATA,
                                Gson().toJson(it)
                            )
                        )
                        finish()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }
}
