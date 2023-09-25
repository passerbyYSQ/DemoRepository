package top.ysqorz.batch.springbatch.model.vo;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.ysqorz.batch.springbatch.model.Constant;
import top.ysqorz.batch.springbatch.model.po.CoreUser;
import top.ysqorz.batch.springbatch.model.po.LoginAccount;

import javax.validation.constraints.NotBlank;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/18
 */
@Data
@EqualsAndHashCode
public class AccountUserVO implements RowVO {
    @NotBlank
    private String account;
    @NotBlank
    private String userName;
    private String contact;
    private String email;
    private String status;

    public CoreUser toCoreUser() {
        CoreUser user = new CoreUser()
                .setUserID(userName)
                .setName(userName)
                .setCName(userName)
                .setTel(contact)
                .setMail(email)
                .setIsFrozen(Constant.MINUS);
        user.setIsDepleted(isDepleted());
        return user;
    }

    public LoginAccount toLoginAccount(String defaultPassword) {
        String salt = RandomUtil.randomString(8); // 加密登录密码和JWT的随机盐
        MD5 md5 = new MD5(salt.getBytes(), 32);
        String cipher = md5.digestHex(defaultPassword);
        return new LoginAccount()
                .setSource(Constant.DEFAULT)
                .setStatus("0")
                .setAccount(account)
                .setUserID(account)
                .setUserName(userName)
                .setSalt(salt)
                .setCipher(cipher);
    }

    public String isDepleted() {
        return "生效".equals(status) ? Constant.PLUS : Constant.MINUS;
    }

    @Override
    public String getRowKey() {
        return account;
    }
}
