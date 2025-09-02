package com.android.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel() : ViewModel() {
    private val _state = MutableStateFlow(CalculatorScreenState())
    val state = _state.asStateFlow()

    fun onNumberClicked(number: String) {
        if (
            _state.value.operators.isNotEmpty() &&
            _state.value.operators.last() == Operator.MOD
        ) return

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
        if (_state.value.currentNumber == "0") return
        if (
            _state.value.operators.isNotEmpty() &&
            _state.value.operators.last() != Operator.MOD &&
            _state.value.operators.size == _state.value.numbers.size + 1
        ) {
            _state.update {
                it.copy(
                    currentEquation = _state.value.currentNumber
                        .split(" ")
                        .dropLast(1)
                        .joinToString(" ") + operator.toEquation(),
                    operators = _state.value.operators.dropLast(1) + operator
                )
            }
        } else {
            _state.update {
                it.copy(
                    currentEquation = buildString {
                        append(_state.value.currentEquation)
                        append(" ")
                        append(_state.value.currentNumber)
                        append(" ")
                        append(operator.toEquation())
                    },
                    numbers = if (
                        _state.value.operators.isNotEmpty() &&
                        _state.value.operators.last() == Operator.MOD
                        )
                        _state.value.numbers
                    else
                        _state.value.numbers + _state.value.currentNumber,
                    currentNumber = if (operator == Operator.MOD) "" else "0",
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
                operators = listOf(),
                numbers = listOf(),
            )
        }
    }

    fun deleteLast() {
        if (_state.value.currentNumber == "0" && _state.value.operators.isEmpty()) return
        else if (
            (_state.value.currentNumber == "0" || _state.value.currentNumber == "") &&
            _state.value.operators.isNotEmpty()
        ) {
            val lastNumber = _state.value.currentEquation
                .split(" ")
                .dropLast(1)
                .last()
            _state.update {
                it.copy(
                    currentEquation = _state.value.currentEquation
                        .split(" ")
                        .dropLast(2)
                        .joinToString(" "),
                    currentNumber = lastNumber,
                    numbers = _state.value.numbers.dropLast(1),
                    operators = _state.value.operators.dropLast(1)
                )
            }
        } else {
            val newNum = _state.value.currentNumber.dropLast(1)
            _state.update { it.copy(currentNumber = if (newNum.isEmpty()) "0" else newNum) }
        }
    }

    fun onEqualButtonClicked() {
        if (_state.value.currentNumber == UNDEFINED) return
        if (_state.value.currentNumber != "")
            _state.update { it.copy(numbers = _state.value.numbers + _state.value.currentNumber) }

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
        val numbers = _state.value.numbers.map { it.toFloat() }.toMutableList()
        val operators = _state.value.operators.toMutableList()

        if (numbers.size == 1) return numbers[0].toString()
        if (numbers.isEmpty()) return "0"

        var i = 0
        while (i < operators.size)  {
            when (operators[i]) {
                Operator.DIVIDER -> {
                    if (numbers[i + 1] == 0f) return UNDEFINED
                    val res = numbers[i] / numbers[i + 1]
                    numbers[i] = res
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                Operator.MULTIPLY -> {
                    val res = numbers[i] * numbers[i + 1]
                    numbers[i] = res
                    numbers.removeAt(i + 1)
                    operators.removeAt(i)
                }
                Operator.MOD -> {
                    val res = numbers[i] / 100
                    numbers[i] = res
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

    private companion object {
        const val UNDEFINED = "Undefined"
        const val FIRST_ITEM = 0
    }
}