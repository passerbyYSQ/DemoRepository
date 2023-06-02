package top.ysqorz.migration.collector;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import top.ysqorz.migration.backup.IBackupWorker;
import top.ysqorz.migration.export.ExportCallback;
import top.ysqorz.migration.export.IExportWorker;
import top.ysqorz.migration.model.PageData;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * n对n的生产者消费者模型
 */
@Slf4j
public class SimpleDataCollector implements IDataCollector, ExportCallback {
    private final Queue<PageData<?>> pageDataQueue = new ConcurrentLinkedQueue<>();
    private final IExportWorker exportWorker; // 生产者线程
    private final IBackupWorker backupWorker; // 消费者线程

    public SimpleDataCollector(IExportWorker exportWorker, IBackupWorker backupWorker) {
        this.exportWorker = exportWorker;
        this.backupWorker = backupWorker;
    }

    /**
     * 阻塞进行数据采集
     */
    public void collect() {
        exportWorker.asyncExport(this);
        while (!isQueueEmpty() || !isProduceCompleted()) {
            PageData<?> pageData = pageDataQueue.poll();
            // 如果队列为空，但是导出尚未结束时，获取队首元素为null。这说明导出线程生产数据不及时，此时主线程自旋等待
            if (ObjectUtils.isEmpty(pageData)) {
                Thread.yield();
                continue;
            }
            // 主线程将备份任务委托给备份线程备份
            log.info("队列中剩余数据包数量：{}，当前备份数据包：{}", pageDataQueue.size(), JSONUtil.toJsonStr(pageData));
            backupWorker.asyncBackup(pageData);
        }
    }

    @Override
    public boolean isQueueEmpty() {
        return pageDataQueue.isEmpty();
    }

    @Override
    public boolean isProduceCompleted() {
        return exportWorker.isAllCompleted();
    }

    /**
     * 导出线程异步将导出的数据加入到阻塞队列尾部
     */
    @Override
    public <T> void pageDataLoaded(PageData<T> pageData) {
        pageDataQueue.offer(pageData);
    }

    @Override
    public void close() throws Exception {
        exportWorker.close();
        backupWorker.close();
        pageDataQueue.clear();
    }
}
