package top.ysqorz.license.core.cipher;

import top.ysqorz.license.api.TrialLicenseCipherStrategy;
import top.ysqorz.license.api.TrialLicense;
import top.ysqorz.license.api.TrialLicenseTranslator;

import java.io.File;

public abstract class AbstractLicenseCipherStrategy implements TrialLicenseCipherStrategy {
    protected TrialLicenseTranslator translator;

    public AbstractLicenseCipherStrategy(TrialLicenseTranslator translator) {
        this.translator = translator;
    }

    @Override
    public void encrypt(TrialLicense license, File cipherFile) {
        encrypt0(translator.translate(license), cipherFile);
    }

    @Override
    public TrialLicense decrypt(File cipherFile) {
        try {
            return translator.translate(decrypt0(cipherFile));
        } catch (Exception e) {
            e.printStackTrace(); // 打印解析错误的异常
            return null;
        }
    }

    protected abstract void encrypt0(String plainText, File cipherFile);
    protected abstract String decrypt0(File cipherFile);
}
