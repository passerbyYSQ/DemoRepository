package top.ysqorz.migration.backup;

import top.ysqorz.migration.model.PageData;

import java.io.File;

public interface IBackupWorker extends AutoCloseable {
    <T> void asyncBackup(PageData<T> pageData);

    File getBackupDir();
}
