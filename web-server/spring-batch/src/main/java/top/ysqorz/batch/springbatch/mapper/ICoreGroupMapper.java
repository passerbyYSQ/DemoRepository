package top.ysqorz.batch.springbatch.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.batch.springbatch.model.po.CoreGroup;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/22
 */
public interface ICoreGroupMapper extends BaseMapper<CoreGroup> {
    default CoreGroup selectByName(String name) {
        return selectOne(
            new QueryWrapper<CoreGroup>().lambda()
                    .eq(CoreGroup::getName, name)
        );
    }
}
