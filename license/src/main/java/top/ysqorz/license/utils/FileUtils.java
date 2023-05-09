package top.ysqorz.license.utils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class FileUtils {
    @Test
    public void testHiddenFile() {
        System.out.println(createHiddenFile(new File("C:\\Users\\Administrator\\Desktop\\备份\\hidden.txt")));
    }

    /**
     * Windows:
     * C:\ProgramData 是隐藏的
     * C:\\ProgramData\\{MAC地址}\\加密文件.cipher
     * <p>
     * Linux:
     * /var/lib/{MAC地址}\\加密文件.cipher
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

    /**
     * 必须文件已存在才生效
     */
    public static void setStrictPermission(File file) {
//        file.setReadable(false);
//        file.setWritable(false);
//        file.setExecutable(false);
//        file.setReadable(false, true);
//        file.setWritable(false, true);
//        file.setExecutable(false, true);
        try {
            UserPrincipalLookupService lookupService = file.toPath().getFileSystem().getUserPrincipalLookupService();
            UserPrincipal owner = lookupService.lookupPrincipalByName(SystemUtils.isWindows() ? "Administrators" : "root");
            // 构造只允许管理员删除的权限
            Set<AclEntryPermission> deleteOnlyPermissions = EnumSet.of(
                    AclEntryPermission.DELETE,
                    AclEntryPermission.DELETE_CHILD
            );
            AclEntry deleteOnlyEntry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(owner)
                    .setPermissions(deleteOnlyPermissions)
                    .build();

            // 设置文件的ACL权限
            AclFileAttributeView aclView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
            aclView.setAcl(Collections.singletonList(deleteOnlyEntry));
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
