package top.ysqorz.license;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import top.ysqorz.license.core.SimpleTrialLicenseManger;
import top.ysqorz.license.core.cipher.AESLicenseCipherStrategy;
import top.ysqorz.license.api.TrialLicense;
import top.ysqorz.license.core.translator.JSONLicenseTranslator;

import java.time.Duration;

/**
 * Hello world!
 */
@SpringBootApplication
@Slf4j
public class LicenseApplication {
    public static void main(String[] args) {
        // 文件的数据格式是JSON，使用AES加解密文件
        // AES的密钥是有长度要求的，这个密钥不能让用户破译看到
        AESLicenseCipherStrategy cipherStrategy = new AESLicenseCipherStrategy(new JSONLicenseTranslator(), "1234567890123456");
        // 初始的试用授权信息
        TrialLicense initialLicense = new TrialLicense()
                .setDuration(Duration.ofMinutes(5)) // 授权试用时长
                .addModule("*"); // 授权所有模块
        SimpleTrialLicenseManger licenseManager = new SimpleTrialLicenseManger(cipherStrategy, initialLicense);
        if (licenseManager.validateExpiration()) {
            return;
        }

        ConfigurableApplicationContext applicationContext = SpringApplication.run(LicenseApplication.class, args);
        // 启动成功之后开启守护
        licenseManager.startDaemon(license -> {
            if (license.containModule("*")) {
                applicationContext.close();
            }
        });
    }
}