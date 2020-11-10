package com.jess.eaiclubnasa.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jess.eaiclubnasa.repository.ApodRepository
import com.jess.eaiclubnasa.usecase.ApodUseCase
import com.jess.eaiclubnasa.viewmodel.event.ApodEvent
import com.jess.eaiclubnasa.viewmodel.interactor.ApodInteractor
import com.jess.eaiclubnasa.viewmodel.state.ApodState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApodViewModel(
    private val ioDispatcher: CoroutineDispatcher,
    repository: ApodRepository
) : ViewModel() {
    private var state: MutableLiveData<ApodState> = MutableLiveData()
    private var event: MutableLiveData<ApodEvent> = MutableLiveData()
    private val useCase = ApodUseCase(repository)
    val viewState = state
    val viewEvent = event

    fun interpret(interactor: ApodInteractor) {
        when (interactor) {
            is ApodInteractor.GetListApod -> getListApod(interactor.date)
        }
    }

    private fun getListApod(date: String) {
        viewModelScope.launch {
            event.value = ApodEvent.Loading(true)
            try {
                val listApodResult = withContext(ioDispatcher) {
                    useCase.getApodList(date)
                }
                state.value = ApodState.ApodListSuccess(listApodResult)
                event.value = ApodEvent.Loading(false)
            } catch (ex: Exception) {
                state.value =
                    ApodState.ApodListError("Ops! Parece que tivemos algum problema =/\nTente novamente!")
            }
        }
    }
}