package com.productinventory.ui.user.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.R
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentWalletBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.userDashboard.PaymentActivity
import com.productinventory.ui.user.userDashboard.UserDashBoardActivity
import com.productinventory.ui.user.viewmodel.ProfileViewModel
import com.productinventory.ui.user.viewmodel.WalletViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.getAmount
import com.productinventory.utils.loadResourceImage
import com.productinventory.utils.showSnackBarToast

class WalletFragment : BaseFragment() {

    private lateinit var binding: FragmentWalletBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private var userModel: UserModel = UserModel()
    private var contexte: UserDashBoardActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as UserDashBoardActivity
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                init()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        binding.imgWalletBg.loadResourceImage(R.mipmap.wallet_bg)
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        // set number input explicitly because allow to add minus, space and special char
        binding.edAmount.inputType = InputType.TYPE_CLASS_NUMBER
        binding.edAmount.text.clear()
    }

    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.userDetailResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        binding.tvAmount.text = userModel.walletbalance?.toDouble().toString()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        walletViewModel.walletDataResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        binding.root.showSnackBarToast(it)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.tv10.setOnClickListener {
            binding.edAmount.setText(getString(R.string._10).getAmount())
            binding.edAmount.setSelection(binding.edAmount.text.length)
        }
        binding.tv15.setOnClickListener {
            binding.edAmount.setText(getString(R.string._15).getAmount())
            binding.edAmount.setSelection(binding.edAmount.text.length)
        }
        binding.tv20.setOnClickListener {
            binding.edAmount.setText(getString(R.string._20).getAmount())
            binding.edAmount.setSelection(binding.edAmount.text.length)
        }
        binding.tv50.setOnClickListener {
            binding.edAmount.setText(getString(R.string._50).getAmount())
            binding.edAmount.setSelection(binding.edAmount.text.length)
        }
        binding.tv500.setOnClickListener {
            binding.edAmount.setText(getString(R.string._500).getAmount())
            binding.edAmount.setSelection(binding.edAmount.text.length)
        }
        binding.tv5000.setOnClickListener {
            binding.edAmount.setText(getString(R.string._5000).getAmount())
            binding.edAmount.setSelection(binding.edAmount.text.length)
        }
        binding.btnSubmit.setOnClickListener {
            if (checkValidation()) {
                redirectToPayment(binding.edAmount.text.toString().trim())
            }
        }
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
    }

    /** open payment page
     **/
    private fun redirectToPayment(amount: String) {
        startForResult.launch(
            Intent(context, PaymentActivity::class.java).putExtra(
                Constants.INTENT_AMOUNT,
                amount.getAmount()
            )
                .putExtra(Constants.INTENT_MODEL, DealerUserModel())
                .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)
        )
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {
        if (TextUtils.isEmpty(binding.edAmount.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_add_or_select_amount))
            return false
        } else if (binding.edAmount.text.toString().toInt() <= 0) {
            binding.root.showSnackBarToast(getString(R.string.please_add_valid_amount))
            return false
        }
        return true
    }
}
