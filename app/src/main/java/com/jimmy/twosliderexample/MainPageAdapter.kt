package com.jimmy.twosliderexample

import android.view.View
import androidx.viewpager.widget.PagerAdapter

/**
 * @author jimmy
 * Created 2022/10/3 at 5:04 PM
 */
class MainPageAdapter : PagerAdapter() {
    override fun getCount(): Int {
        return 4
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        TODO("Not yet implemented")
    }

}