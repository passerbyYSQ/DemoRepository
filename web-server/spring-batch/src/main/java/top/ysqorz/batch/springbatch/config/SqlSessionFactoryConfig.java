package top.ysqorz.batch.springbatch.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.util.List;

//@MapperScan(basePackages = "top.ysqorz.batch.springbatch.mapper") // 有两个SqlSessionFactory会报错
@Configuration
@Slf4j
public class SqlSessionFactoryConfig extends MybatisPlusAutoConfiguration {
    private final MybatisPlusProperties properties;

    public SqlSessionFactoryConfig(MybatisPlusProperties properties,
                                   ObjectProvider<Interceptor[]> interceptorsProvider,
                                   ObjectProvider<TypeHandler[]> typeHandlersProvider,
                                   ObjectProvider<LanguageDriver[]> languageDriversProvider,
                                   ResourceLoader resourceLoader,
                                   ObjectProvider<DatabaseIdProvider> databaseIdProvider,
                                   ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
                                   ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider,
                                   ApplicationContext applicationContext) {
        super(properties, interceptorsProvider, typeHandlersProvider, languageDriversProvider, resourceLoader, databaseIdProvider,
                configurationCustomizersProvider, mybatisPlusPropertiesCustomizerProvider, applicationContext);
        this.properties = properties;
    }

    /**
     * 分页插件
     */
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
        interceptor.setOverflow(true); // 溢出总页数后是否进行处理
        //interceptor.setMaxLimit(500L); // 单页分页条数限制(默认无限制)
        MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
        plusInterceptor.addInnerInterceptor(interceptor);
        return plusInterceptor;
    }

    @Bean("originSqlSessionFactory")
    public SqlSessionFactory originSqlSessionFactory(@Qualifier("originDataSource") DataSource dataSource) {
        return createSqlSessionFactory(dataSource);
    }

    @Bean("targetSqlSessionFactory")
    public SqlSessionFactory targetSqlSessionFactory(@Qualifier("targetDataSource") DataSource dataSource) {
        return createSqlSessionFactory(dataSource);
    }

    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource) {
        try {
            properties.setConfiguration(null);
            properties.setGlobalConfig(new GlobalConfig());
            // 注意不能写成sqlSessionFactory(dataSource)，这样调用的是代理之后方法
            SqlSessionFactory sqlSessionFactory = super.sqlSessionFactory(dataSource);
            // Mapper接口的包路径
            sqlSessionFactory.getConfiguration().addMappers("top.ysqorz.batch.springbatch.mapper");
            // 分页插件
            sqlSessionFactory.getConfiguration().addInterceptor(mybatisPlusInterceptor());
            return sqlSessionFactory;
        } catch (Exception e) {
            log.error("创建SqlSessionFactory失败", e);
            throw new RuntimeException(e);
        }
    }

    @Bean("originSqlSessionTemplate")
    public SqlSessionTemplate originSqlSessionTemplate(@Qualifier("originSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
       return super.sqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("targetSqlSessionTemplate")
    public SqlSessionTemplate targetSqlSessionTemplate(@Qualifier("targetSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return super.sqlSessionTemplate(sqlSessionFactory);
    }
}
