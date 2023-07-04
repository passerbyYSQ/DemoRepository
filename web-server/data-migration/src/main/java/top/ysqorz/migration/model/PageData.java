package top.ysqorz.migration.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageData<T> {
    private String tableName;
    private Long currPage;
    private Long pageSize;
    private Long pageCount;
    private Long total;
    private List<T> records;

    public PageData(String tableName, IPage<T> pageData) {
        this.tableName = tableName;
        this.currPage = pageData.getCurrent();
        this.pageSize = pageData.getSize();
        this.total = pageData.getTotal();
        this.pageCount = pageData.getPages();
        this.records = pageData.getRecords();
    }
}
