package top.ysqorz.file.license;

import lombok.extern.slf4j.Slf4j;
import top.ysqorz.file.utils.SystemUtils;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 授权守护
 */
@Slf4j // TODO 传入调用方的Log
public class TrialLicenseDaemon {
    private Duration daemonDuration; // 守护间隔
    private File licenseFile;
    private TrialLicense license;
    private LicenseCallback callback;
    private TrialLicenseCipherStrategy cipherStrategy;

    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });
    private boolean notFirst;

    public TrialLicenseDaemon(Duration daemonDuration, File licenseFile, TrialLicense license, LicenseCallback callback,
            TrialLicenseCipherStrategy cipherStrategy) {
        this.daemonDuration = daemonDuration;
        this.licenseFile = licenseFile;
        this.license = license;
        this.callback = callback;
        this.cipherStrategy = cipherStrategy;
    }

    /**
     * 开启守护线程
     */
    public void startDaemon() {
        scheduledExecutor.scheduleAtFixedRate(new DaemonTask(), 0, daemonDuration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 校验授权时间
     *
     * @return  是否已经过期，true：已过期
     */
    public boolean validateExpiration() {
        // 校验当前设备信息
        String macAddress = license.getSystem().getMacAddress();
        if (!macAddress.equals(SystemUtils.getMacAddress())) {
            log.error("当前设备信息与首次安装启动的设备信息不符，程序安装之后可能又被迁移至其他设备");
            return false;
        }

        long currentTime = System.currentTimeMillis(); // 当前时间
        Long firstStartupTime = license.getMonitor().getFirstStartup(); // 首次启动时间
        Long lastCheckTime = license.getMonitor().getLastCheckTime(); // 上一次打点时间
        Long runningDuration = license.getMonitor().getRunningDuration(); // 程序已经累计运行的时长
        Long licenseDuration = license.getLicense().getDuration(); // 授权试用的时长

        // 正常试用过期
        if (currentTime - firstStartupTime > licenseDuration) {
            log.error("授权已过期");
            return true;
        }

        // 防君子：防止用户篡改操作系统时间，将操作系统调成一个很早的时间
        if (currentTime <= firstStartupTime) {
            log.error("当前时间早于首次启动时间，操作系统时间可能已经被篡改");
            return true;
        }
        // 防小人
        if (notFirst) { // 本轮监控非首次检查，需要检查上一次打点时间
            // 当前时间 - 上一次打点时间 <= 守护间隔，否则就是守护线程出现异常
            // 防止用户首次启动之后，程序运行了一段时间，用户又把操作系统的时间调味首次启动之后
            // 这样之后用户只能将操作系统时间调成上一次打点时间之后的一个守护间隔内(比如10分钟)，如果守护间隔比较大，那么用户可以慢慢薅羊毛
            if (currentTime - lastCheckTime > daemonDuration.toMillis()) {
                log.error("操作系统时间可能已经被篡改");
                return true;
            }
        }
        notFirst = true;
        // 防奸人
        return runningDuration > licenseDuration;
    }

    public class DaemonTask implements Runnable {
        @Override
        public void run() {
            if (validateExpiration()) { // 已经过期
                callback.licenseExpired(license); // 过期时的具体行为必须由调用方来提供
                scheduledExecutor.shutdownNow(); // 停止守护线程，自己停止自己，是否死循环？？？TODO
                log.error("授权已过期");
            } else {
                license.markLastCheckTime(); // 更新上一次打点时间
                license.addRunningDuration(daemonDuration); // 加上累计运行的时间
                cipherStrategy.encrypt(license, licenseFile); // 持久化到磁盘，覆盖更新
                log.info("授权监控");
            }
        }
    }
}
