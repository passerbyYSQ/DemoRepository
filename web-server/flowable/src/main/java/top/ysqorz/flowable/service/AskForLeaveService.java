package top.ysqorz.flowable.service;

import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ysqorz.flowable.vo.AskForLeaveVO;

import java.util.HashMap;
import java.util.Map;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/5/24
 */
@Service
public class AskForLeaveService {
    @Autowired
    private RuntimeService runtimeService;

    public String askForLeave(AskForLeaveVO askForLeaveVO) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", askForLeaveVO.getName());
        variables.put("days", askForLeaveVO.getDays());
        variables.put("reason", askForLeaveVO.getReason());
        try {
            runtimeService.startProcessInstanceByKey("holidayRequest", askForLeaveVO.getName(), variables);
            return "已提交请假申请";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "提交请假申请失败";
    }
}
