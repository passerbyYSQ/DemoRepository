package top.ysqorz.wpe.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class SucoreClassMeta {
    private String module;
    private String packageName;
    private String className;
    private String classComment;
    private Boolean isAbstract;
    private Boolean isDynamic;
    private List<AttributeMeta> attrs;
    private SucoreClassMeta parent; // 父类

    public <T> SucoreClassMeta addAttr(String name) {
        return addAttr(String.class, name);
    }

    public <T> SucoreClassMeta addAttr(String name, String comment) {
        return addAttr(String.class, name, comment);
    }

    public <T> SucoreClassMeta addAttr(Class<T> typeClazz, String name) {
        return addAttr(typeClazz, name, null);
    }

    public <T> SucoreClassMeta addAttr(Class<T> typeClazz, String name, String comment) {
        if (attrs == null) {
            attrs = new ArrayList<>();
        }
        attrs.add(new AttributeMeta(typeClazz, name, comment));
        return this;
    }
}
