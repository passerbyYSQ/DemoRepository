package top.ysqorz.migration.repos.zwt.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 所有持久化PO的公共字段
 */
@Data
@EqualsAndHashCode
public class BaseEntity implements Serializable {
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
}
