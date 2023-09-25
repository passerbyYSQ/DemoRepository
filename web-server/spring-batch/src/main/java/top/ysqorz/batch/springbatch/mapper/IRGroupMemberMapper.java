package top.ysqorz.batch.springbatch.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.batch.springbatch.model.po.CoreGroup;
import top.ysqorz.batch.springbatch.model.po.CoreUser;
import top.ysqorz.batch.springbatch.model.po.RGroupMember;

public interface IRGroupMemberMapper extends BaseMapper<RGroupMember> {
    default RGroupMember selectByBothUUID(CoreGroup group, CoreUser user) {
        return selectOne(
            new QueryWrapper<RGroupMember>().lambda()
                    .eq(RGroupMember::getLeftUUID, group.getUUID())
                    .eq(RGroupMember::getRightUUID, user.getUUID())
        );
    }
}
