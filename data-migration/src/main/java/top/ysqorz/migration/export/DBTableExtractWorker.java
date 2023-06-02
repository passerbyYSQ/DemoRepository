package top.ysqorz.migration.export;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.TypeUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.util.ObjectUtils;
import top.ysqorz.migration.model.PageData;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 数据源是数据库
 * <a href="https://blog.csdn.net/qq_42413011/article/details/118640420">...</a>
 */
@Slf4j
public class DBTableExtractWorker implements IExtractWorker {
    private ExecutorService exportExecutor = Executors.newFixedThreadPool(16);
    private List<String> tableNames = new ArrayList<>(); // 要导出的表名，与@TableName注解的一致。有顺序
    private SqlSessionFactory sqlSessionFactory;
    private int pageSize = 500;
    private CountDownLatch countDownLatch;

    /**
     * 私有化构造方法，使用建造者模式创建实例
     */
    private DBTableExtractWorker() {
    }

    public static ExportHandlerBuilder builder() {
        return new ExportHandlerBuilder();
    }

    /**
     * 开始执行导出
     */
    @Override
    public void asyncExtract(ExportCallback callback) {
        // 遍历所有的表
        SqlSession defaultSqlSession = sqlSessionFactory.openSession();
        List<Runnable> taskList = new ArrayList<>();
        for (String tableName : tableNames) {
            Class<BaseMapper<?>> mapperClass = getMapperClassByTableName(tableName);
            if (ObjectUtils.isEmpty(mapperClass)) {
                log.error("数据库表{}不存在对应的Mapper类", tableName);
                continue;
            }
            BaseMapper<?> mapper = defaultSqlSession.getMapper(mapperClass);
            Long total = mapper.selectCount(null); // 单表的总记录数
            int pageCount = (int) Math.ceil(total / (double) pageSize); // 总页数
            // 每一页的查询都作为一个任务，拆分任务
            for (int i = 1; i <= pageCount; i++) {
                int currPage = i;
                taskList.add(new ExecuteSQLTask(sqlSessionFactory, sqlSession -> {
                    BaseMapper<?> mapper1 = sqlSession.getMapper(mapperClass);
                    Page<?> pageData = mapper1.selectPage(Page.of(currPage, pageSize), null);
                    callback.pageDataLoaded(new PageData<>(tableName, pageData));
                    countDownLatch.countDown(); // 任务完成，计数器减一
                }));
            }
        }
        defaultSqlSession.close();

        // 提交给线程池异步执行
        countDownLatch = new CountDownLatch(taskList.size()); // 初始化计数器一定要在启动线程之前
        for (Runnable task : taskList) {
            exportExecutor.execute(task);
        }
    }

    /**
     * 是否所有的异步任务都已经完成
     */
    public boolean isAllCompleted() {
        return countDownLatch.getCount() == 0;
    }

    @Override
    public void close() {
        exportExecutor.shutdown();
    }

    @AllArgsConstructor
    public static class ExecuteSQLTask implements Runnable {
        private SqlSessionFactory sqlSessionFactory;
        private Consumer<SqlSession> consumer;

        @Override
        public void run() {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                consumer.accept(sqlSession);
            }
        }
    }

    /**
     * 获取Mapper对应的表名
     */
    public static String getTableNameByMapper(Class<?> mapperClass) {
        Type[] typeArgs = TypeUtil.getTypeArguments(mapperClass);
        Class<?> poClazz = (Class<?>) typeArgs[0];
        TableName tableName = AnnotationUtil.getAnnotation(poClazz, TableName.class); // 必须使用注解声明表名
        if (Objects.nonNull(tableName)) {
            return tableName.value();
        }
        return poClazz.getSimpleName(); // TODO 简单处理
    }

    /**
     * 根据表名获取对应Mapper的Class对象
     */
    @SuppressWarnings("unchecked")
    public Class<BaseMapper<?>> getMapperClassByTableName(String tableName) {
        MapperRegistry mapperRegistry = sqlSessionFactory.getConfiguration().getMapperRegistry();
        return (Class<BaseMapper<?>>) mapperRegistry.getMappers().stream()
                .filter(clazz -> tableName.equals(getTableNameByMapper(clazz)))
                .findFirst().orElse(null);
    }

    public static class ExportHandlerBuilder {
        private final DBTableExtractWorker exportHandler = new DBTableExtractWorker();
        private final MybatisConfiguration mybatisConfig = new MybatisConfiguration(); // plus的增强配置

        /**
         * 数据库连接池
         */
        public ExportHandlerBuilder setDataSource(DataSource dataSource) { // 通过配置类方式往IOC容器中注入多个DataSource，从中选取一个
            Environment environment = new Environment("1", new JdbcTransactionFactory(), dataSource);
            mybatisConfig.setEnvironment(environment);
            return this;
        }

        /**
         * Mapper类所在的包
         */
        public ExportHandlerBuilder setMapperPackage(String pkgPath) {
            mybatisConfig.addMappers(pkgPath);
            return this;
        }

        /**
         * 添加要导出的表
         */
        public ExportHandlerBuilder addExportTable(String... tableNames) {
            exportHandler.tableNames.addAll(Arrays.asList(tableNames));
            return this;
        }

        /**
         * 设置导出的线程池
         */
        public ExportHandlerBuilder setExportExecutor(ExecutorService executor) {
            exportHandler.exportExecutor = executor;
            return this;
        }

        /**
         * 初始化分页插件
         */
        public ExportHandlerBuilder initPagination(int pageSize) {
            PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
            interceptor.setOverflow(true); // 溢出总页数后是否进行处理
            //interceptor.setMaxLimit(500L); // 单页分页条数限制(默认无限制)
            MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
            plusInterceptor.addInnerInterceptor(interceptor);
            mybatisConfig.addInterceptor(plusInterceptor);
            exportHandler.pageSize = pageSize;
            return this;
        }

        /**
         * 补充默认配置
         */
        public void initOtherProps() {
            // 开启驼峰大小写转换
            mybatisConfig.setMapUnderscoreToCamelCase(true);
            // 配置添加数据自动返回数据主键
            mybatisConfig.setUseGeneratedKeys(true);
            // 配置日志实现
            mybatisConfig.setLogImpl(StdOutImpl.class); // 将SQL打印到标准输出，方便调试
            // 构建mybatis-plus需要的GlobalConfig
            GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(mybatisConfig);
            // 此参数会自动生成实现baseMapper的基础方法映射
            globalConfig.setSqlInjector(new DefaultSqlInjector());
            // 设置id生成器
            globalConfig.setIdentifierGenerator(new DefaultIdentifierGenerator());
            // 设置超类mapper
            globalConfig.setSuperMapperClass(BaseMapper.class);
        }

        public DBTableExtractWorker build() {
            assert !ObjectUtils.isEmpty(exportHandler.tableNames) : "没有要导出的表";
            initOtherProps();
            exportHandler.sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig);
            return exportHandler;
        }
    }
}
