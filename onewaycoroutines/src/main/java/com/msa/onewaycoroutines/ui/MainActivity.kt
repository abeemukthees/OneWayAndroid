package com.msa.onewaycoroutines.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msa.core.EventAction
import com.msa.core.name
import com.msa.onewaycoroutines.base.BaseViewModelFactory
import com.msa.onewaycoroutines.common.ShowToastAction
import com.msa.onewaycoroutines.databinding.ActivityMainBinding
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import com.msa.onewaycoroutines.ui.viewmodels.CounterViewModelEight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    var counter = 0

    private val viewModel by viewModels<CounterViewModelEight> {
        BaseViewModelFactory {
            CounterViewModelEight.get(this)
        }
    }
    private val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonDecrement.setOnClickListener {
            viewModel.dispatch(CounterAction.DecrementAction)
            /*lifecycleScope.launch {
                repeat(25) {
                    viewModel.dispatch(CounterAction.DecrementAction)
                }
            }*/
        }

        binding.buttonIncrement.setOnClickListener {
            viewModel.dispatch(CounterAction.IncrementAction)
            /*scope.launch {
                repeat(25) {
                    viewModel.dispatch(CounterAction.IncrementAction)
                    viewModel.dispatch(CounterAction.ForceUpdateAction(viewModel.awaitState().counter - 1))
                }
            }*/
        }

        binding.buttonReset.setOnClickListener {
            viewModel.dispatch(CounterAction.ResetAction)
        }

        binding.buttonShowToast.setOnClickListener {
            viewModel.dispatch(ShowToastAction("${System.currentTimeMillis()}"))
        }

        lifecycleScope.launchWhenCreated {
            viewModel.states.collect(::setupViews)
        }

        //Can be handled from a Middleware
        /*lifecycleScope.launchWhenResumed {
            viewModel.eventActions.collect(::processEvents)
        }*/

        //Listen to changes of a specific value/part of the state
        lifecycleScope.launchWhenStarted {
            viewModel.states.map { it.updateOn }.distinctUntilChanged().collect {
            }
        }
    }

    private fun setupViews(state: CounterState) {
        //Log.d(TAG, "$counter setupViews = ${state.counter}")
        counter++
        binding.textCount.text = state.counter.toString()
    }

    private fun processEvents(action: EventAction) {
        Log.d(TAG, "processEvents = ${action.name()}")
        when (action) {

            is ShowToastAction -> {
                Toast.makeText(this, "A ${action.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}