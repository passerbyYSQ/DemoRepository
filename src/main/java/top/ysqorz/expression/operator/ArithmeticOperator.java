package top.ysqorz.expression.operator;

import top.ysqorz.expression.operand.NumericValue;

public interface ArithmeticOperator extends Operator {
    NumericValue evaluate(NumericValue leftValue, NumericValue rightValue);
}
