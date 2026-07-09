@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.mewx.wenku8.R
import org.mewx.wenku8.adapter.SearchHistoryAdapter
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.listener.MyItemLongClickListener
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * Search activity.
 */
class SearchActivity : BaseMaterialActivity(), MyItemClickListener, MyItemLongClickListener {
    private lateinit var toolbarSearchView: EditText
    private var adapter: SearchHistoryAdapter? = null
    private var historyList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_search, StatusBarColor.WHITE)
        installFadeBackCallback()

        GoogleServicesHelper.initFirebase(this)

        toolbarSearchView = findViewById(R.id.search_view)
        val searchClearButton = findViewById<View>(R.id.search_clear)
        searchClearButton.setOnClickListener { toolbarSearchView.setText("") }

        val searchClearIcon = findViewById<ImageView>(R.id.search_clear_icon)
        searchClearIcon.setColorFilter(resources.getColor(R.color.mySearchToggleColor), PorterDuff.Mode.SRC_ATOP)

        val layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        val recyclerView = findViewById<RecyclerView>(R.id.search_history_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = layoutManager

        historyList = GlobalConfig.getSearchHistory()
        adapter = SearchHistoryAdapter(historyList).also {
            it.setOnItemClickListener(this)
            it.setOnItemLongClickListener(this)
        }
        recyclerView.adapter = adapter

        toolbarSearchView.setOnEditorActionListener { _, _, _ ->
            val searchText = toolbarSearchView.text.toString().trim()
            if (searchText.isEmpty()) {
                return@setOnEditorActionListener false
            }

            GlobalConfig.addSearchHistory(searchText)
            refreshHistoryList()

            val intent = Intent(this, SearchResultActivity::class.java).apply {
                putExtra("key", searchText)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.hold)

            false
        }
    }

    override fun onResume() {
        super.onResume()

        val upArrow = resources.getDrawable(R.drawable.ic_svg_back)
        if (supportActionBar != null) {
            upArrow.setColorFilter(resources.getColor(R.color.mySearchToggleColor), PorterDuff.Mode.SRC_ATOP)
            supportActionBar?.setHomeAsUpIndicator(upArrow)
        }

        refreshHistoryList()
    }

    private fun refreshHistoryList() {
        historyList = GlobalConfig.getSearchHistory()
        adapter?.notifyDataSetChanged()
    }

    override fun onItemClick(view: View?, position: Int) {
        if (position < 0 || position >= historyList.size) {
            Toast.makeText(
                this,
                "ArrayIndexOutOfBoundsException: $position in size ${historyList.size}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val history = historyList[position]
        toolbarSearchView.setText(history)
        toolbarSearchView.setSelection(history.length)
    }

    override fun onItemLongClick(view: View?, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.dialog_content_delete_one_search_record))
            .setMessage(historyList[position])
            .setPositiveButton(R.string.dialog_positive_likethis) { _, _ ->
                GlobalConfig.deleteSearchHistory(historyList[position])
                refreshHistoryList()
            }
            .setNegativeButton(R.string.dialog_negative_preferno, null)
            .show()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
