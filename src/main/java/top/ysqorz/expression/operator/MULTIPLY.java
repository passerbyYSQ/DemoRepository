package top.ysqorz.expression.operator;

import top.ysqorz.expression.operand.NumericValue;

import java.math.BigDecimal;

public class MULTIPLY implements ArithmeticOperator {

    @Override
    public NumericValue evaluate(NumericValue leftValue, NumericValue rightValue) {
        BigDecimal multiply = leftValue.getBigDecimal().multiply(rightValue.getBigDecimal());
        return new NumericValue(multiply);
    }

    @Override
    public OperatorPriority getPriority() {
        return OperatorPriority.MULTIPLY;
    }
}
