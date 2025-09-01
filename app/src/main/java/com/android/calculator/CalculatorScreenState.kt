package com.android.calculator

data class CalculatorScreenState(
    val operators: List<Operator> = listOf(),
    val currentEquation: String = "",
    val currentNumber: String = "0",
    val numbers: List<String> = listOf(),
    val lastOperation: String = "",
    )

enum class Operator{
    DIVIDER,
    MULTIPLY,
    ADDITION,
    MOD,
    MINUS,
}
