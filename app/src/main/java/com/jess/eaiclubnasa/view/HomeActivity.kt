package com.jess.eaiclubnasa.view

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jess.eaiclubnasa.R
import com.jess.eaiclubnasa.model.ApodResult
import com.jess.eaiclubnasa.repository.ApodRepository
import com.jess.eaiclubnasa.viewmodel.ApodViewModel
import com.jess.eaiclubnasa.viewmodel.ApodViewModelFactory
import com.jess.eaiclubnasa.viewmodel.event.ApodEvent
import com.jess.eaiclubnasa.viewmodel.interactor.ApodInteractor
import com.jess.eaiclubnasa.viewmodel.state.ApodState
import kotlinx.coroutines.Dispatchers

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerViewApod: RecyclerView
    private lateinit var loading: ProgressBar
    private var listApodResult: MutableList<ApodResult> = mutableListOf()
    private val viewModel: ApodViewModel by lazy {
        val factory = ApodViewModelFactory(Dispatchers.IO, ApodRepository())
        ViewModelProvider(this, factory).get(ApodViewModel::class.java)
    }

    private val adapter: ApodAdapter by lazy {
        ApodAdapter(
            ArrayList()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initViews()
        initViewModel()
        scrollPaginationList()
    }

    private fun initViews() {
        recyclerViewApod = findViewById(R.id.rv_apod)
        loading = findViewById(R.id.loading)
    }

    private fun initViewModel() {
        viewModel.viewState.observe(this, Observer { state ->
            state?.let {
                when (it) {
                    is ApodState.ApodListSuccess -> showList(it.list)
                }
            }
        })

        viewModel.viewEvent.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is ApodEvent.Loading -> showLoading(it.status)
                }
            }
        })

        viewModel.interpret(ApodInteractor.GetListApod("2020-10-10"))
    }

    private fun showList(listApod: MutableList<ApodResult>) {
        listApodResult = listApod
        adapter.update(listApod)
        recyclerViewApod.adapter = adapter
        recyclerViewApod.layoutManager = LinearLayoutManager(this)
    }

    private fun scrollPaginationList() {

        recyclerViewApod.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                @NonNull recyclerView: RecyclerView, dx: Int, dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val totalItemCount: Int = layoutManager?.itemCount ?: 0
                val lastVisible: Int = layoutManager?.findLastVisibleItemPosition() ?: 0
                val ultimoItem = lastVisible + 5 >= totalItemCount

                if (totalItemCount > 0 && ultimoItem &&
                    viewModel.viewEvent.value == ApodEvent.Loading(false)
                ) {
                    viewModel.interpret(ApodInteractor.GetListApod(listApodResult[5].date))
                }
            }
        })
    }

    private fun showLoading(status: Boolean) {
        when {
            status -> {
                loading.visibility = View.VISIBLE
            }
            else -> {
                loading.visibility = View.GONE
            }
        }
    }
}