package top.ysqorz.redis.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class RedisConfig {
    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String dateTimePattern;

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接池，必须存在commons-pool2的依赖
        redisTemplate.setConnectionFactory(factory);

        // String序列化策略
        //StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // Json序列化策略
        //Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        //jsonRedisSerializer.setObjectMapper(objectMapper); // 使用被我们定制过的ObjectMapper，而不是重新new一个

        // TODO 强烈不推荐value的序列化方式使用json，除了直观以外并无其他优点，对于一些实现了Serializable接口的非纯洁的POJO有可能会序列化失败，如：KeyPair
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        redisTemplate.afterPropertiesSet(); // 其他采用默认默认序列化方式(jdk)
        return redisTemplate;
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        LocalDateTimeSerializer dateTimeSerializer = new LocalDateTimeSerializer(dateTimeFormatter);
        LocalDateTimeDeserializer dateTimeDeserializer = new LocalDateTimeDeserializer(dateTimeFormatter);
        return builder.serializationInclusion(JsonInclude.Include.NON_NULL) // 不序列化空的字段
                .serializerByType(LocalDateTime.class, dateTimeSerializer)
                .deserializerByType(LocalDateTime.class, dateTimeDeserializer)
                .createXmlMapper(false)
                .build();
    }
}