package com.android.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel() : ViewModel() {
    private val _state = MutableStateFlow(CalculatorScreenState())
    val state = _state.asStateFlow()

    fun onNumberClicked(number: String) {
        _state.update {
            it.copy(
                currentNumber = _state.value.currentNumber + number
            )
        }
    }

    fun onChangeSignClicked() {
        _state.update {
            it.copy(
                currentNumber = "-" + _state.value.currentNumber
            )
        }
    }

    fun onOperatorClicked(operator: Operator) {
        if (_state.value.operators.size == _state.value.numbers.size){
            return
        }
        _state.update {
            it.copy(
                currentEquation = _state.value.currentEquation + _state.value.currentNumber + operator.toEquation(),
                numbers = _state.value.numbers + _state.value.currentNumber,
                currentNumber = "",
                operators = _state.value.operators + operator
            )
        }
    }

    fun clearEquation() {
        _state.update {
            it.copy(
                currentEquation = "",
                currentNumber = "",
                operators = listOf(),
                numbers = listOf(),
            )
        }
    }

    fun deleteLast() {
        _state.update { it.copy(currentNumber = _state.value.currentNumber.dropLast(1)) }
    }

    fun onEqualButtonClicked() {
        if (_state.value.operators.size == _state.value.numbers.size){
            return
        }

        val res = calculateResult()

        _state.update {
            it.copy(
                lastOperation = _state.value.currentEquation + _state.value.currentNumber,
                currentEquation = res,
                currentNumber = res,
                numbers = listOf(),
                operators = listOf()
            )
        }
    }

    private fun calculateResult(): String {
        val numbers = _state.value.numbers
        val operators = _state.value.operators

        if (numbers.size == 1) return numbers[0]
        if (numbers.isEmpty()) return "0"

        var res = numbers[0].toFloat()
        for (i in 0..numbers.size - 1) {
            when(operators[i]){
                Operator.DIVIDER -> res /= numbers[i].toFloat()
                Operator.MULTIPLY -> res *= numbers[i].toFloat()
                Operator.ADDITION -> res += numbers[i].toFloat()
                Operator.MOD -> res %= numbers[i].toFloat()
                Operator.MINUS -> res -= numbers[i].toFloat()
            }
        }
        return res.toString()
    }

    private fun Operator.toEquation(): String {
        return when (this) {
            Operator.DIVIDER -> "/"
            Operator.MULTIPLY -> "X"
            Operator.ADDITION -> "+"
            Operator.MOD -> "%"
            Operator.MINUS -> "-"
        }
    }
}