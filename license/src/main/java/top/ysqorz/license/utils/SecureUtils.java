package top.ysqorz.license.utils;

import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecureUtils {

    @Test
    public void testSha256() {
        // 134563d4e440f0e418b0f382f23a2cf301af6d7f648ccfae9895018345d779a3
        System.out.println(sha256("Hello", "123"));
    }

    @Test
    public void testMd5() {
        // d0aabe9a362cb2712ee90e04810902f3
        System.out.println(md5("Hello", "123"));
    }

    @Test
    public void testEncryptByAES() {
        String keyStr = "1234567890123456"; // 16个字符，128位
        String plainText = "Hello World!";
        File cipherFile = new File("C:\\Users\\Administrator\\Desktop\\备份\\cipher\\cipher.txt");
        encryptByAES(keyStr, plainText, cipherFile);
    }

    @Test
    public void testDecryptByAES() {
        String keyStr = "1234567890123456"; // 16个字符，128位，必须和加密是同一把密钥
        File cipherFile = new File("C:\\Users\\Administrator\\Desktop\\备份\\cipher\\cipher.txt");
        System.out.println(decryptByAES(keyStr, cipherFile));
    }

    public static final int AES_BUFFER_LEN = 1024;

    public static void encryptByAES(String keyStr, String plainText, File cipherFile) {
        try {
            ByteArrayInputStream plainInputStream = new ByteArrayInputStream(plainText.getBytes(StandardCharsets.UTF_8));
            OutputStream cipherOutputStream = Files.newOutputStream(cipherFile.toPath());
            encryptByAES(keyStr, plainInputStream, cipherOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e); // 弱化异常
        }
    }

    public static String decryptByAES(String keyStr, File cipherFile) {
        try (FileInputStream cipherInputStream = new FileInputStream(cipherFile);
             ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
            decryptByAES(keyStr, cipherInputStream, byteOutputStream);
            return byteOutputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e); // 弱化异常
        }
    }

    /**
     * key必须是128, 192, 256位
     */
    public static void encryptByAES(String keyStr, InputStream plainInputStream, OutputStream cipherOutputStream) {
        aes(keyStr, plainInputStream, cipherOutputStream, Cipher.ENCRYPT_MODE, AES_BUFFER_LEN);
    }

    /**
     * @param keyStr 必须与加密的时候是同一把密钥
     */
    public static void decryptByAES(String keyStr, InputStream cipherInputStream, OutputStream plainOutputStream) {
        aes(keyStr, cipherInputStream, plainOutputStream, Cipher.DECRYPT_MODE, AES_BUFFER_LEN / 16 * 16 + 16);
    }

    public static void aes(String keyStr, InputStream inputStream, OutputStream outputStream, int mode, int bufLen) {
        try {
            Key key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, key);
            byte[] buf = new byte[bufLen];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                byte[] bytes = cipher.update(buf, 0, len);
                outputStream.write(bytes);
            }
            byte[] finalBytes = cipher.doFinal(); // 最后一个数据块
            outputStream.write(finalBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String sha256(String origin, String salt) {
        return digest("SHA-256", origin, salt);
    }

    public static String md5(String origin, String salt) {
        return digest("MD5", origin, salt);
    }

    private static String digest(String algorithm, String origin, String salt) {
        try {
            // 创建一个 MessageDigest 对象
            MessageDigest md = MessageDigest.getInstance(algorithm);
            // 计算 SHA-256 值
            byte[] digest = md.digest((origin + salt).getBytes(StandardCharsets.UTF_8));
            // 将盐值和 SHA-256 值一起转换为十六进制字符串
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    public static String bytesToHex(byte[] bytes, boolean lowerCase) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02" + (lowerCase ? 'x' : 'X'), b);
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
