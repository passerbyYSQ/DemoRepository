package top.ysqorz.migration.collector;

import top.ysqorz.migration.backup.IBackupWorker;
import top.ysqorz.migration.export.IExtractWorker;

public interface IDataCollector extends AutoCloseable {
    void collect();

    boolean isQueueEmpty();

    IExtractWorker getExtractWorker();

    IBackupWorker getBackupWorker();

    boolean isExtractCompleted();
}
