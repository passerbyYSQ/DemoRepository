package top.ysqorz.file;

import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Unit test for simple App.
 */
public class FileApplicationTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        long start = System.currentTimeMillis();
        String md5 = SecureUtil.md5(new File("E:\\Download\\Chrome\\测试加密\\ZW3D_2024_All_Win_64bit.exe"));
        System.out.println("md5: " + md5);
        // d0495c8b4fd73adecebae400d171a666
        // d0495c8b4fd73adecebae400d171a666
        System.out.println((System.currentTimeMillis() - start) + " ms");
    }

    /**
     * AES加密和解密文件
     */
    @Test
    public void aesEncryptFile() throws IOException {
        // 随机生成密钥
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
        AES aes = SecureUtil.aes(key);
        BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(Paths.get("E:\\Download\\Chrome\\ZW3D_2024_All_Win_64bit.exe")));
        BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get("E:\\Download\\Chrome\\测试加密\\ZW3D_2024_All_Win_64bit.zwt")));
        byte[] buf = new byte[2048];
        int len;
        while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
            outputStream.write(aes.encrypt(Arrays.copyOf(buf, len)));
        }
        outputStream.close();
        inputStream.close();

        inputStream = new BufferedInputStream(Files.newInputStream(Paths.get("E:\\Download\\Chrome\\测试加密\\ZW3D_2024_All_Win_64bit.zwt")));
        outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get("E:\\Download\\Chrome\\测试加密\\ZW3D_2024_All_Win_64bit.exe")));
        buf = new byte[2048 / 16 * 16 + 16];
        while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
            outputStream.write(aes.decrypt(Arrays.copyOf(buf, len)));
        }
        outputStream.close();
        inputStream.close();
    }

    @Test
    public void testEncodeFilename() {
        // 原本：ysq-1,A,1:ysq-1
        // 错误：ysq-12C1%3Aysq-1
        // 正确：ysq-1%2CA%2C1%3Aysq-1
        System.out.println(URLUtil.encodeAll("ysq-1,A,1:ysq-1"));
    }
}
