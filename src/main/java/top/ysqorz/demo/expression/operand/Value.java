package top.ysqorz.demo.expression.operand;

import java.math.BigDecimal;

public abstract class Value implements Operand {
    protected String operand;

    public Value(String operand) {
        this.operand = operand;
    }

    @Override
    public Boolean getBoolean() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal() {
        throw new UnsupportedOperationException();
    }
}
