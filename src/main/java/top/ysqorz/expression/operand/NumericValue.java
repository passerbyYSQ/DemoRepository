package top.ysqorz.expression.operand;

import java.math.BigDecimal;

public class NumericValue extends Value {
    private BigDecimal value;

    public NumericValue(String operand) {
        super(operand.trim());
        this.value = new BigDecimal(this.operand);
    }

    public NumericValue(BigDecimal value) {
        super(null);
        this.value = value;
    }

    @Override
    public BigDecimal getBigDecimal() {
        return value;
    }
}
