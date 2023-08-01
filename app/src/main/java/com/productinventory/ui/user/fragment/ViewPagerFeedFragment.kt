package com.productinventory.ui.user.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.productinventory.R
import com.productinventory.databinding.FragmentScreenSlidePageBinding
import com.productinventory.ui.user.userDashboard.ImageViewActivity

class ViewPagerFeedFragment : Fragment() {

    private lateinit var binding: FragmentScreenSlidePageBinding

    companion object {
        fun newInstance(img: String?): ViewPagerFeedFragment {
            val args = Bundle()
            args.putString("img", img)
            val fragment = ViewPagerFeedFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentScreenSlidePageBinding.inflate(inflater, container, false)

        val bundle = arguments
        Glide.with(this).load(bundle!!.getString("img")!!.replace("\\s", "").trim { it <= ' ' })
            .apply(RequestOptions.placeholderOf(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder).priority(Priority.IMMEDIATE)
                .diskCacheStrategy(DiskCacheStrategy.DATA))
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?,
                                          model: Any,
                                          target: Target<Drawable?>,
                                          isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?,
                                             model: Any,
                                             target: Target<Drawable?>,
                                             dataSource: DataSource,
                                             isFirstResource: Boolean): Boolean {
                    return false
                }
            }).into(binding.imgInstaFeedPager)

        binding.imgInstaFeedPager.setOnClickListener {
            redirectImageViewActivity(bundle.getString("img")!!.replace("\\s", "")
                .trim { it <= ' ' })
        }
        return binding.root
    }

    /**
     * Redirecting to full screen image view activity
     */
    fun redirectImageViewActivity(url: String) {
        val intent = Intent(context, ImageViewActivity::class.java)
        intent.putExtra("ImageUrl", url)
        context!!.startActivity(intent)
    }
}
