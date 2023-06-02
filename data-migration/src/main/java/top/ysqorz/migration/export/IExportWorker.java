package top.ysqorz.migration.export;

public interface IExportWorker extends AutoCloseable {
    void asyncExport(ExportCallback callback);

    boolean isAllCompleted();
}
