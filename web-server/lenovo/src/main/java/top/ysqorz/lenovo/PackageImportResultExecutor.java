package top.ysqorz.lenovo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/11/30
 */
@Slf4j
public class PackageImportResultExecutor {
    public static List<String> SQL_FILE_NAME_LIST = Arrays.asList(
            "aticnvrtreq.sql",
            "caddrawing.sql",
            "designmaster.sql",
            "designrev.sql",
            "raticnvrtbi.sql",
            "raticnvrtsd.sql",
            "raticnvrttd.sql",
            "rdsgncntnts.sql",
            "rvaultfolder.sql",
            "thngenreq.sql",
            "zipfile.sql"
    );

    public static void main(String[] args) throws IOException {
        File dir = new File("E:\\工作\\客户-项目\\山西电机\\导入数据包");
        packageImportResult("E:\\工作\\客户-项目\\山西电机\\导入数据包\\汇总数据1-69956", dir.listFiles());
    }

    public static void packageImportResult(String outDirPath, File... dirs) throws IOException {
        File outDir = new File(outDirPath);
        if (outDir.isFile()) {
            throw new IOException("输出目录不能是文件：" + outDir.getAbsolutePath());
        }
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                throw new IOException("创建目录失败：" + outDir.getAbsolutePath());
            }
        }
        // 过滤出合法的源目录
        List<File> sourceDirs = Arrays.stream(dirs).filter(PackageImportResultExecutor::isDirValid).collect(Collectors.toList());
        // 创建汇总后的目标SQL文件
        File targetSqlFile = new File(outDir, "import_design_total.sql");
        if (targetSqlFile.exists()) {
            log.warn("文件已存在：{}", targetSqlFile.getAbsolutePath());
            String newFileName = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + "-" + targetSqlFile.getName();
            targetSqlFile = new File(outDir, newFileName);
        }
        if (!targetSqlFile.createNewFile()) {
            log.warn("文件创建失败：{}", targetSqlFile.getAbsolutePath());
        }
        // 汇总SQL文件
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetSqlFile, true)))) {
            printWriter.println("BEGIN TRANSACTION;"); // 写入开启事务
            for (String sqlFileName : SQL_FILE_NAME_LIST) { // 先按照类型归并
                for (File sourceDir : sourceDirs) { // 遍历所有源目录
                    append2TargetSqlFile(new File(sourceDir, sqlFileName), printWriter);
                }
            }
            printWriter.println("UPDATE `rvaultfolder`" + System.lineSeparator() +
                    "SET `UUID_L` = (" + System.lineSeparator() +
                    "    SELECT `UUID`" + System.lineSeparator() +
                    "    FROM `vault`" + System.lineSeparator() +
                    "    WHERE `Name` = 'SXDJVault'" + System.lineSeparator() +
                    "    LIMIT 1" + System.lineSeparator() +
                    ")" + System.lineSeparator() +
                    "WHERE `UUID_L` = '666cf442-7c44-11ee-85cb-04421ae5fd71';");
            printWriter.println("COMMIT;");
        }
        // 初始化空的压缩包
        try (ZipOutputStream outZip = new ZipOutputStream(Files.newOutputStream(new File(outDir, "SXDJVault.zip").toPath()), StandardCharsets.UTF_8)) {
            for (File sourceDir : sourceDirs) {
                File sourceZipFile = findSourceZipFile(sourceDir);
                if (ObjectUtils.isEmpty(sourceZipFile)) {
                    log.warn("源目录缺少zip压缩文件：{}", sourceDir);
                    continue;
                }
                append2TargetZipFile(sourceZipFile, outZip);
            }
        }
    }

    public static void append2TargetZipFile(File sourceZipFile, ZipOutputStream outZip) throws IOException {
        try (ZipInputStream inZip = new ZipInputStream(Files.newInputStream(sourceZipFile.toPath()), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while (Objects.nonNull(entry = inZip.getNextEntry())) {
                outZip.putNextEntry(new ZipEntry(entry.getName()));
                IoUtil.copy(inZip, outZip);
                outZip.closeEntry();
            }
            log.info("文件已拷贝完：{}", sourceZipFile.getAbsolutePath());
            outZip.flush();
        }
    }

    public static void append2TargetSqlFile(File sourceSqlFile, PrintWriter printWriter) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(sourceSqlFile))) {
            String line;
            while (!ObjectUtils.isEmpty(line = fileReader.readLine())) { // 读取的一行不是空
                if (!line.endsWith(";")) {
                    line += ";";
                }
                printWriter.println(line);
            }
            log.info("文件已拷贝完：{}", sourceSqlFile.getAbsolutePath());
            printWriter.flush();
        }
    }

    private static File findSourceZipFile(File dir) {
        File[] files = dir.listFiles();
        if (ObjectUtils.isEmpty(files)) {
            return null;
        }
        for (File file : files) {
            if (file.getName().contains("SXDJVault") && file.getName().endsWith(".zip")) {
                return file;
            }
        }
        return null;
    }

    public static boolean isDirValid(File dir) {
        if (!dir.exists() || dir.isFile()) {
            log.warn("目录不存在：{}", dir.getAbsolutePath());
            return false;
        }
        // 包含11个sql文件
        File[] files = dir.listFiles();
        if (ObjectUtils.isEmpty(files)) {
            return false;
        }
        Set<String> fileNameSet = Arrays.stream(files).map(File::getName).collect(Collectors.toSet());
        for (String sqlFileName : SQL_FILE_NAME_LIST) {
            if (!fileNameSet.contains(sqlFileName)) {
                log.warn("目录：{}，缺少SQL文件：{}", dir.getAbsolutePath(), sqlFileName);
                return false;
            }
        }
        for (String fileName : fileNameSet) {
            if (fileName.contains("SXDJVault") && fileName.endsWith(".zip")) {
                return true;
            }
        }
        log.warn("目录：{}，缺少压缩文件：SXDJVault{}.zip", dir.getAbsolutePath(), "{占位符}");
        return false;
    }
}
