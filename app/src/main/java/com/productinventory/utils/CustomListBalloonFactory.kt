/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.productinventory.utils

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.productinventory.R
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec

/**
 * CustomListBalloonFactory: to show popup dialog set value as per your requirement
 */
class CustomListBalloonFactory : Balloon.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
        return Balloon.Builder(context)
            .setLayout(R.layout.layout_custom_list)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(Constants.INT_250)
            .setArrowOrientation(ArrowOrientation.TOP)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowColorResource(R.color.edittext_bg)
            .setArrowPosition(Constants.FLOAT_0_5)
            .setArrowSize(Constants.INT_10)
            .setTextSize(Constants.LONG_12)
            .setCornerRadius(Constants.FLOAT_6)
            .setMarginRight(Constants.INT_12)
            .setElevation(Constants.INT_6)
            .setBackgroundColorResource(R.color.white)
            .setBalloonAnimation(BalloonAnimation.FADE)
            .setIsVisibleOverlay(false)
            .setOverlayColorResource(R.color.grey)
            .setOverlayPadding(Constants.FLOAT_12_5)
            .setDismissWhenShowAgain(true)
            .setDismissWhenTouchOutside(true)
            .setDismissWhenOverlayClicked(false)
            .setLifecycleOwner(lifecycle)
            .build()
    }
}
