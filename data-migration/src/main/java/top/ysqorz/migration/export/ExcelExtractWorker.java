package top.ysqorz.migration.export;

/**
 * 数据源是Excel文件
 */
public class ExcelExtractWorker implements IExtractWorker {
    @Override
    public void asyncExtract(ExportCallback callback) {

    }

    @Override
    public boolean isAllCompleted() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }
}
