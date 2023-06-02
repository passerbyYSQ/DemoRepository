package top.ysqorz.migration.repos.zwt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.migration.repos.zwt.po.CoreUser;

public interface ICoreUserMapper extends BaseMapper<CoreUser> {
    /**
     * TODO 只做单表操作，最好只做单表查询，不要直连数据库修改表数据，而是通过sucore的外部消息
     */
}




