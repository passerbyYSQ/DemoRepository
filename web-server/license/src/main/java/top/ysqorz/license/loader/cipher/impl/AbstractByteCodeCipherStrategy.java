package top.ysqorz.license.loader.cipher.impl;

import top.ysqorz.license.loader.cipher.ByteCodeCipherStrategy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/4
 */
public abstract class AbstractByteCodeCipherStrategy implements ByteCodeCipherStrategy {
    @Override
    public void encrypt(File srcFile, File destFile) throws IOException {
        if (!srcFile.exists()) {
            throw new FileNotFoundException(srcFile.getAbsolutePath()); // 源文件必须存在
        }
        if (srcFile.isFile()) {
            if (!isSameSuffix(srcFile, destFile)) {
                // 当destFile是目录路径，不管是否存在
                if (!destFile.mkdirs()) {
                    throw new IOException("创建目录失败：" + destFile.getAbsolutePath());
                }
                destFile = new File(destFile, srcFile.getName());
            }
            processFile(srcFile, destFile);
        } else {
            encryptDir(srcFile, destFile);
        }
    }

    public boolean isSameSuffix(File srcFile, File destFile) {
        int srcIdx = srcFile.getName().lastIndexOf(".");
        int destIdx = destFile.getName().lastIndexOf(".");
        // 任意一个无后缀
        if (srcIdx == -1 || destIdx == -1) {
            return false;
        }
        // 两个都有后缀，则截取
        String srcSuffix = srcFile.getName().substring(srcIdx);
        String destSuffix = destFile.getName().substring(destIdx);
        return srcSuffix.equals(destSuffix);
    }

    private void processFile(File srcFile, File destFile) throws IOException {
        File tempFile = new File(destFile.getAbsolutePath() + ".tmp");
        // 处理成临时文件
        if (srcFile.getName().endsWith(".class")) { // 只加密.class文件
            FileInputStream srcInputStream = new FileInputStream(srcFile);
            FileOutputStream tempOutStream = new FileOutputStream(tempFile);
            encrypt(srcInputStream, tempOutStream);
            tempOutStream.close(); // 及时关闭资源，将缓冲区内容刷入临时文件
            srcInputStream.close();
        } else {
            Files.copy(srcFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        // 临时文件拷贝到目标文件
        Files.copy(tempFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        // 最后删除临时文件
        //noinspection ResultOfMethodCallIgnored
        tempFile.delete();
    }

    // 假设srcDir一定存在，destDir不存在
    public void encryptDir(File srcFile, File destFile) throws IOException {
        if (srcFile.isFile()) {
            processFile(srcFile, destFile);
            return;
        }
        String[] paths = srcFile.list();
        if (Objects.isNull(paths)) {
            return;
        }
        if (!destFile.exists()) {
            if (!destFile.mkdirs()) {
                throw new IOException("创建目录失败：" + destFile.getAbsolutePath());
            }
        }
        for (String path : paths) {
            encryptDir(new File(srcFile, path), new File(destFile, path));
        }
    }

    @Override
    public void encrypt(File srcFile) throws IOException {
        encrypt(srcFile, srcFile);
    }
}
