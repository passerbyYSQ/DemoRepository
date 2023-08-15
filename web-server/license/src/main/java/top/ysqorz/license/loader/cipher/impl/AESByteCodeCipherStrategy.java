package top.ysqorz.license.loader.cipher.impl;

import top.ysqorz.license.utils.SecureUtils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/4
 */
public class AESByteCodeCipherStrategy extends AbstractByteCodeCipherStrategy {
    private final String key;

    public AESByteCodeCipherStrategy(String key) {
        this.key = key;
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) {
        SecureUtils.encryptByAES(key, inputStream, outputStream);
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) {
        SecureUtils.decryptByAES(key, inputStream, outputStream);
    }
}
