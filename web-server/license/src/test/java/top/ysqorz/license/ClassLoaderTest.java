package top.ysqorz.license;

import org.junit.Test;
import top.ysqorz.license.api.TrailLicenseManager;
import top.ysqorz.license.api.TrialLicense;
import top.ysqorz.license.api.TrialLicenseCipherStrategy;
import top.ysqorz.license.api.TrialLicenseTranslator;
import top.ysqorz.license.loader.TrialLicenseClassLoader;
import top.ysqorz.license.loader.cipher.ByteCodeCipherStrategy;
import top.ysqorz.license.loader.cipher.impl.AESByteCodeCipherStrategy;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/7
 */
public class ClassLoaderTest {
    private static final String AES_BYTE_CODE_CIPHER_KEY = "1234567890123456";

    /**
     * 加密top.ysqorz.license.core包下的字节码，该包下的字节码是加密的
     */
    @Test
    public void testEncryptCoreClassDir() throws IOException {
        ByteCodeCipherStrategy cipherStrategy = new AESByteCodeCipherStrategy(AES_BYTE_CODE_CIPHER_KEY);
        File srcDir = new File("E:\\Project\\IdeaProjects\\DemoRepository\\web-server\\license\\target\\classes\\top\\ysqorz\\license\\core");
        cipherStrategy.encrypt(srcDir); // 加密后直接覆盖原文件
    }

    /**
     * 测试加载加密的字节码
     */
    @Test
    public void testLoadCoreClass() throws Exception {
        ByteCodeCipherStrategy cipherStrategy = new AESByteCodeCipherStrategy(AES_BYTE_CODE_CIPHER_KEY);
        TrialLicenseClassLoader classLoader = new TrialLicenseClassLoader(cipherStrategy);
        TrialLicenseTranslator licenseTranslator = createXMLLicenseTranslator(classLoader);
        TrialLicenseCipherStrategy licenseCipherStrategy = createAESLicenseCipherStrategy(classLoader, licenseTranslator);

        TrialLicense trialLicense = new TrialLicense()
                .setAppName("testLoadCoreClass")
                .addModule("omf", "pdm", "zxt")
                .setCheckInterval(Duration.ofSeconds(10))
                .setDuration(Duration.ofMinutes(1));

        TrailLicenseManager licenseManger = createSimpleLicenseManger(classLoader, licenseCipherStrategy, trialLicense);
        if (licenseManger.validateExpiration()) {
            System.out.println("试用已过期11111");
            return;
        }
        licenseManger.startDaemon(license -> {
            System.out.println("试用已过期22222");
            System.exit(-1);
        });
        Thread.sleep(Duration.ofMinutes(2).toMillis());
    }

    public TrailLicenseManager createSimpleLicenseManger(TrialLicenseClassLoader classLoader,
                                                         TrialLicenseCipherStrategy licenseCipherStrategy,
                                                         TrialLicense trialLicense) throws Exception {
        return (TrailLicenseManager) classLoader.loadClass("top.ysqorz.license.core.SimpleTrialLicenseManger")
                .getConstructor(TrialLicenseCipherStrategy.class, TrialLicense.class)
                .newInstance(licenseCipherStrategy, trialLicense);
    }

    public TrialLicenseCipherStrategy createAESLicenseCipherStrategy(TrialLicenseClassLoader classLoader, TrialLicenseTranslator licenseTranslator) throws Exception {
        return (TrialLicenseCipherStrategy) classLoader.loadClass("top.ysqorz.license.core.cipher.AESLicenseCipherStrategy")
                .getConstructor(TrialLicenseTranslator.class, String.class)
                .newInstance(licenseTranslator, "1234567890123456");
    }

    public TrialLicenseTranslator createXMLLicenseTranslator(TrialLicenseClassLoader classLoader) throws Exception {
        return (TrialLicenseTranslator) classLoader.loadClass("top.ysqorz.license.core.translator.XMLicenseTranslator").newInstance();
    }
}
