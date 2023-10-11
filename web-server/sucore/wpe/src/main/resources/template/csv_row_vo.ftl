import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ${classComment}
 *
 * @author yaoshiquan
 * @date 2023/9/18
 */
@Data
@EqualsAndHashCode
public class ${className} implements RowVO {

    <#list attrs! as attr>
    /**
     * ${attr.comment}
     */
    private ${attr.type} ${attr.formattedName};

    </#list>
}