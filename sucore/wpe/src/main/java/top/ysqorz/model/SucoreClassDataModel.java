package top.ysqorz.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class SucoreClassDataModel {
    private String module;
    private String packageName;
    private String className;
    private String classComment;
    private Boolean isAbstract;
    private List<Attribute> attrs;
    private SucoreClassDataModel parent; // 父类

    public <T> SucoreClassDataModel addAttr(String name) {
        return addAttr(String.class, name);
    }

    public <T> SucoreClassDataModel addAttr(String name, String comment) {
        return addAttr(String.class, name, comment);
    }

    public <T> SucoreClassDataModel addAttr(Class<T> typeClazz, String name) {
        return addAttr(typeClazz, name, null);
    }

    public <T> SucoreClassDataModel addAttr(Class<T> typeClazz, String name, String comment) {
        if (attrs == null) {
            attrs = new ArrayList<>();
        }
        attrs.add(new Attribute(typeClazz, name, comment));
        return this;
    }

    @Data
    public static class Attribute {
        private Class<?> typeClazz;
        private String type;
        private String name;

        private String comment;

        public Attribute(String name) {
            this(String.class, name);
        }

        public Attribute(String name, String comment) {
            this(String.class, name, comment);
        }

        public Attribute(Class<?> typeClazz, String name) {
            this(typeClazz, name, null);
        }

        public Attribute(Class<?> typeClazz, String name, String comment) {
            this.typeClazz = typeClazz;
            this.type = typeClazz.getSimpleName();
            this.name = name;
            this.comment = comment;
        }
    }
}
