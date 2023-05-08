package top.ysqorz.file.utils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;

public class FileUtils {
    @Test
    public void testHiddenFile() {
        System.out.println(createHiddenFile(new File("C:\\Users\\Administrator\\Desktop\\备份\\hidden.txt")));
    }

    /**
     * Windows:
     * C:\ProgramData 是隐藏的
     * C:\\ProgramData\\{MAC地址}\\加密文件.cipher
     *
     * Linux:
     * /var/lib/{MAC地址}\\加密文件.cipher
     *
     */
    @Test
    public void testProgramDataDir() {
        String programDataDir = System.getenv("ProgramData");
        System.out.println(programDataDir);
    }

    @Test
    public void testCreateTime() {
        FileTime createTime = getCreateTime(new File("C:\\Users\\Administrator\\Desktop\\备份\\.hidden.txt"));
        System.out.println(createTime);
    }

    @Test
    public void setLastModifiedTime() {
        File file = new File("C:\\Users\\Administrator\\Desktop\\备份\\.hidden.txt");
        FileTime createTime = getCreateTime(file);
        setLastModifiedTime(file, createTime);
    }

    /**
     * UTC时间
     */
    public FileTime getCreateTime(File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attrs.creationTime();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLastModifiedTime(File file, FileTime fileTime) {
        if (!file.exists()) {
            return;
        }
        try {
            Files.setLastModifiedTime(file.toPath(), fileTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean createHiddenFile(File file) {
        if (file.exists()) { // 已存在
            return false;
        }
        File dir = file.getParentFile();
        if (!file.getName().startsWith(".")) { // 兼容Linux
            file = new File(dir, "." + file.getName());
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) { // 祖先目录不存在则尝试创建
                return false;
            }
        }
        try {
            if (file.createNewFile()) {
                // Windows
                if (SystemUtils.isWindows()) {
                    DosFileAttributeView view = Files.getFileAttributeView(file.toPath(), DosFileAttributeView.class);
                    //DosFileAttributes attrs = view.readAttributes();
                    view.setHidden(true);
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
