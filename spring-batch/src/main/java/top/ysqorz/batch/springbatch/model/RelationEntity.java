package top.ysqorz.batch.springbatch.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RelationEntity extends BaseEntity {
    /**
     * 左侧对象内部ID
     */
    @TableField("UUID_L")
    private String uuidL;

    /**
     * 右侧对象内部ID
     */
    @TableField("UUID_R")
    private String uuidR;

    /**
     * 左侧类
     */
    @TableField("LeftClass")
    private String leftClass;

    /**
     * 右侧类
     */
    @TableField("RightClass")
    private String rightClass;
}
