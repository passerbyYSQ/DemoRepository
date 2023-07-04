package top.ysqorz.migration.repos.zwt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.migration.repos.zwt.po.BaseEntity;

/**
 * 不加这个Mapper会报错：com.baomidou.mybatisplus.core.exceptions.MybatisPlusException: can not find lambda cache
 * for this entity [com.zwteamworks.auth.login.model.RelationEntity]
 */
public interface IBaseEntityMapper extends BaseMapper<BaseEntity> {
}
