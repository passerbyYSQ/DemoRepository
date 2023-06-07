package top.ysqorz.batch.springbatch.config;

import com.zaxxer.hikari.HikariDataSource;

public class MyHikariDataSource extends HikariDataSource {
    public void setUrl(String url) {
        super.setJdbcUrl(url);
    }
}
