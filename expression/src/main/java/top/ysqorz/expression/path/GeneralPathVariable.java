package top.ysqorz.expression.path;

import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

@Getter
public abstract class GeneralPathVariable implements PathVariable {
    protected String syntax;
    protected Object result;

    public GeneralPathVariable(String syntax) {
        this.syntax = syntax;
    }

    @Override
    public String getSyntax() {
        return syntax;
    }

    @Override
    public Object reduce(Object reducer) {
        if (Objects.nonNull(result)) {
            return result;
        }
        return result = reduce0(reducer); // 缓存计算结果
    }

    protected abstract Object reduce0(Object reducer);

    @Override
    public Object getResult() {
        return result;
    }

    protected Object getAttr(Object obj, String attr) {
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(attr);
        }
        return ReflectUtil.getFieldValue(obj, attr); // syntax即为属性名
    }
}
