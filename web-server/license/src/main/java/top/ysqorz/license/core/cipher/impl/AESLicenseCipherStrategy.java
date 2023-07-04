package top.ysqorz.license.core.cipher.impl;

import top.ysqorz.license.core.translator.TrialLicenseTranslator;
import top.ysqorz.license.utils.SecureUtils;

import java.io.File;

public class AESLicenseCipherStrategy extends AbstractLicenseCipherStrategy {
    private final String key;

    public AESLicenseCipherStrategy(TrialLicenseTranslator translator, String key) {
        super(translator);
        this.key = key;
    }

    @Override
    protected void encrypt0(String plainText, File cipherFile) {
        SecureUtils.encryptByAES(key, plainText, cipherFile);
    }

    @Override
    protected String decrypt0(File cipherFile) {
        return SecureUtils.decryptByAES(key, cipherFile);
    }
}
