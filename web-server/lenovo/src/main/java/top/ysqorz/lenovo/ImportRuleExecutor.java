package top.ysqorz.lenovo;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/7
 */
public class ImportRuleExecutor {
    public static void main(String[] args) {
        List<String> relations = ZWTUtils.parseJSONConfig("business_relation.json").getBeanList("relations", String.class);
        for (String relation : relations) {
            passBusinessRelationByRule(relation);
        }
    }

    public static JSONObject passBusinessRelationByRule(String className) {
        JSONObject creator = new JSONObject()
                .set("IsExternMessage", "+")
                .set("ImpactInheritances", "+")
                .set("ParticipantID", "*")
                .set("ParticipantClass", "CoreUser")
                .set("ClassName", className) // 放行的关系名
                .set("IsMessageGroup", "-")
                .set("MessageName", "*")
                .set("ConditionName", "TRUE")
                .set("Description", "系统规则：开放" + className + "及其子类的外部消息权限")
                .set("ImpactChild", "+");
        try {
            return ZWTUtils.createObject("Rule", creator);
        } catch (Exception ex) {
            System.out.printf("创建Rule失败，creator：%s，错误信息：%s%n", JSONUtil.toJsonStr(creator), ex.getMessage());
            return null;
        }
    }
}
