package ${packagePath};

public interface ${className} {

    <#list props?keys as key>
    String ${key} = "${props[key]}";

    </#list>
}
