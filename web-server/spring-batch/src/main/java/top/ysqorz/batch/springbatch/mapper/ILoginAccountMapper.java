package top.ysqorz.batch.springbatch.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.batch.springbatch.model.po.LoginAccount;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/19
 */
public interface ILoginAccountMapper extends BaseMapper<LoginAccount> {
    default LoginAccount selectByAccount(String account) {
        return selectOne(
                new QueryWrapper<LoginAccount>().lambda()
                        .eq(LoginAccount::getAccount, account)
        );
    }
}
