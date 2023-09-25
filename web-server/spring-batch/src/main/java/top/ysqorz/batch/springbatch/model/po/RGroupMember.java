package top.ysqorz.batch.springbatch.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.ysqorz.batch.springbatch.model.Constant;

/**
 * 用户组成员关系
 */
@TableName("rGrpMmbr")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RGroupMember extends RelationEntity {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 是否冻结
     */
    @TableField("IsFrozen")
    private String isFrozen;

    public RGroupMember(CoreGroup group, CoreUser user) {
        super(group, user);
    }

    @Override
    public String getClassName() {
        return Constant.CLASS_GROUP_MEMBER;
    }
}
