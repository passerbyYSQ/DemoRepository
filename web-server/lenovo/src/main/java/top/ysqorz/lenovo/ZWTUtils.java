package top.ysqorz.lenovo;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/5
 */
public class ZWTUtils {
    private final static JSONObject config;

    static {
        config = parseJSONConfig("config.json");
    }

    public static JSONObject parseJSONConfig(String configPath) {
        return JSONUtil.parseObj(IoUtil.readUtf8(ResourceUtil.getStream(configPath)));
    }

    public static JSONObject createRelation(String relation, JSONObject objL, JSONObject objR, JSONObject creator) {
        JSONObject params = new JSONObject()
                .set("relation", relation)
                .set("objL", objL)
                .set("objR", objR)
                .set("creator", creator.set("Creator", "system"));
        IPCRequestDTO reqDTO = new IPCRequestDTO("CreateRelation2", "relation", params);
        JSONObject rel = dispatch(reqDTO).getJSONObject("objRel");
        System.out.println(JSONUtil.toJsonStr(rel));
        return rel;
    }

    public static JSONObject createObject(String className, JSONObject creator) {
        JSONObject params = new JSONObject()
                .set("clsName", className)
                .set("creator", creator.set("Creator", "system"));
        IPCRequestDTO reqDTO = new IPCRequestDTO("CreateObject2", "clsName", params);
        JSONObject obj = dispatch(reqDTO).getJSONObject("objNew");
        System.out.println(JSONUtil.toJsonStr(obj));
        return obj;
    }

    public static List<JSONObject> queryObject(String className, JSONObject filter) {
        JSONObject params = new JSONObject()
                .set("clsName", className)
                .set("filter", filter.set("IsDepleted", "-"));
        IPCRequestDTO reqDTO = new IPCRequestDTO("QueryObject", "clsName", params);
        return dispatch(reqDTO).getBeanList("objset", JSONObject.class);
    }

    public static JSONObject dispatch(IPCRequestDTO reqDTO) {
        try (HttpResponse response = HttpUtil.createPost(getServer() + "/core/message/dispatch")
                .header("Authorization", getJWT())
                .body(JSONUtil.toJsonStr(reqDTO))
                .execute()) {
            JSONObject result = JSONUtil.parseObj(response.body());
            if (result.getInt("code") != 0) {
                throw new RuntimeException(result.getStr("msg"));
            }
            return result.getByPath("data.params", JSONObject.class);
        }
    }

    public static String getServer() {
        return config.getStr("url");
    }

    public static String getJWT() {
        return config.getStr("jwt");
    }
}
