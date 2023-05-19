package top.ysqorz.expression.path.var.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.springframework.util.ObjectUtils;
import top.ysqorz.expression.model.PatternConstant;
import top.ysqorz.expression.path.BeanPath;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * func(arg1, arg2, arg3)
 */
@Getter
public class FuncPathVariable extends GeneralPathVariable {
    public static final Pattern pattern = Pattern.compile(PatternConstant.FUNC_CALL_PATTERN);
    private Matcher matcher;
    private String funcName;
    private List<Object> argsValueList;
    private Object source;

    public FuncPathVariable(String syntax, Object source) {
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
        this.funcName = matcher.group(1);
        this.argsValueList = Arrays.stream(matcher.group(2).split("\\s*,\\s*"))
                .map(arg -> {
                    // 连续逗号
                    if (ObjectUtils.isEmpty(arg)) {
                        throw new RuntimeException(funcName + "的参数列表错误");
                    }
                    // 字符串 "123"
                    if (StrUtil.isWrap(arg, "\"")) {
                        return arg.substring(1, arg.length() - 1);
                    }
                    // Integer 123
                    if (NumberUtil.isInteger(arg)) {
                        return Integer.valueOf(arg);
                    }
                    // Double 浮点数 123.123
                    if (NumberUtil.isDouble(arg)) {
                        return Double.valueOf(arg);
                    }
                    // 当作BeanPath形式的变量处理
                    return new BeanPath(arg, source).getValue();
                })
                .collect(Collectors.toList());
        return ReflectUtil.invoke(reducer, funcName, this.argsValueList.toArray());
    }
}
