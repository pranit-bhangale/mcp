package com.mcp.client.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class CalculatorTools {

    @Tool(
            name = "addition",
            description = "Adds two numerical values and returns their sum. Use this tool when the user asks to add numbers or perform an addition operation."
    )
    public double addition(
            @ToolParam(description = "The first number to add") double a,
            @ToolParam(description = "The second number to add") double b
    ) {
        return a + b;
    }

    @Tool(
            name = "subtraction",
            description = "Subtracts the second numerical value from the first numerical value. Use this tool when the user asks to subtract numbers."
    )
    public double subtraction(
            @ToolParam(description = "The original number from which the second number will be subtracted") double a,
            @ToolParam(description = "The number to subtract from the first number") double b
    ) {
        return a - b;
    }

    @Tool(
            name = "multiplication",
            description = "Multiplies two numerical values and returns their product. Use this tool when the user asks to multiply numbers."
    )
    public double multiplication(
            @ToolParam(description = "The first number to multiply") double a,
            @ToolParam(description = "The second number to multiply") double b
    ) {
        return a * b;
    }

    @Tool(
            name = "division",
            description = "Divides the first numerical value by the second numerical value. Use this tool when the user asks to divide numbers. The divisor must not be zero."
    )
    public double division(
            @ToolParam(description = "The dividend, which is the number being divided") double a,
            @ToolParam(description = "The divisor, which is the number used to divide the dividend. Must not be zero") double b
    ) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero");
        }
        return a / b;
    }

    @Tool(
            name = "modulus",
            description = "Calculates the remainder after dividing the first number by the second number. Use this tool for modulo or remainder operations."
    )
    public double modulus(
            @ToolParam(description = "The dividend number") double a,
            @ToolParam(description = "The divisor number. Must not be zero") double b
    ) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero");
        }
        return a % b;
    }

    @Tool(
            name = "power",
            description = "Calculates the first number raised to the power of the second number. For example, 2 raised to the power of 3 returns 8."
    )
    public double power(
            @ToolParam(description = "The base number") double a,
            @ToolParam(description = "The exponent to which the base number is raised") double b
    ) {
        return Math.pow(a, b);
    }

    @Tool(
            name = "squareRoot",
            description = "Calculates the square root of a non-negative number. Use this tool when the user asks for the square root of a value."
    )
    public double squareRoot(
            @ToolParam(description = "The non-negative number whose square root must be calculated") double number
    ) {
        if (number < 0) {
            throw new IllegalArgumentException("Negative number");
        }
        return Math.sqrt(number);
    }

    @Tool(
            name = "square",
            description = "Calculates the square of a number by multiplying the number by itself."
    )
    public double square(
            @ToolParam(description = "The number whose square must be calculated") double number
    ) {
        return number * number;
    }

    @Tool(
            name = "cube",
            description = "Calculates the cube of a number by multiplying the number by itself three times."
    )
    public double cube(
            @ToolParam(description = "The number whose cube must be calculated") double number
    ) {
        return number * number * number;
    }

    @Tool(
            name = "percentage",
            description = "Calculates a specified percentage of a given number. For example, calculate 18 percent of 5000."
    )
    public double percentage(
            @ToolParam(description = "The original number from which the percentage is calculated") double number,
            @ToolParam(description = "The percentage value to calculate, such as 18 for 18 percent") double percent
    ) {
        return number * percent / 100;
    }

    @Tool(
            name = "average",
            description = "Calculates the arithmetic average of three numerical values by adding them and dividing the sum by three."
    )
    public double average(
            @ToolParam(description = "The first number") double a,
            @ToolParam(description = "The second number") double b,
            @ToolParam(description = "The third number") double c
    ) {
        return (a + b + c) / 3;
    }

    @Tool(
            name = "maximum",
            description = "Finds and returns the largest value among three numerical values."
    )
    public double maximum(
            @ToolParam(description = "The first number to compare") double a,
            @ToolParam(description = "The second number to compare") double b,
            @ToolParam(description = "The third number to compare") double c
    ) {
        return Math.max(a, Math.max(b, c));
    }

    @Tool(
            name = "minimum",
            description = "Finds and returns the smallest value among three numerical values."
    )
    public double minimum(
            @ToolParam(description = "The first number to compare") double a,
            @ToolParam(description = "The second number to compare") double b,
            @ToolParam(description = "The third number to compare") double c
    ) {
        return Math.min(a, Math.min(b, c));
    }

    @Tool(
            name = "absoluteValue",
            description = "Returns the absolute value of a number, removing its negative sign if present."
    )
    public double absoluteValue(
            @ToolParam(description = "The number whose absolute value must be calculated") double number
    ) {
        return Math.abs(number);
    }

    @Tool(
            name = "factorial",
            description = "Calculates the factorial of a non-negative integer. Factorial is the product of all positive integers from 1 to the given number. Supports values from 0 to 20."
    )
    public long factorial(
            @ToolParam(description = "The non-negative integer whose factorial must be calculated. Allowed range is 0 to 20") int number
    ) {
        if (number < 0 || number > 20) {
            throw new IllegalArgumentException(
                    "Number must be between 0 and 20"
            );
        }

        long result = 1;

        for (int i = 1; i <= number; i++) {
            result *= i;
        }

        return result;
    }

    @Tool(
            name = "roundNumber",
            description = "Rounds a decimal number to the nearest whole integer using standard Java rounding rules."
    )
    public long roundNumber(
            @ToolParam(description = "The decimal number that must be rounded to the nearest integer") double number
    ) {
        return Math.round(number);
    }

    @Tool(
            name = "ceiling",
            description = "Rounds a decimal number upward to the smallest integer greater than or equal to the given number."
    )
    public double ceiling(
            @ToolParam(description = "The decimal number that must be rounded upward") double number
    ) {
        return Math.ceil(number);
    }

    @Tool(
            name = "floor",
            description = "Rounds a decimal number downward to the largest integer less than or equal to the given number."
    )
    public double floor(
            @ToolParam(description = "The decimal number that must be rounded downward") double number
    ) {
        return Math.floor(number);
    }

    @Tool(
            name = "convertToFahrenheit",
            description = "Converts a temperature from Celsius to Fahrenheit using the formula Fahrenheit = (Celsius * 9/5) + 32."
    )
    public double convertToFahrenheit(
            @ToolParam(description = "Temperature value in degrees Celsius") double celsius
    ) {
        return (celsius * 9 / 5) + 32;
    }

    @Tool(
            name = "convertToCelsius",
            description = "Converts a temperature from Fahrenheit to Celsius using the formula Celsius = (Fahrenheit - 32) * 5/9."
    )
    public double convertToCelsius(
            @ToolParam(description = "Temperature value in degrees Fahrenheit") double fahrenheit
    ) {
        return (fahrenheit - 32) * 5 / 9;
    }
}