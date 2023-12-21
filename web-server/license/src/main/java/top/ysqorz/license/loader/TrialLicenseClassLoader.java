package top.ysqorz.license.loader;

import top.ysqorz.license.loader.cipher.ByteCodeCipherStrategy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/6
 */
public class TrialLicenseClassLoader extends URLClassLoader {
    public static final String PACKAGE_PATH = "top.ysqorz.license.core";
    private final Map<String, Class<?>> privateClassCache = new ConcurrentHashMap<>();
    private final ByteCodeCipherStrategy cipherStrategy;

    public TrialLicenseClassLoader(ByteCodeCipherStrategy cipherStrategy, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.cipherStrategy = cipherStrategy;
    }

    public TrialLicenseClassLoader(ByteCodeCipherStrategy cipherStrategy) {
        this(cipherStrategy, new URL[]{}, TrialLicenseClassLoader.class.getClassLoader());
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // 先判断是不是加密类，如果是加密类，直接由自定义类加载器加载，否则走双亲委派
        if (isPrivateClass(name)) {
            return findClass(name); // findClass内部要加缓存，因为此处是在双亲委派之外，没法命中双亲委派的缓存
        }
        try {
            // 走双亲委派的逻辑，内部会有缓存
            return super.loadClass(name);
        } catch (ClassFormatError ignored) {
            // 兜底操作
            // 双亲委派自上往下加载时，父加载器不认得加密后的字节码，会抛出ClassFormatError，终止双亲委派的逻辑
            // Error被自定义的类加载捕获，然后尝试加载
            return findClass(name);
        }
    }

    public boolean isPrivateClass(String name) {
        return name.startsWith(PACKAGE_PATH);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!isPrivateClass(name)) {
            return null;
        }
        try {
            Class<?> clazz;
            if (Objects.isNull(clazz = privateClassCache.get(name))) { // 双重检测锁
                synchronized (privateClassCache) { // 并发安全，防止多线程对同一个字节码文件进行解密
                    if (Objects.isNull(clazz = privateClassCache.get(name))) {
                        String relativePath = name.replace(".", "/") + ".class";
                        InputStream inputStream = getResourceAsStream(relativePath);
                        if (Objects.isNull(inputStream)) {
                            return null;
                        }
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        cipherStrategy.decrypt(inputStream, outputStream);
                        clazz = defineClass(name, outputStream.toByteArray(), 0, outputStream.size());
                        privateClassCache.put(name, clazz); // 缓存解密后的字节码
                    }
                }
            }
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException("字节码解密失败", e);
        }
    }
}
