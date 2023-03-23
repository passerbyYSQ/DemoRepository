package top.ysqorz.TransmittableThreadLocal.demo.expression.operator;

public interface LogicalOperator extends Operator {
    Boolean evaluate(Boolean left, Boolean right);
}
