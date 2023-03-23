package top.ysqorz.wpe;

import cn.hutool.json.JSONUtil;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println(DataProps.instance().getUsername());
        DataProps.instance().setUsername("tianyu").setPassword("tianyu123");
        String json = JSONUtil.toJsonStr(DataProps.instance());
        System.out.println(json);
    }
}
