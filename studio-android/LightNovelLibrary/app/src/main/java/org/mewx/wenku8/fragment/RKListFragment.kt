@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.component.PagerSlidingTabStrip

/**
 * Parent fragment that hosts ranked novel list pages in a pager.
 */
class RKListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rklist, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity()
        val pager = activity.findViewById<ViewPager>(R.id.rklist_pager)
        pager.adapter = MyPagerAdapter(childFragmentManager)

        val tabs = activity.findViewById<PagerSlidingTabStrip>(R.id.rklist_tabs)
        tabs.setViewPager(pager)

        val pageMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            4f,
            resources.displayMetrics
        ).toInt()
        pager.pageMargin = pageMargin

        val adapter = MyPagerAdapter(childFragmentManager)
        pager.adapter = adapter
    }

    inner class MyPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        private val titleList = arrayOf(
            Wenku8API.NovelSortedBy.allVisit,
            Wenku8API.NovelSortedBy.allVote,
            Wenku8API.NovelSortedBy.monthVisit,
            Wenku8API.NovelSortedBy.monthVote,
            Wenku8API.NovelSortedBy.weekVisit,
            Wenku8API.NovelSortedBy.weekVote,
            Wenku8API.NovelSortedBy.dayVisit,
            Wenku8API.NovelSortedBy.dayVote,
            Wenku8API.NovelSortedBy.postDate,
            Wenku8API.NovelSortedBy.goodNum,
            Wenku8API.NovelSortedBy.size,
            Wenku8API.NovelSortedBy.fullFlag
        )

        override fun getPageTitle(position: Int): CharSequence =
            resources.getString(getNovelSortedByChsId(titleList[position]))

        override fun getCount(): Int = titleList.size

        override fun getItem(type: Int): Fragment {
            val bundle = Bundle().apply {
                putString("type", Wenku8API.getNovelSortedBy(titleList[type]))
            }
            return NovelItemListFragment.newInstance(bundle)
        }

        private fun getNovelSortedByChsId(sortedBy: Wenku8API.NovelSortedBy): Int =
            when (sortedBy) {
                Wenku8API.NovelSortedBy.allVisit -> R.string.tab_allvisit
                Wenku8API.NovelSortedBy.allVote -> R.string.tab_allvote
                Wenku8API.NovelSortedBy.monthVisit -> R.string.tab_monthvisit
                Wenku8API.NovelSortedBy.monthVote -> R.string.tab_monthvote
                Wenku8API.NovelSortedBy.weekVisit -> R.string.tab_weekvisit
                Wenku8API.NovelSortedBy.weekVote -> R.string.tab_weekvote
                Wenku8API.NovelSortedBy.dayVisit -> R.string.tab_dayvisit
                Wenku8API.NovelSortedBy.dayVote -> R.string.tab_dayvote
                Wenku8API.NovelSortedBy.postDate -> R.string.tab_postdate
                Wenku8API.NovelSortedBy.lastUpdate -> R.string.tab_lastupdate
                Wenku8API.NovelSortedBy.goodNum -> R.string.tab_goodnum
                Wenku8API.NovelSortedBy.size -> R.string.tab_size
                Wenku8API.NovelSortedBy.fullFlag -> R.string.tab_fullflag
            }
    }
}
