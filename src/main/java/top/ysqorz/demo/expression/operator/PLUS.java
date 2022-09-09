package top.ysqorz.demo.expression.operator;

import top.ysqorz.demo.expression.operand.NumericValue;

import java.math.BigDecimal;

/**
 * 加号(+)
 */
public class PLUS implements ArithmeticOperator {
    @Override
    public OperatorPriority getPriority() {
        return OperatorPriority.PLUS;
    }

    @Override
    public NumericValue evaluate(NumericValue leftValue, NumericValue rightValue) {
        BigDecimal sum = leftValue.getBigDecimal().add(rightValue.getBigDecimal());
        return new NumericValue(sum);
    }
}
