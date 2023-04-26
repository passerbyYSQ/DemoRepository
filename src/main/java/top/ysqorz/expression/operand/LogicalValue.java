package top.ysqorz.expression.operand;

public class LogicalValue extends Value {
    private Boolean value;

    public LogicalValue(String operand) {
        super(operand.trim());
        this.value = Boolean.valueOf(this.operand);
    }

    @Override
    public Boolean getBoolean() {
        return value;
    }
}
