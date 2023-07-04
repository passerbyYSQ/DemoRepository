package top.ysqorz.expression;

public interface ExpressionEntity {
    int OPERAND = 0; // 操作数
    int OPERATOR = 1; // 运算符
    int BRACKET = 2; // 括号

    int getEntityType();
}
