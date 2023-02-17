package ${packageName} ;

import lombok.Data;
<#if parent??>
import lombok.EqualsAndHashCode;
<#if packageName != parent.packageName>
import ${parent.packageName}.${parent.className};
</#if>
</#if>

/**
 * ${classComment}
 */
@Data
<#if parent??>@EqualsAndHashCode(callSuper = true)</#if>
public <#if isAbstract>abstract</#if> class ${className} <#if parent??>extends ${parent.className}</#if> {

    <#list attrs! as attr>
    /**
     * ${attr.comment}
     */
    private ${attr.type} ${attr.name};

    </#list>
}
