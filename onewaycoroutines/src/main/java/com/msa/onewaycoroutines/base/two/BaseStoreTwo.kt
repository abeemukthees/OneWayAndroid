package com.msa.onewaycoroutines.base.two

import android.util.Log
import com.msa.core.Action
import com.msa.core.State
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

internal class GetStateAction(
    val deferred: CompletableDeferred<State>
) : Action

class ExceededTimeLimitToComputeNewStatException(override val message: String) : Exception()

private fun <S : State> CoroutineScope.store(
    initialState: S,
    inputActions: SharedFlow<Action>,
    setStates: MutableStateFlow<S>,
    relayActions: MutableSharedFlow<Action>,
    reduce: suspend (action: Action, state: S) -> S
) = launch {

    var state = initialState

    inputActions
        .collect { action ->
            when (action) {
                is GetStateAction -> {
                    action.deferred.complete(state)
                }
                else -> {
                    measureTimeMillis {
                        val updatedState = reduce(action, state)
                        state = updatedState
                        setStates.emit(state)
                        relayActions.emit(action)
                    }.let { timeTakenToComputeNewState ->
                        //To make sure we are not doing any heavy work in reducer
                        Log.d("Store", "Took ${timeTakenToComputeNewState}ms for $action")
                        if (timeTakenToComputeNewState > 8) {
                            throw ExceededTimeLimitToComputeNewStatException("Took $timeTakenToComputeNewState for $action")
                        }
                    }
                }
            }
        }
}


abstract class BaseStoreTwo<S : State>(
    initialState: S,
    private val scope: CoroutineScope
) {

    private val mutableInputActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private val mutableStates: MutableStateFlow<S> = MutableStateFlow(initialState).apply {
        buffer(capacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    }

    val states: Flow<S> = mutableStates

    private val mutableRelayActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    val relayActions: Flow<Action> = mutableRelayActions

    init {
        scope.store(
            initialState = initialState,
            inputActions = mutableInputActions,
            setStates = mutableStates,
            relayActions = mutableRelayActions
        ) { action, state -> reduce(action, state) }

    }

    fun dispatch(action: Action) {
        mutableInputActions.tryEmit(action)
    }

    fun state(): S = mutableStates.value

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> currentState(): S {
        val deferred = CompletableDeferred<State>()
        mutableInputActions.emit(GetStateAction(deferred))
        return deferred.await() as S
    }

    protected abstract suspend fun reduce(action: Action, currentState: S): S

    fun cancel() {
        scope.cancel()
    }
}