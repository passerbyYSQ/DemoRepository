package top.ysqorz.batch.springbatch.batch;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import top.ysqorz.batch.springbatch.util.MybatisContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class MyBatisPlusPagingReader<T> extends AbstractPagingItemReader<T> {
    private SqlSessionFactory sqlSessionFactory;
    private Class<? extends BaseMapper<T>> mapperClass;
    private QueryWrapper<T> queryWrapper;

    private MyBatisPlusPagingReader() {
    }

    public static <T> MyBatisPlusPagingReaderBuilder<T> builder() {
        return new MyBatisPlusPagingReaderBuilder<>();
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }
        List<T> items = MybatisContext.execute(sqlSessionFactory, mapperClass, mapper -> {
            // mybatis-plus的分页从1开始
            return mapper.selectPage(Page.of(getPage() + 1, getPageSize()), queryWrapper).getRecords();
        });
        results.addAll(items);
        // TODO debug
        log.info("读取之后：{}", JSONUtil.toJsonStr(items));
    }

    @Override
    protected void doJumpToPage(int itemIndex) {

    }

    public static class MyBatisPlusPagingReaderBuilder<T> {
        private final MyBatisPlusPagingReader<T> reader = new MyBatisPlusPagingReader<>();

        public MyBatisPlusPagingReaderBuilder<T> sqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
            reader.sqlSessionFactory = sqlSessionFactory;
            return this;
        }

        public MyBatisPlusPagingReaderBuilder<T> mapperClass(Class<? extends BaseMapper<T>> mapperClass) {
            reader.mapperClass = mapperClass;
            return this;
        }

        public MyBatisPlusPagingReaderBuilder<T> queryWrapper(QueryWrapper<T> queryWrapper) {
            reader.queryWrapper = queryWrapper;
            return this;
        }

        public MyBatisPlusPagingReaderBuilder<T> saveState(boolean saveState) {
            reader.setSaveState(saveState);
            return this;
        }

        public MyBatisPlusPagingReaderBuilder<T> pageSize(int pageSize) {
            reader.setPageSize(pageSize);
            return this;
        }

        public MyBatisPlusPagingReader<T> build() {
            assert Objects.nonNull(reader.sqlSessionFactory) && Objects.nonNull(reader.mapperClass);
            return reader;
        }
    }
}
