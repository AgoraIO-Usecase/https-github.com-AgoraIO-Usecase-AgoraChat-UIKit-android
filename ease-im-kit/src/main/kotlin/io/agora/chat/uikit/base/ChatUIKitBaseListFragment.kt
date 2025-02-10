package io.agora.chat.uikit.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isNotEmpty
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.databinding.UikitFragmentBaseListBinding
import io.agora.chat.uikit.feature.group.interfaces.ISearchResultListener
import io.agora.chat.uikit.feature.search.ChatUIKitSearchActivity
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.interfaces.OnItemClickListener
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.launch

abstract class ChatUIKitBaseListFragment<T>:ChatUIKitBaseFragment<UikitFragmentBaseListBinding>(), OnItemClickListener {
    lateinit var srlContactRefresh:SmartRefreshLayout
    var mRecyclerView: RecyclerView? = null
    lateinit var mListAdapter: ChatUIKitBaseRecyclerViewAdapter<T>
    protected lateinit var concatAdapter: ConcatAdapter
    private var searchResultListener: ISearchResultListener? = null

    companion object{
        const val KEY_USER = "user"
        const val KEY_SELECT_USER = "select_user"
    }

    private val returnSearchClickResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onClickResult(result) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.let {
            srlContactRefresh = it.srlContactRefresh
            val refreshHeader = it.srlContactRefresh.refreshHeader
            if (refreshHeader == null) {
                it.srlContactRefresh.setRefreshHeader(RefreshHeader(context))
            }
            mRecyclerView = if (initRecyclerView()?.isNotEmpty() == true){
                initRecyclerView()
            }else{
                it.rvList
            }
        }
        mRecyclerView?.layoutManager = getLayoutManager()
        concatAdapter = ConcatAdapter()
        addHeader(concatAdapter)
        mListAdapter = initAdapter()
        concatAdapter.addAdapter(mListAdapter)
        mRecyclerView?.adapter = concatAdapter
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentBaseListBinding {
        return UikitFragmentBaseListBinding.inflate(inflater)
    }

    override fun initListener() {
        super.initListener()
        mListAdapter.setOnItemClickListener(this)
        srlContactRefresh.setOnRefreshListener {
            refreshData()
        }
        binding?.searchBar?.setOnClickListener {
            returnSearchClickResult.launch(
                ChatUIKitSearchActivity.createIntent(
                    context = mContext,
                    searchType = ChatUIKitSearchType.SELECT_USER
                )
            )
        }
    }

    private fun onClickResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(KEY_SELECT_USER)?.let { selectMembers->
                if (selectMembers.isNotEmpty()){
                    searchResultListener?.onSearchResultListener(selectMembers.toMutableList())
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mRecyclerView?.adapter = concatAdapter
    }

    fun setSearchViewVisible(visible: Boolean){
        if (visible){
            binding?.searchBar?.visibility = View.VISIBLE
        }else{
            binding?.searchBar?.visibility = View.GONE
        }
    }

    fun setSearchResultListener(listener: ISearchResultListener){
        this.searchResultListener = listener
    }

    /**
     * Can add header adapters
     * @param adapter
     */
    open fun addHeader(adapter: ConcatAdapter) {
        // Add header adapter by adapter
    }

    /**
     * Can change the RecyclerView's orientation
     * @return
     */
    protected open fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(mContext)
    }

    /**
     * Must initialize the RecyclerView
     * @return
     */
    protected abstract fun initRecyclerView(): RecyclerView?

    /**
     * Must provide the list adapter
     * @return
     */
    protected abstract fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<T>


    protected abstract fun refreshData()


    override fun onItemClick(view: View?, position: Int) {

    }

    fun finishRefresh() {
        lifecycleScope.launch {
            if (srlContactRefresh.isNotEmpty()) {
                srlContactRefresh.finishRefresh()
            }
        }
    }
}