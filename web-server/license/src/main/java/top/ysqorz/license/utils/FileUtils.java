package top.ysqorz.license.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;

public class FileUtils {
    /**
     * UTC时间
     */
    public static FileTime getCreateTime(File file) {
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

    public static void setLastModifiedTime(File file, FileTime fileTime) {
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
                    view.setSystem(true); // 设置true之后，Windows文件管理器勾选显示隐藏文件都无法显示
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
