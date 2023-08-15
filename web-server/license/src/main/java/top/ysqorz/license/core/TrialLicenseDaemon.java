package top.ysqorz.license.core;

import top.ysqorz.license.api.TrialLicenseCallback;
import top.ysqorz.license.api.TrialLicenseCipherStrategy;
import top.ysqorz.license.api.TrialLicense;
import top.ysqorz.license.utils.FileUtils;
import top.ysqorz.license.utils.SystemUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 授权守护
 */
public class TrialLicenseDaemon {
    private static final Logger log = Logger.getLogger(TrialLicenseDaemon.class.getName());
    private final File licenseFile;
    private final TrialLicense license;
    private TrialLicenseCallback callback;
    private final TrialLicenseCipherStrategy cipherStrategy;

    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });
    private boolean notFirst;

    public TrialLicenseDaemon(File licenseFile, TrialLicense license,
                              TrialLicenseCipherStrategy cipherStrategy) {
        this.licenseFile = licenseFile;
        this.license = license;
        this.cipherStrategy = cipherStrategy;
    }

    /**
     * 开启守护线程
     */
    public void startDaemon(TrialLicenseCallback callback) {
        if (Objects.isNull(this.callback)) {
            log.info(String.format("试用授权时长: %d ms, 剩余试用时长: %d ms", license.getLicense().getDuration(), getRemainedLicenseMillis()));
            this.callback = callback;
            Long checkInterval = license.getLicense().getCheckInterval();
            scheduledExecutor.scheduleAtFixedRate(new DaemonTask(), checkInterval, checkInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 剩余试用时长，注意试用授权时长是从首次启动就开始计时，不管程序程序是否在运行
     */
    public Long getRemainedLicenseMillis() {
        return license.getMonitor().getFirstStartup() + license.getLicense().getDuration() - System.currentTimeMillis();
    }

    /**
     * 校验授权时间
     *
     * @return 是否已经过期，true：已过期
     */
    public boolean validateExpiration() {
        // 校验当前设备信息
        String macAddress = license.getSystem().getMacAddress();
        if (!macAddress.equals(SystemUtils.getMacAddress())) {
            log.info("当前设备信息与首次安装启动的设备信息不符，程序安装之后可能又被迁移至其他设备");
            return false;
        }

        long currentTime = System.currentTimeMillis(); // 当前时间
        Long firstStartupTime = license.getMonitor().getFirstStartup(); // 首次启动时间
        Long lastCheckTime = license.getMonitor().getLastCheckTime(); // 上一次打点时间
        Long runningDuration = license.getMonitor().getRunningDuration(); // 程序已经累计运行的时长
        Long licenseDuration = license.getLicense().getDuration(); // 授权试用的时长
        Long checkInterval = license.getLicense().getCheckInterval(); // 检查间隔

        // 正常试用过期
        if (currentTime - firstStartupTime >= licenseDuration) {
            log.info("试用授权已过期");
            return true;
        }

        // 防君子：防止用户篡改操作系统时间，将操作系统调成一个很早的时间
        if (currentTime <= firstStartupTime) {
            log.info("当前时间早于首次启动时间，操作系统时间可能已经被篡改");
            return true;
        }
        // 防小人
        if (notFirst) { // 本轮启动非首次监控才存在上一次打点时间，此时才需要检查上一次打点时间
            // 当前时间 - 上一次打点时间 <= 守护间隔，否则就是守护线程出现异常
            // 防止用户首次启动之后，程序运行了一段时间，用户又把操作系统的时间调味首次启动之后
            // 这样之后用户只能将操作系统时间调成上一次打点时间之后的一个守护间隔内(比如10分钟)，如果守护间隔比较大，那么用户可以慢慢薅羊毛
            // 定时器可能有延迟误差，导致currentTime - lastCheckTime 偏大，故允许有checkInterval/10的误差
            long tolerance = (long) Math.ceil(checkInterval / 10.0);
            if (currentTime - lastCheckTime > checkInterval + tolerance) {
                log.info("试用授权检测：操作系统时间可能已经被篡改");
                return true;
            }
        }
        // 防奸人
        return runningDuration > licenseDuration;
    }

    public class DaemonTask implements Runnable {
        @Override
        public void run() {
            boolean expired = validateExpiration(); // 已经过期
            Long lastCheckTime = license.getMonitor().getLastCheckTime();
            license.markLastCheckTime(); // 更新上一次打点时间
            license.addRunningDuration(license.getLicense().getCheckInterval()); // 加上累计运行的时间
            cipherStrategy.encrypt(license, licenseFile); // 持久化到磁盘，覆盖更新，即使已经检查到期，在停止之前也要更新一遍
            // 重置修改时间，防止用户查找最近修改的文件来猜出试用授权文件
            FileUtils.setLastModifiedTime(licenseFile, FileUtils.getCreateTime(licenseFile));

            log.info(String.format("授权监控: 打点, LastCheckTime %s, RunningDuration %d ms, RemainedDuration %d ms, CheckDifDuration %d ms",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(license.getMonitor().getLastCheckTime()), // 最新的时间
                    license.getMonitor().getRunningDuration(), getRemainedLicenseMillis(),
                    (license.getMonitor().getLastCheckTime() - lastCheckTime)));
            notFirst = true; // 不能写在validateExpiration()里面，否则外部手动调用validateExpiration()之后，会造成守护线程误判

            if (expired) { // 通知过期
                callback.licenseExpired(license); // 过期时的具体行为必须由调用方来提供
            }
        }
    }
}
