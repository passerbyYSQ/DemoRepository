package top.ysqorz.batch.springbatch.util;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class MybatisContext {
    public static  <M, R> R execute(SqlSessionFactory sqlSessionFactory, Class<M> clazz, Function<M, R> function) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        M mapper = sqlSession.getMapper(clazz);
        R res = function.apply(mapper);
        sqlSession.close();
        return res;
    }

    public static <M> void execute(SqlSessionFactory sqlSessionFactory, Class<M> clazz, Consumer<M> consumer) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        M mapper = sqlSession.getMapper(clazz);
        consumer.accept(mapper);
        sqlSession.close();
    }
}
