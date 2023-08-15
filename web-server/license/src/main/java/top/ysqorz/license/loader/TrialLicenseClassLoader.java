package top.ysqorz.license.loader;

import top.ysqorz.license.loader.cipher.ByteCodeCipherStrategy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/6
 */
public class TrialLicenseClassLoader extends ClassLoader {
    public static final String PACKAGE_PATH = "top.ysqorz.license.core";
    private final ByteCodeCipherStrategy cipherStrategy;

    public TrialLicenseClassLoader(ByteCodeCipherStrategy cipherStrategy) {
        this.cipherStrategy = cipherStrategy;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassFormatError ignored) {
            return findClass(name);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!name.startsWith(PACKAGE_PATH)) {
            return null;
        }
        // top.ysqorz.license.core.xxx.SimpleTrialLicenseManger
        String relativePath = name.substring(PACKAGE_PATH.length() + 1).replace(".", "/");
        URL resource = this.getClass().getResource("../core/" + relativePath + ".class");
        if (Objects.isNull(resource)) {
            return null;
        }
        try {
            FileInputStream inputStream = new FileInputStream(new File(resource.toURI()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            cipherStrategy.decrypt(inputStream, outputStream);
//            FileUtil.writeBytes(outputStream.toByteArray(), new File("C:\\Users\\Administrator\\Desktop\\hidden\\字节码解密\\test.class"));
            return defineClass(name, outputStream.toByteArray(), 0, outputStream.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException("字节码解密失败", e);
        }
    }
}
