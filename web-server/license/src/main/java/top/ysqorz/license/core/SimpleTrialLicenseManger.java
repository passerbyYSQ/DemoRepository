package top.ysqorz.license.core;

import top.ysqorz.license.api.TrailLicenseManager;
import top.ysqorz.license.api.TrialLicenseCallback;
import top.ysqorz.license.api.TrialLicenseCipherStrategy;
import top.ysqorz.license.api.TrialLicense;
import top.ysqorz.license.utils.FileUtils;
import top.ysqorz.license.utils.SecureUtils;
import top.ysqorz.license.utils.SystemUtils;
import top.ysqorz.license.utils.TrialLicenseException;

import java.io.File;
import java.util.Objects;

public class SimpleTrialLicenseManger implements TrailLicenseManager {
    private final TrialLicenseCipherStrategy cipherStrategy;
    private TrialLicenseDaemon licenseDaemon;
    private File licenseFile;
    private TrialLicense license;

    public SimpleTrialLicenseManger(TrialLicenseCipherStrategy cipherStrategy, TrialLicense initialLicense) {
        initialLicense.check();
        this.cipherStrategy = cipherStrategy;
        this.license = initialLicense;
        this.licenseFile = getTrialLicenseFile();

        init();
    }

    protected void init() {
        // 处理文件名，以便在Linux下隐藏文件
        if (!licenseFile.getName().startsWith(".")) {
            licenseFile = new File(licenseFile.getParentFile(), "." + licenseFile.getName());
        }
        if (!licenseFile.exists()) { // 授权文本不存在说明是首次启动，生成授权文本
            FileUtils.createHiddenFile(licenseFile); // 隐藏文件
            license.markFirstStartup().markLastCheckTime(); // 填充额外的监控信息
            cipherStrategy.encrypt(license, licenseFile); // 写入加密后的字节
            //FileUtils.setStrictPermission(licenseFile); // 初始化文件之后，设置严格权限
        } else {
            license = cipherStrategy.decrypt(licenseFile); // TODO 将授权文本解密成内存上的数据对象，如果文本被篡改，此处会抛出异常
            if (Objects.isNull(license)) {
                throw new TrialLicenseException("试用授权许可解析异常，可能已被篡改");
            }
        }
        // 创建守护者
        licenseDaemon = new TrialLicenseDaemon(licenseFile, license, cipherStrategy);
    }

    @Override
    public void startDaemon(TrialLicenseCallback callback) {
        licenseDaemon.startDaemon(callback);
    }

    public Long getRemainedLicenseMillis() {
        return licenseDaemon.getRemainedLicenseMillis();
    }

    /**
     * 绝对不能让用户破译看到此段代码，知道试用授权文本的存放位置，否则用户每次强制将文件删除之后程序每次都是首次启动
     * <p>
     * Windows:
     * C:\ProgramData 是隐藏的
     * C:\\ProgramData\\{MAC地址}\\加密文件.cipher
     * <p>
     * Linux:
     * /var/lib/{MAC地址}\\加密文件.cipher
     * <p>
     * 找不到就认为是首次启动
     */
    @Override
    public File getTrialLicenseFile() {
        String macAddress = SystemUtils.getMacAddress();
        String appName = license.getLicense().getAppName();
        String cipherFilename = SecureUtils.md5(macAddress, appName) + ".cipher"; // Linux下隐藏文件
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
    public boolean validateExpiration() {
        return licenseDaemon.validateExpiration();
    }

    public File getLicenseFile() {
        return licenseFile;
    }

    public TrialLicense getLicense() {
        return license;
    }
}
