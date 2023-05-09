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
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class LicenseApplicationTest {
    @Test
    public void testPrinciple() {
        System.out.println(System.getProperty("user.name"));
    }
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
     * 测试AES加密和解密文件
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

    /**
     * 测试URL编码
     */
    @Test
    public void testEncodeFilename() {
        // 原本：ysq-1,A,1:ysq-1
        // 错误：ysq-12C1%3Aysq-1
        // 正确：ysq-1%2CA%2C1%3Aysq-1
        System.out.println(URLUtil.encodeAll("ysq-1,A,1:ysq-1"));
    }

    /**
     * 测试String的joiner，只有一个元素时是否追加分隔符
     */
    @Test
    public void testJoiner() {
        System.out.println("[" + String.join(System.lineSeparator(), "哈哈") + "]");
    }

    /**
     * 测试不定长参数
     */
    @Test
    public void testFunc() {
        func1("123", "哈哈");
    }

    private void func1(Object... objs) {
        func(objs);
    }

//    private void func2(String... strs) {
//        func(strs); // warning
//    }

    private void func(Object... objs) {
        System.out.println(Arrays.toString(objs));
    }

    @Test
    public void testAssert() {
        String abc = null;
        assert abc != null : "abc must be not null";
    }

    @Test
    public void testHashMap() {
        Map<Object, Object> map = new HashMap<>();
        map.put(null, "123");
        System.out.println(123);
    }
}
