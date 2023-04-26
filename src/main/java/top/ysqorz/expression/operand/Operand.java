package top.ysqorz.expression.operand;

import top.ysqorz.expression.ExpressionEntity;

import java.math.BigDecimal;

public interface Operand extends ExpressionEntity {
    @Override
    default int getEntityType() {
        return OPERAND;
    }

    Boolean getBoolean();

    BigDecimal getBigDecimal();

    String getString();
}