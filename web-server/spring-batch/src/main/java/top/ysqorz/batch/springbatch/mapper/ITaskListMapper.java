package top.ysqorz.batch.springbatch.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.ysqorz.batch.springbatch.model.po.TaskList;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/19
 */
public interface ITaskListMapper extends BaseMapper<TaskList> {
    default TaskList selectByUserID(String userID) {
        return selectOne(
                new QueryWrapper<TaskList>().lambda()
                        .eq(TaskList::getUserID, userID)
        );
    }
}
