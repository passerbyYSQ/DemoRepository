package top.ysqorz.file.license;

import top.ysqorz.file.utils.FileUtils;
import top.ysqorz.file.utils.SecureUtils;
import top.ysqorz.file.utils.SystemUtils;

import java.io.File;
import java.time.Duration;

public class SimpleTrialLicenseManger implements TrailLicenseManager {
    private TrialLicenseCipherStrategy cipherStrategy;
    private TrialLicenseDaemon licenseDaemon;
    private Duration licenseDuration;
    private LicenseCallback callback;
    private File licenseFile;
    private TrialLicense license;

    public SimpleTrialLicenseManger(TrialLicenseCipherStrategy cipherStrategy,
                                    Duration licenseDuration, LicenseCallback callback) {
        this.cipherStrategy = cipherStrategy;
        this.licenseDuration = licenseDuration;
        this.callback = callback;
        this.licenseFile = getTrialLicenseFile();

        init();
    }

    private void init() {
        if (!licenseFile.exists()) { // 授权文本不存在说明是首次启动，生成授权文本
            FileUtils.createHiddenFile(licenseFile); // 隐藏文件
            cipherStrategy.encrypt(license = generateInitialLicense(), licenseFile); // 写入加密后的字节
        } else {
            license = cipherStrategy.decrypt(licenseFile); // 将授权文本解密成内存上的数据对象
        }

        // 启动守护线程
        this.licenseDaemon = new TrialLicenseDaemon(Duration.ofMinutes(1), licenseFile, license, callback, cipherStrategy);
        this.licenseDaemon.startDaemon();
    }

    /**
     * Windows:
     * C:\ProgramData 是隐藏的
     * C:\\ProgramData\\{MAC地址}\\加密文件.cipher
     * <p>
     * Linux:
     * /var/lib/{MAC地址}\\加密文件.cipher
     *
     * 找不到就认为是首次启动
     */
    @Override
    public File getTrialLicenseFile() {
        String macAddress = SystemUtils.getMacAddress();
        String cipherFilename = SecureUtils.md5(macAddress, this.getClass().getSimpleName()) + ".cipher"; // Linux下隐藏文件
        String subPath = String.join(File.separator, macAddress, cipherFilename);
        String parentPath;
        if (SystemUtils.isWindows()) {
            parentPath = System.getenv("ProgramData");
        } else {
            parentPath = String.join("var", "lib");
        }
        return new File(parentPath, subPath);
    }

    @Override
    public TrialLicense generateInitialLicense() {
        return new TrialLicense()
                .setDuration(licenseDuration)
                .addModule("*")
                .markFirstStartup()
                .markLastCheckTime();
    }

    @Override
    public boolean validateExpiration() {
        return licenseDaemon.validateExpiration();
    }
}
