package top.ysqorz.batch.springbatch.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class RelationEntity extends BaseEntity {
    /**
     * 左侧对象内部ID
     */
    @TableField("UUID_L")
    private String leftUUID;

    /**
     * 右侧对象内部ID
     */
    @TableField("UUID_R")
    private String rightUUID;

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

    public RelationEntity(BaseEntity leftEntity, BaseEntity rightEntity) {
        this(leftEntity.getUUID(), rightEntity.getUUID(), leftEntity.getClassName(), rightEntity.getClassName());
    }
}
