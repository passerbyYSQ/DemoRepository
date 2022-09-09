package top.ysqorz.demo.expression.operand;

public class StringValue extends Value {
    public StringValue(String operand) {
        super(operand);
    }

    @Override
    public String getString() {
        return operand;
    }
}
