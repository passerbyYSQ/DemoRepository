package top.ysqorz.migration.export;

import top.ysqorz.migration.model.PageData;

public interface ExportCallback {
    <T> void pageDataLoaded(PageData<T> pageData);
}
