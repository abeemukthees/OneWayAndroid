package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.ControlledRunner
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.name
import com.msa.onewaycoroutines.base.nine.BaseSideEffectHotColdNine
import com.msa.onewaycoroutines.base.nine.BaseStoreNine
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

class CounterSideEffectNine(
    store: BaseStoreNine<*>,
    dispatchers: CoroutineDispatcherProvider,
) : BaseSideEffectHotColdNine(store, dispatchers) {

    private val controlledRunner by lazy { ControlledRunner<Int>() }

    override fun handleHot(action: Action) {
        Log.d(TAG,
            "handle HOT : ${action.name()} | ${state<CounterState>().counter} | ${System.nanoTime()}")
        when (action) {

            /*is CounterAction.IncrementAction -> {
                scope.launch {
                    val counter = controlledRunner.cancelPreviousThenRun {
                        awaitState<CounterState>().counter
                    }
                    if (counter == 25) {
                        Log.d(TAG, "Updated state 0 = ${state<CounterState>()}")
                        dispatch(CounterAction.ForceUpdateAction(state<CounterState>().counter * 10))
                        Log.d(TAG, "Updated state 1 = ${state<CounterState>()}")
                        Log.d(TAG, "Updated state 2 = ${awaitState<CounterState>()}")
                    }
                }
            }*/

            is CounterAction.DecrementAction -> {
                /* for (i in 0..10000) {
                     Log.d(TAG, "i = $i")
                 }*/
            }

            is CounterAction.ForceUpdateAction -> {

            }

            is CounterAction.ResetAction -> {

            }
        }
    }

    override fun handleCold(action: Action) {
        Log.d(TAG,
            "handle COLD : ${action.name()} | ${state<CounterState>()} | ${System.nanoTime()}")
    }
}