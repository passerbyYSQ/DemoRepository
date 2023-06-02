package top.ysqorz.migration.convert;

import java.util.List;
import java.util.Map;

/**
 * n -> n
 */
public interface DataConverter {
    List<Class<?>> getOriginPOClass();

    List<Class<?>> getTargetPOClass();

    Map<Class<?>, List<Object>> convert(Map<Class<?>, List<Object>> originData);
}
