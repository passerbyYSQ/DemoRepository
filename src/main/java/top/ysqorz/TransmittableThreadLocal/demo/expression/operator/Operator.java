package top.ysqorz.TransmittableThreadLocal.demo.expression.operator;

import top.ysqorz.TransmittableThreadLocal.demo.expression.ExpressionEntity;

/**
 * 双目运算符
 */
public interface Operator extends ExpressionEntity {
    @Override
    default int getEntityType() {
        return OPERATOR;
    }

    /**
     * 运算符优先级
     */
    OperatorPriority getPriority();
}
