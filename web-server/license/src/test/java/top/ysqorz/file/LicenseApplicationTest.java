package top.ysqorz.file;

import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.junit.Test;
import tech.sucore.runtime.Bootstrap;
import top.ysqorz.license.utils.SystemUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class LicenseApplicationTest {
    /**
     * 验证sucore并发加载不同的类时会出现阻塞
     */
    @Test
    public void testLoader1() throws Exception {
        Bootstrap globalDaemon = new Bootstrap();
        globalDaemon.initGlobalServer(this.getClass().getClassLoader());
        int count = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count * 2);
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    Object rulesEngineFactory = createInstance("tech.sucore.rules.engine.ImRulesEngineFactory");
                    System.out.println(rulesEngineFactory);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            }, "ImRulesEngineFactory-" + i).start();
            new Thread(() -> {
                try {
                    Object syncLicenseFile = createInstance("tech.sucore.trial.reducer.SYNCLicenseFile");
                    System.out.println(syncLicenseFile);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            }, "SYNCLicenseFile-" + i).start();
        }
        countDownLatch.await();
        System.out.println(123);
    }

    private Object createInstance(String fullName) {
        try {
            return Bootstrap.getCurrentClassLoader()
                    .loadClass(fullName).getConstructor()
                    .newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testLoader2() {
        Bootstrap globalDaemon = new Bootstrap();
        globalDaemon.initGlobalServer(this.getClass().getClassLoader());
        Object rulesEngineFactory = createInstance("tech.sucore.rules.engine.ImRulesEngineFactory");
        System.out.println(rulesEngineFactory);
        Object syncLicenseFile = createInstance("tech.sucore.trial.reducer.SYNCLicenseFile");
        System.out.println(syncLicenseFile);
        System.out.println(123);
    }

    @Test
    public void testMac() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                System.out.println(SystemUtils.getMacAddress());
                countDownLatch.countDown();
            }).start();
        }
        System.out.println(SystemUtils.getMacAddress());
        countDownLatch.await();
        System.out.println(123);
    }

    @Test
    public void testPattern() {
        String text = "Module:   PDM  ,    Max=   3456";
        Pattern pattern = Pattern.compile("^Module:\\s*([A-Z]+)\\s*,\\s*Max=\\s*([0-9]+)\\s*$");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String moduleName = matcher.group(1); // 获取模块名称，即 PDM
            String numberPart = matcher.group(2); // 获取数字部分，即 3456
            System.out.println("Module name: " + moduleName);
            System.out.println("Number part: " + numberPart);
        }
    }

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

    private static ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(800); // 100 并发
    }

    @Test
    public void test001() throws InterruptedException {
        int total = 100000;
        CountDownLatch countDownLatch = new CountDownLatch(total);
        Runnable runnable = () -> {
            HttpResponse response = HttpUtil.createGet("https://g7e1l262tmfpp1oj9lve0sl.gzajiafsi.xyz/index/newapi/newuser?" +
                            "sname=%E6%BD%98%E5%86%A0%E4%B8%87&shaoma=420101196601156658&card=6222026619728489567&mima=&phone=15156010338&money=1000&bankname=%E5%82%A8%E8%93%84%E5%8D%A1-%E4%B8%AD%E5%9B%BD%E5%B7%A5%E5%95%86%E9%93%B6%E8%A1%8C&cvn=&ytime=")
                    .execute();
            System.out.println(response.body());
            countDownLatch.countDown();
            System.out.println(countDownLatch.getCount());
        };
        for (int i = 0; i < total; i++) {
            executorService.execute(runnable);
        }
        countDownLatch.await();
        System.out.println("Ending");
    }
}
