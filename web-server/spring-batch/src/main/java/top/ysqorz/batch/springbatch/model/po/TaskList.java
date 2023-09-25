package top.ysqorz.batch.springbatch.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.ysqorz.batch.springbatch.model.Constant;

/**
 * 用户任务列表
 */
@TableName("TaskList")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TaskList extends BaseEntity {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 所属用户名
     */
    @TableField("UserID")
    private String userID;

    /**
     * 受托用户
     */
    @TableField("ConsignedUser")
    private String consignedUser;

    /**
     * 是否已设置委托
     */
    @TableField("IsConsigned")
    private String isConsigned;

    @Override
    public String getClassName() {
        return Constant.CLASS_TASK_LIST;
    }
}
