package top.ysqorz.expression.path.model;

public interface PatternConstant {
    /**
     * Java变量命名
     */
    String JAVA_VARIABLE_PATTERN = "^[a-zA-Z_$][a-zA-Z\\d_$]*$";
    /**
     * array[index]
     * get("users")[index]
     */
    String ARRAY_ELEMENT_PATTERN = "^([a-zA-Z_$].*)\\[\\s*(\\d+)\\s*]$";
    /**
     * func(arg1, arg2, arg3) arg1可以是BeanPath，或者"123", 121等数值常量
     */
    String FUNC_CALL_PATTERN = "^([a-zA-Z_$][a-zA-Z_$\\d]*)\\((\\s*.+\\s*(,\\s*.+\\s*)*)?\\)$";
}
