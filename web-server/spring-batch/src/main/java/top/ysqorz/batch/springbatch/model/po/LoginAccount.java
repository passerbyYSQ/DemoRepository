package top.ysqorz.batch.springbatch.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.ysqorz.batch.springbatch.model.Constant;

/**
 * 登录账号
 */
@TableName("LoginAccount")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LoginAccount extends BaseEntity {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 账号状态
     */
    @TableField("Status")
    private String status;

    /**
     * 账号名称
     */
    @TableField("Account")
    private String account;

    /**
     * 加密用的随机盐
     */
    @TableField("Salt")
    private String salt;

    /**
     * 上一次登录时间
     */
    @TableField("LastLoginTime")
    private String lastLoginTime;

    /**
     * 用户名称
     */
    @TableField("UserName")
    private String userName;

    /**
     * 用户标识
     */
    @TableField("UserID")
    private String userID;

    /**
     * 加密后的密码
     */
    @TableField("Cipher")
    private String cipher;

    /**
     * 账号来源
     */
    @TableField("Source")
    private String source;

    @Override
    public String getClassName() {
        return Constant.CLASS_LOGIN_ACCOUNT;
    }
}
