package top.ysqorz.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;
import top.ysqorz.migration.backup.SimpleBackupWorker;
import top.ysqorz.migration.collector.SimpleDataCollector;
import top.ysqorz.migration.extract.DBTableExtractWorker;
import top.ysqorz.migration.repos.zwt.mapper.ICoreUserMapper;

import javax.sql.DataSource;
import java.io.File;

@SpringBootApplication
@Slf4j
public class DataMigrationApplication {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(DataMigrationApplication.class, args);
        DataSource zwtDataSource = (DataSource) context.getBean("zwtDataSource");
//        Thread.sleep(10000);
        DBTableExtractWorker extractWorker = DBTableExtractWorker.builder()
                .setDataSource(zwtDataSource) // 连接的数据源
                .setMapperPackage(ICoreUserMapper.class.getPackage().getName())
                .initPagination(2)
                .addExportTable("CoreUser")
                .build();
        File classPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        // TODO 重复执行时，数据出现重复
        File backupDir = new File(classPath, "backup");
        SimpleBackupWorker backupWorker = new SimpleBackupWorker(backupDir);
        SimpleDataCollector dataCollector = new SimpleDataCollector(extractWorker, backupWorker);

        log.info("数据采集开始");
        long start = System.currentTimeMillis();
        // 阻塞进行数据采集并备份
        dataCollector.collect();
        log.info("数据采集完成，耗时：{} ms，备份路径：{}", (System.currentTimeMillis() - start), backupDir.getAbsolutePath());
//        dataCollector.close(); // TODO 有问题
    }
}
