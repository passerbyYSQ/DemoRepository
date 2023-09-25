package top.ysqorz.lenovo;

import cn.hutool.json.JSONObject;

import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/13
 */
public class ImportRelPrjRoleExecutor {
    public static void main(String[] args) {
        List<JSONObject> prefUsers = getPrefUsers();
        JSONObject reposPrjTeam = getReposPrjTeam();
        for (JSONObject user : prefUsers) {
            createVisitorRelPrjRole(reposPrjTeam, user);
        }
        System.out.println("创建关系数量：" + prefUsers.size());
    }

    public static JSONObject createVisitorRelPrjRole(JSONObject prjTeam, JSONObject user) {
        JSONObject creator = new JSONObject()
                .set("Name", "访客");
        return ZWTUtils.createRelation("rPrjRole", prjTeam, user, creator);
    }

    public static JSONObject getReposPrjTeam() {
        JSONObject filter = new JSONObject()
                .set("Name", "存储库");
        List<JSONObject> roles = ZWTUtils.queryObject("PrjTeam", filter);
        return roles.get(0);
    }

    public static JSONObject getVisitorRole() {
        JSONObject filter = new JSONObject()
                .set("Name", "访客");
        List<JSONObject> roles = ZWTUtils.queryObject("Role", filter);
        return roles.get(0);
    }

    public static List<JSONObject> getPrefUsers() {
        JSONObject filter = new JSONObject()
                .set("Creator", "system");
        return ZWTUtils.queryObject("CoreUser", filter);
    }


}
