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
                currentNumber = if (_state.value.currentNumber == "0" && number != ".") number
                else if (_state.value.currentNumber == UNDEFINED && number != ".") number
                else if (_state.value.currentNumber == UNDEFINED && number == ".") "0$number"
                else _state.value.currentNumber + number
            )
        }
    }

    fun onChangeSignClicked() {
        if (_state.value.currentNumber == "0") return
        _state.update {
            it.copy(
                currentNumber = numToString((_state.value.currentNumber.toFloat() * -1))
            )
        }
    }

    fun onOperatorClicked(operator: Operator) {
        if (
            _state.value.operators.isNotEmpty() &&
            _state.value.operators.size == _state.value.numbers.size + 1
        ) {
            return
        }
        _state.update {
            it.copy(
                currentEquation = buildString {
                    append(_state.value.currentEquation)
                    append(" ")
                    append(_state.value.currentNumber)
                    append(" ")
                    append(operator.toEquation())
                },
                numbers = _state.value.numbers + _state.value.currentNumber,
                currentNumber = "0",
                operators = _state.value.operators + operator
            )
        }
    }

    fun clearEquation() {
        _state.update {
            it.copy(
                currentEquation = "",
                currentNumber = "0",
                lastOperation = "",
                operators = listOf(),
                numbers = listOf(),
            )
        }
    }

    fun deleteLast() {
        if (_state.value.currentNumber == "0") return
        val newNum = _state.value.currentNumber.dropLast(1)
        _state.update { it.copy(currentNumber = if (newNum.isEmpty()) "0" else newNum) }
    }

    fun onEqualButtonClicked() {
        if (_state.value.currentNumber == UNDEFINED) return
        _state.update { it.copy(numbers = _state.value.numbers + _state.value.currentNumber) }
        if (_state.value.operators.size == _state.value.numbers.size) {
            return
        }

        val res = calculateResult()

        _state.update {
            it.copy(
                lastOperation = _state.value.currentEquation + " " + _state.value.currentNumber,
                currentEquation = "",
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
        for (i in 0 until numbers.size - 1) {
            when (operators[i]) {
                Operator.DIVIDER -> {
                    if (numbers[i + 1].toFloat() == 0f) return UNDEFINED
                    res /= numbers[i + 1].toFloat()
                }
                Operator.MULTIPLY -> res *= numbers[i + 1].toFloat()
                Operator.ADDITION -> res += numbers[i + 1].toFloat()
                Operator.MOD -> res %= numbers[i + 1].toFloat()
                Operator.MINUS -> res -= numbers[i + 1].toFloat()
            }
        }
        return numToString(res)
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

    private fun numToString(num: Float): String {
        val newNum = num.toInt()
        return if (num == newNum.toFloat()) {
            newNum.toString()
        } else {
            num.toString()
        }
    }

    private companion object{
        const val UNDEFINED = "Undefined"
    }
}