package top.ysqorz.demo.expression.operator;

import top.ysqorz.demo.expression.operand.NumericValue;

public interface ArithmeticOperator extends Operator {
    NumericValue evaluate(NumericValue leftValue, NumericValue rightValue);
}
