package top.ysqorz.expression.path.var.impl;

import top.ysqorz.expression.path.model.PatternConstant;

public class PropsPathVariable extends GeneralPathVariable {
    public PropsPathVariable(String syntax) {
        super(syntax);
    }

    @Override
    public boolean match() {
        return syntax.matches(PatternConstant.JAVA_VARIABLE_PATTERN);
    }

    @Override
    public Object reduce0(Object reducer) {
        return getAttr(reducer, syntax);
    }
}
