package top.ysqorz.migration.collector;

public interface IDataCollector extends AutoCloseable {
    void collect();

    boolean isQueueEmpty();

    boolean isProduceCompleted();
}
