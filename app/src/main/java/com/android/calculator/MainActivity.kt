package com.android.calculator

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.android.calculator.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpData()
        callBacks()

    }

    private fun setUpData(){
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.lastEquation.text = state.lastOperation
                binding.currentEquation.text = state.currentEquation + " " + state.currentNumber
            }
        }
    }

    private fun callBacks(){
        binding.acBtn.setOnClickListener {
            viewModel.clearEquation()
        }
        binding.backBtn.setOnClickListener {
            viewModel.deleteLast()
        }
        binding.equleBtn.setOnClickListener {
            viewModel.onEqualButtonClicked()
        }
        binding.changeSignBtn.setOnClickListener {
            viewModel.onChangeSignClicked()
        }
    }

    fun onClickNum(v : View){
        val buttonLayout = v as ConstraintLayout
        val textView = buttonLayout.getChildAt(0) as TextView
        val number = textView.text.toString()
        viewModel.onNumberClicked(number)
    }

    fun onClickOperator(v : View){
        val buttonLayout = v as ConstraintLayout
        val textView = buttonLayout.getChildAt(0) as TextView
        val op = when(textView.text.toString()){
            "X" -> Operator.MULTIPLY
            "-" -> Operator.MINUS
            "/" -> Operator.DIVIDER
            "%" -> Operator.MOD
            "+" -> Operator.ADDITION
            else -> Operator.MULTIPLY
        }
        viewModel.onOperatorClicked(op)
    }
}