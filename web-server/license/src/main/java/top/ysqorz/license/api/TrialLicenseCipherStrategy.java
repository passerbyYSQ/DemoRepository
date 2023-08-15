package top.ysqorz.license.api;

import java.io.File;

/**
 * 将来扩展使用不同的加密和解密策略，现在采用AES
 */
public interface TrialLicenseCipherStrategy {
    /**
     * @param license
     * @param cipherFile    加密文件
     */
    void encrypt(TrialLicense license, File cipherFile);

    /**
     * @param cipherFile    加密文件
     * @return              解密之后的明文
     */
    TrialLicense decrypt(File cipherFile);
}
