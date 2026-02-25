package st.service.core_base_feature

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseScreenModelV2<S : Any, SE : Any, E : Any>(
    initialState: S,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatcher)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SE>(extraBufferCapacity = 64)
    val sideEffect: SharedFlow<SE> = _sideEffect.asSharedFlow()

    protected val currentState: S
        get() = _state.value

    abstract fun dispatch(event: E)

    open fun onFetchData() = Unit

    protected fun updateState(reducer: (S) -> S) {
        _state.value = reducer(_state.value)
    }

    protected suspend fun postSideEffect(effect: SE) {
        _sideEffect.emit(effect)
    }

    protected fun screenModelScope(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    open fun dispose() {
        scope.cancel()
    }
}
