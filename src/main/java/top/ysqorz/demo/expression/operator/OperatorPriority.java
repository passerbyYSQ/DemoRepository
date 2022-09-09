package top.ysqorz.demo.expression.operator;

/**
 * 运算符优先级
 */
public enum OperatorPriority {

    PLUS(6),
    MINUS(6),
    MULTIPLY(8)
    ;

    int priority;

    OperatorPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
