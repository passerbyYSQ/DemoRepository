package ${packageName};
<#if !isDynamic>
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;</#if>
import lombok.Data;
import lombok.EqualsAndHashCode;<#if parent?? && packageName != parent.packageName>
import ${parent.packageName}.${parent.className};</#if>

import java.io.Serializable;

/**
 * ${classComment}
 */<#if !isDynamic>
@TableName("${className}")</#if>
@Data
@EqualsAndHashCode<#if parent??>(callSuper = true)</#if>
public<#if isAbstract> abstract</#if> class ${className}<#if parent??> extends ${parent.className}</#if> implements Serializable {
    <#if !isDynamic>
    @TableField(exist = false)</#if>
    private static final long serialVersionUID = 1L;

    <#list attrs! as attr>
    /**
     * ${attr.comment}
     */<#if !isDynamic><#if attr.name == 'IsDepleted'>
    @TableLogic</#if><#if attr.name == 'UUID'>
    @TableId("UUID")<#else>
    @TableField("${attr.name}")</#if></#if>
    private ${attr.type} ${attr.formattedName};

    </#list>
}
