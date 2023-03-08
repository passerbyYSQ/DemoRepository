package ${packageName};

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;<#if parent?? && packageName != parent.packageName>
import ${parent.packageName}.${parent.className};</#if>

import java.io.Serializable;

/**
 * ${classComment}
 */
@TableName("${className}")
@Data
@EqualsAndHashCode<#if parent??>(callSuper = true)</#if>
public<#if isAbstract> abstract</#if> class ${className}<#if parent??> extends ${parent.className}</#if> implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    <#list attrs! as attr>
    /**
     * ${attr.comment}
     */<#if attr.name == 'IsDepleted'>
    @TableLogic</#if><#if attr.name == 'UUID'>
    @TableId("UUID")<#else>
    @TableField("${attr.name}")</#if>
    private ${attr.type} ${attr.formattedName};

    </#list>
}
