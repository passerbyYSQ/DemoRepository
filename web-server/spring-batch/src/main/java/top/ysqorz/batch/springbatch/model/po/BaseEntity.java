package top.ysqorz.batch.springbatch.model.po;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.ysqorz.batch.springbatch.model.Constant;

import java.io.Serializable;

/**
 * 所有持久化PO的公共字段
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public abstract class BaseEntity implements Serializable {
    /**
     * 内部唯一标识
     */
    @TableId("UUID")
    private String UUID;

    /**
     * 所有者
     */
    @TableField("Owner")
    private String owner;

    /**
     * 创建者
     */
    @TableField("Creator")
    private String creator;

    /**
     * 创建时间戳记
     */
    @TableField("CreateTimeStamp")
    private String createTimeStamp;

    /**
     * 更新时间戳记
     */
    @TableField("ModifyTimeStamp")
    private String modifyTimeStamp;

    /**
     * 是否已废弃
     */
//    @TableLogic
    @TableField("IsDepleted")
    private String isDepleted;

    public BaseEntity() {
        this.UUID = IdUtil.randomUUID();
        this.owner = Constant.ADMIN;
        this.creator = Constant.ADMIN;
        String timestamp = String.valueOf(System.currentTimeMillis());
        this.createTimeStamp = timestamp;
        this.modifyTimeStamp = timestamp;
        this.isDepleted = Constant.MINUS;
    }

    public abstract String getClassName();
}
