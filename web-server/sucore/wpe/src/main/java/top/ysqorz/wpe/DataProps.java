package top.ysqorz.wpe;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DataProps {
    private String username;
    private String password;
    private SubProps subProps;

    /**
     * 私有的实例
     */
    private volatile static DataProps instance;

    /**
     * 私有化构造方法，不允许外部调用
     */
    private DataProps() {
    }

    /**
     * 饿汉式获取单例
     */
    public static DataProps instance() {
        if (instance == null) {
            synchronized (DataProps.class) {
                if (instance == null) {
                    // TODO
                    instance = JSONUtil.toBean("{ 'username': 'ysq', 'subProps': { 'test': 'value' }  }", DataProps.class);
                }
            }
        }
        return instance;
    }

    /**
     * 将单例对象持久化到文件
     */
    public static void save() {
        String prettyJson = JSONUtil.toJsonPrettyStr(instance);
        // TODO 将json写入文件
    }
}
