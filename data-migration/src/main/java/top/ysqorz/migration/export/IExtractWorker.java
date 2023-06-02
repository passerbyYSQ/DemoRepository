package top.ysqorz.migration.export;

/**
 * 数据抽取的Worker
 */
public interface IExtractWorker extends AutoCloseable {
    /**
     * 异步抽取
     */
    void asyncExtract(ExportCallback callback);

    boolean isAllCompleted();
}
