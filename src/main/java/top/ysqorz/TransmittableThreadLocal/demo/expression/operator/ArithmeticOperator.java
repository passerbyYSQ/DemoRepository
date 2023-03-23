package top.ysqorz.TransmittableThreadLocal.demo.expression.operator;

import top.ysqorz.TransmittableThreadLocal.demo.expression.operand.NumericValue;

public interface ArithmeticOperator extends Operator {
    NumericValue evaluate(NumericValue leftValue, NumericValue rightValue);
}
