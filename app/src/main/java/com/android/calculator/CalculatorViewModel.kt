package com.android.calculator

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel() : ViewModel() {
    private val _state = MutableStateFlow(CalculatorScreenState())
    val state = _state.asStateFlow()

    fun onNumberClicked(number: String) {
        if (_state.value.numbers.isNotEmpty() && _state.value.numbers.size >= 7) return
        if (_state.value.currentNumber.length >= 10) return
        if (_state.value.currentNumber.contains(".") && number == ".") return
        _state.update {
            it.copy(
                currentNumber = if (_state.value.currentNumber == UNDEFINED && number != ".") number
                else if (_state.value.currentNumber == UNDEFINED && number == ".") "0$number"
                else if (_state.value.currentNumber == "" && number == ".") "0$number"
                else if (_state.value.currentEquation == "" && _state.value.currentNumber == "0" && number != ".") number
                else _state.value.currentNumber + number,
                res = ""
            )
        }
    }

    fun onChangeSignClicked() {
        _state.update {
            it.copy(
                currentNumber = if (_state.value.currentNumber == "") "-"
                else if (_state.value.currentNumber == "0") "-"
                else if (_state.value.currentNumber.first() == '-') _state.value.currentNumber.drop(1)
                else "-" + _state.value.currentNumber
            )
        }
    }

    fun onOperatorClicked(operator: Operator) {
        if (_state.value.operators.isNotEmpty() && _state.value.operators.size >= 6) return
        if (_state.value.currentNumber == UNDEFINED) return
        if (_state.value.currentNumber != "" && _state.value.currentNumber != "-") {
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
                    currentNumber = "",
                    operators = _state.value.operators + operator
                )
            }
        }
    }

    fun clearEquation() {
        _state.update {
            it.copy(
                currentEquation = "",
                currentNumber = "0",
                lastOperation = "",
                res = "",
                operators = listOf(),
                numbers = listOf(),
            )
        }
    }

    fun deleteLast() {
        if (_state.value.currentNumber == UNDEFINED) _state.update { it.copy(currentNumber = "0") }
        else if (_state.value.currentNumber == "" && _state.value.operators.isNotEmpty()) {
            _state.update {
                it.copy(
                    currentEquation = _state.value.currentEquation
                        .split(" ")
                        .dropLast(2)
                        .joinToString(" "),
                    currentNumber = _state.value.numbers.last(),
                    numbers = _state.value.numbers.dropLast(1),
                    operators = _state.value.operators.dropLast(1),
                )
            }
        }
        else if (_state.value.currentNumber != "") {
            val newNum = _state.value.currentNumber.dropLast(1)
            if (newNum == "" && _state.value.currentEquation == "")
                _state.update { it.copy(currentNumber = "0") }
            else
                _state.update { it.copy(currentNumber = newNum) }
        }
    }

    fun onEqualButtonClicked() {
        if (_state.value.currentNumber == UNDEFINED) return
        if (_state.value.currentNumber.isEmpty()) return
        if (_state.value.currentNumber == "-") return
        if (_state.value.operators.size != _state.value.numbers.size) return
        _state.update { it.copy(numbers = _state.value.numbers + _state.value.currentNumber) }
        val res = calculateResult()

        _state.update {
            it.copy(
                lastOperation = _state.value.currentEquation + " " + _state.value.currentNumber,
                currentEquation = "",
                currentNumber = res,
                res = res,
                numbers = listOf(),
                operators = listOf()
            )
        }
    }

    private fun calculateResult(): String {
        val numbers = _state.value.numbers.map { it.toFloat() }.toMutableList()
        val operators = _state.value.operators.toMutableList()

        if (numbers.size == 1) return numToString(numbers[0])
        if (numbers.isEmpty()) return "0"

        var i = 0
        while (i < operators.size) {
            when (operators[i]) {
                Operator.DIVIDER -> {
                    if (numbers[i + 1] == 0f) return UNDEFINED
                    numbers[i] /= numbers[i + 1]
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                Operator.MULTIPLY -> {
                    numbers[i] *= numbers[i + 1]
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                Operator.MOD -> {
                    if (numbers[i + 1] == 0f) return UNDEFINED
                    numbers[i] %= numbers[i+1]
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                Operator.ADDITION, Operator.MINUS -> i++
            }
        }

        while (operators.isNotEmpty()) {
            when (operators[FIRST_ITEM]) {
                Operator.ADDITION -> {
                    val res = numbers[FIRST_ITEM] + numbers[FIRST_ITEM + 1]
                    numbers[FIRST_ITEM] = res
                    numbers.removeAt(FIRST_ITEM + 1)
                    operators.removeAt(FIRST_ITEM)
                }

                Operator.MINUS -> {
                    val res = numbers[FIRST_ITEM] - numbers[FIRST_ITEM + 1]
                    numbers[FIRST_ITEM] = res
                    operators.removeAt(FIRST_ITEM)
                }

                Operator.DIVIDER, Operator.MULTIPLY, Operator.MOD -> {}
            }
        }
        return numToString(numbers[FIRST_ITEM])
    }

    private fun Operator.toEquation(): String {
        return when (this) {
            Operator.DIVIDER -> "/"
            Operator.MULTIPLY -> "x"
            Operator.ADDITION -> "+"
            Operator.MOD -> "%"
            Operator.MINUS -> "-"
        }
    }

    private fun numToString(num: Float): String {
        var n = num
        if (num.toString().contains("-0")) n *= -1
        return if (n.toString().endsWith(".0")) {
            n.toString().substring(0, n.toString().length - 2)
        } else {
            n.toString()
        }
    }

    private companion object {
        const val UNDEFINED = "Undefined"
        const val FIRST_ITEM = 0
    }
}