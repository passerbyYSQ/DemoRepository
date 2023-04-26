package top.ysqorz.expression.operator;

import top.ysqorz.expression.operand.NumericValue;

import java.math.BigDecimal;

public class MINUS implements ArithmeticOperator {

    @Override
    public NumericValue evaluate(NumericValue leftValue, NumericValue rightValue) {
        BigDecimal subtract = leftValue.getBigDecimal().subtract(rightValue.getBigDecimal());
        return new NumericValue(subtract);
    }

    @Override
    public OperatorPriority getPriority() {
        return OperatorPriority.MINUS;
    }
}
