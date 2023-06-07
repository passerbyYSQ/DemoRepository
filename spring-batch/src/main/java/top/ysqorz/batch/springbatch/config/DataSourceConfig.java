package top.ysqorz.batch.springbatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    /**
     * 当有多个DataSource时，如果不指定主数据源，sql.init不会执行
     */
//    @Primary
//    @Bean(name = "springBatchDataSource")
//    @ConfigurationProperties(prefix="spring.datasource.spring-batch")
//    public DataSource springBatchDataSource() {
//        return DataSourceBuilder.create().type(DruidDataSource.class).build();
//    }

    @Bean(name = "originDataSource")
    @ConfigurationProperties(prefix="spring.datasource.origin-data")
    public DataSource originDataSource() {
        return DataSourceBuilder.create().type(MyHikariDataSource.class).build();
    }

    @Bean(name = "targetDataSource")
    @ConfigurationProperties(prefix="spring.datasource.target-data")
    public DataSource targetDataSource() {
        return DataSourceBuilder.create().type(MyHikariDataSource.class).build();
    }
}
