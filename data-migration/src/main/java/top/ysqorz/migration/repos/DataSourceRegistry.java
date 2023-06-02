package top.ysqorz.migration.repos;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceRegistry {
    @Bean("zwtDataSource")
    public DataSource zwtDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/default_default?allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&useUnicode=true");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("ysqJYKL2010");
        dataSource.setIdleTimeout(60000);
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setMaxLifetime(60000 * 10);
        dataSource.setConnectionTestQuery("SELECT 1");
        return dataSource;
    }
}
