package top.ysqorz.TransmittableThreadLocal.demo.expression.operand;

import top.ysqorz.TransmittableThreadLocal.demo.expression.ExpressionEntity;

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
