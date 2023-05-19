package top.ysqorz.expression.path.var.impl;

import lombok.Getter;
import top.ysqorz.expression.model.PatternConstant;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * array[index]
 * get("$...", asfgag)[]
 */
@Getter
public class ArrayElementPathVariable extends GeneralPathVariable {
    public static final Pattern pattern = Pattern.compile(PatternConstant.ARRAY_ELEMENT_PATTERN);
    private Matcher matcher;
    private String arrayName;
    private int index;
    private Object source;

    public ArrayElementPathVariable(String syntax, Object source) {
        super(syntax);
        this.matcher = pattern.matcher(syntax);
        this.source = source;
    }

    @Override
    public boolean match() {
        return matcher.matches();
    }

    @Override
    public Object reduce0(Object reducer) {
        this.arrayName = matcher.group(1).trim();
        FuncPathVariable funcPathVar = new FuncPathVariable(arrayName, source);
        Object arr;
        if (funcPathVar.match()) {
            arr = funcPathVar.reduce(reducer); // get("users")[index]
        } else {
            arr = getAttr(reducer, arrayName);// array[index]
        }
        this.index = Integer.parseInt(matcher.group(2));
        // 不做越界检查，越界让它抛出异常以便定位问题所在
        if (arr instanceof Object[]) {
            return ((Object[]) arr)[index];
        }
        if (arr instanceof List) {
            return ((List<?>) arr).get(index);
        }
        return null;
    }
}
