package top.ysqorz.migration.repos.zwt.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户组成员关系
 */
@TableName("rGrpMmbr")
@Data
@EqualsAndHashCode(callSuper = true)
public class RGroupMember extends RelationEntity {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 是否冻结
     */
    @TableField("IsFrozen")
    private String isFrozen;

}
