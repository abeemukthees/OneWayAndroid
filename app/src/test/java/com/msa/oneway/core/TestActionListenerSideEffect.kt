package com.msa.oneway.core

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */
class TestActionListenerSideEffect(
    store: Store<*>,
    threadExecutorService: ThreadExecutorService
) : BaseSideEffect(store, threadExecutorService) {

    override fun handle(action: Action) {
        println("handle action = ${action.javaClass.simpleName}")
    }
}