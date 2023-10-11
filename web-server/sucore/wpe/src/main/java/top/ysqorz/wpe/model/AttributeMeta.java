package top.ysqorz.wpe.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.beans.Introspector;

@Data
@Accessors(chain = true)
public class AttributeMeta {
    private String type;
    private String name;
    private String formattedName;
    private String comment;

    public AttributeMeta(String name) {
        this(String.class, name);
    }

    public AttributeMeta(String name, String comment) {
        this(String.class, name, comment);
    }

    public AttributeMeta(Class<?> typeClazz, String name) {
        this(typeClazz, name, null);
    }

    public AttributeMeta(Class<?> typeClazz, String name, String comment) {
        this.type = typeClazz.getSimpleName();
        this.name = name;
        this.formattedName = formatAttrName(name);
        this.comment = comment;
    }

    private String formatAttrName(String attrName) {
        if ("Class".equalsIgnoreCase(attrName)) {
            return "Clazz";
        }
        // https://zhuanlan.zhihu.com/p/383518075
        return Introspector.decapitalize(StrUtil.toCamelCase(attrName));
    }
}