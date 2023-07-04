package top.ysqorz.migration.extract;

import top.ysqorz.migration.model.PageData;

public interface ExportCallback {
    <T> void pageDataLoaded(PageData<T> pageData);
}
