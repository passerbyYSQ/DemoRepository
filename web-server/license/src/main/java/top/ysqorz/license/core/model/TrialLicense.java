package top.ysqorz.license.core.model;

import top.ysqorz.license.utils.SystemUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 授权文本中要包含的内容信息
 */
public class TrialLicense {
    private final License license = new License();
    private final Monitor monitor = new Monitor();
    private final System system = new System();

    public void check() {
        license.check();
    }

    public TrialLicense setDuration(Duration duration) {
        license.setDuration(duration.toMillis());
        return this;
    }

    public TrialLicense setCheckInterval(Duration checkInterval) {
        license.setCheckInterval(checkInterval.toMillis());
        return this;
    }

    public TrialLicense addModule(String module) {
        license.getModules().add(module);
        return this;
    }

    public TrialLicense addRunningDuration(Long duration) {
        Long temp = monitor.getRunningDuration() + duration;
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

    public License getLicense() {
        return license;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public System getSystem() {
        return system;
    }

    /**
     * 授权相关信息
     */
    public static class License {
        /**
         * 授权时长，单位ms
         */
        private Long duration = Duration.ofDays(90).toMillis();

        /**
         * 检测间隔，单位ms
         */
        private Long checkInterval = Duration.ofMinutes(10).toMillis();

        /**
         * 授权的应用名称，确保唯一
         */
        private String appName;

        /**
         * 授权模块，*表示所有模块
         */
        private List<String> modules = new ArrayList<>();

        public void check() {
            assert duration > checkInterval;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }

        public Long getCheckInterval() {
            return checkInterval;
        }

        public void setCheckInterval(Long checkInterval) {
            this.checkInterval = checkInterval;
        }

        public List<String> getModules() {
            return modules;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }
    }

    /**
     * 监控信息
     */
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

        public Long getFirstStartup() {
            return firstStartup;
        }

        public void setFirstStartup(Long firstStartup) {
            this.firstStartup = firstStartup;
        }

        public Long getLastCheckTime() {
            return lastCheckTime;
        }

        public void setLastCheckTime(Long lastCheckTime) {
            this.lastCheckTime = lastCheckTime;
        }

        public Long getRunningDuration() {
            return runningDuration;
        }

        public void setRunningDuration(Long runningDuration) {
            this.runningDuration = runningDuration;
        }
    }

    public static class System {
        /**
         * 安装设备的mac地址
         */
        private final String macAddress;

        public System() {
            macAddress = SystemUtils.getMacAddress();
        }

        public String getMacAddress() {
            return macAddress;
        }
    }
}
