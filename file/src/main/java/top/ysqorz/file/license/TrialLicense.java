package top.ysqorz.file.license;

import lombok.Data;
import lombok.experimental.Accessors;
import top.ysqorz.file.utils.SystemUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 授权文本中要包含的内容信息
 */
@Data
@Accessors(chain = true)
public class TrialLicense {
    private License license = new License();
    private Monitor monitor = new Monitor();
    private System system = new System();

    public TrialLicense setDuration(Duration duration) {
        license.setDuration(duration.toMillis());
        return this;
    }

    public TrialLicense addModule(String module) {
        license.getModules().add(module);
        return this;
    }

    public TrialLicense addRunningDuration(Duration duration) {
        Long temp = monitor.getRunningDuration() + duration.toMillis();
        monitor.setRunningDuration(temp);
        return this;
    }

    public boolean containModule(String module) {
        List<String> modules = license.getModules();
        return modules.contains("*") || modules.contains(module);
    }

    public TrialLicense markFirstStartup() {
        monitor.setFirstStartup(java.lang.System.currentTimeMillis());
        return this;
    }

    public TrialLicense markLastCheckTime() {
        monitor.setLastCheckTime(java.lang.System.currentTimeMillis());
        return this;
    }


    /**
     * 授权相关信息
     */
    @Data
    public static class License {
        /**
         * 授权时长，单位ms
         */
        private Long duration;

        /**
         * 授权模块，*表示所有模块
         */
        private List<String> modules = new ArrayList<>();
    }

    /**
     * 监控信息
     */
    @Data
    public static class Monitor {
        /**
         * 首次启动时间。UTC时间，ms
         */
        private Long firstStartup;

        /**
         * 上一次检查程序活跃的时间
         */
        private Long lastCheckTime;

        /**
         * 程序已经运行的时间
         */
        private Long runningDuration = 0L;
    }

    @Data
    public static class System {
        /**
         * 安装设备的mac地址
         */
        private String macAddress;

        public System() {
            macAddress = SystemUtils.getMacAddress();
        }
    }
}
