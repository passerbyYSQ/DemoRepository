package top.ysqorz.batch.springbatch.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.ysqorz.batch.springbatch.model.Constant;

/**
 * 用户
 */
@TableName("CoreUser")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CoreUser extends BaseEntity {

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

    /**
     * 头像
     */
    @TableField("ThumbnailID")
    private String thumbnailID;

    /**
     * 邮箱地址
     */
    @TableField("Mail")
    private String mail;

    /**
     * 首选用户组
     */
    @TableField("InitialGroup")
    private String initialGroup;

    /**
     * 口令修改周期
     */
    @TableField("PwModifyTime")
    private String pwModifyTime;

    /**
     * 用户ID
     */
    @TableField("UserID")
    private String userID;

    /**
     * 组织名称
     */
    @TableField("OrganizerName")
    private String organizerName;

    /**
     * 联系电话
     */
    @TableField("Tel")
    private String tel;

    /**
     * 冻结标识
     */
    @TableField("IsFrozen")
    private String isFrozen;

    /**
     * 用户名全名
     */
    @TableField("CName")
    private String CName;

    /**
     * 缺省授权级别
     */
    @TableField("AuthoriseLevel")
    private String authoriseLevel;

    @Override
    public String getClassName() {
        return Constant.CLASS_CORE_USER;
    }
}
