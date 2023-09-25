package top.ysqorz.batch.springbatch.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.batch.springbatch.model.Constant;
import top.ysqorz.batch.springbatch.model.po.Collector;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/19
 */
public interface ICollectorMapper extends BaseMapper<Collector> {
    default Collector selectRootByOwner(String owner) {
        return selectOne(
                new QueryWrapper<Collector>().lambda()
                        .eq(Collector::getName, Constant.ROOT)
                        .eq(Collector::getOwner, owner)
        );
    }
}
