package top.ysqorz.license.loader.cipher;

import java.io.*;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/4
 */
public interface ByteCodeCipherStrategy {
    void encrypt(InputStream inputStream, OutputStream outputStream);

    void encrypt(File srcFile, File destFile) throws IOException;

    void encrypt(File srcFile) throws IOException;

    void decrypt(InputStream inputStream, OutputStream outputStream);
}
