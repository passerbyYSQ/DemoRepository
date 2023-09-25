package top.ysqorz.batch.springbatch.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.ysqorz.batch.springbatch.model.Constant;

/**
 * 数据收藏夹
 */
@TableName("Collector")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Collector extends BaseEntity {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 描述
     */
    @TableField("Description")
    private String description;

    /**
     * 名称
     */
    @TableField("Name")
    private String name;

    @Override
    public String getClassName() {
        return Constant.CLASS_COLLECTOR;
    }
}
