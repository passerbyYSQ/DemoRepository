package top.ysqorz.migration.backup;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.ysqorz.migration.model.PageData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SimpleBackupWorker implements IBackupWorker {
    private ExecutorService backupExecutor = Executors.newFixedThreadPool(1);
    private File backupDir;

    public SimpleBackupWorker(File backupDir) {
        this.backupDir = backupDir;
    }

    @Override
    public <T> void asyncBackup(PageData<T> pageData) {
        backupExecutor.execute(new JSONRowBackupTask(backupDir, pageData));
    }

    @Override
    public File getBackupDir() {
        return backupDir;
    }

    @Override
    public void close() {
        backupExecutor.shutdown();
    }

    @Slf4j
    public static class JSONRowBackupTask implements Runnable {
        private final PageData<?> pageData;
        private final File backupFile;

        public JSONRowBackupTask(File dir, PageData<?> pageData) {
            this.pageData = pageData;
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new RuntimeException("创建目录失败：" + dir.getAbsolutePath());
                }
            }
            File backupFile = new File(dir, pageData.getTableName() + ".bkp");
            if (!backupFile.exists()) {
                try {
                    if (!backupFile.createNewFile()) {
                        throw new IOException();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("创建备份文件失败：" + backupFile.getAbsolutePath());
                }
            }
            this.backupFile = backupFile;
        }

        @Override
        public void run() {
            // TODO debug 减低消费速度，让队列出现堆积
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            List<String> jsonList = pageData.getRecords().stream()
                    .map((Function<Object, String>) JSONUtil::toJsonStr)
                    .collect(Collectors.toList());
            // 并发写入同一个文件抢的是同一把锁
            try (FileOutputStream backupOutputStream = new FileOutputStream(backupFile, true);
                 BufferedWriter backupWriter = new BufferedWriter(new OutputStreamWriter(backupOutputStream, StandardCharsets.UTF_8))) {
                synchronized (pageData.getTableName()) {
                    for (String json : jsonList) {
                        backupWriter.write(json);
                        backupWriter.newLine(); // 换行
                    }
                }
            } catch (IOException e) {
                log.error("写入备份文件出错", e);
            }
        }
    }
}
