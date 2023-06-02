package top.ysqorz.expression.utils;

import org.junit.Test;
import tech.sucore.utils.ObjUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectUtils {

    @Test
    public void test() {
        List<String> list = new ArrayList<>();
        list.add("123");
        Object contains = ReflectUtils.invoke(list, "contains", "1234");
        System.out.println(contains);
    }

    /**
     * 方法缓存
     */
    private static final Map<Class<?>, Method[]> METHODS_CACHE = new ConcurrentHashMap<>();
    /**
     * 包装类型为Key，原始类型为Value，例如： Integer.class =》 int.class.
     */
    public static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new ConcurrentHashMap<>(8);
    /**
     * 原始类型为Key，包装类型为Value，例如： int.class =》 Integer.class.
     */
    public static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new ConcurrentHashMap<>(8);

    static {
        WRAPPER_PRIMITIVE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_PRIMITIVE_MAP.put(Byte.class, byte.class);
        WRAPPER_PRIMITIVE_MAP.put(Character.class, char.class);
        WRAPPER_PRIMITIVE_MAP.put(Double.class, double.class);
        WRAPPER_PRIMITIVE_MAP.put(Float.class, float.class);
        WRAPPER_PRIMITIVE_MAP.put(Integer.class, int.class);
        WRAPPER_PRIMITIVE_MAP.put(Long.class, long.class);
        WRAPPER_PRIMITIVE_MAP.put(Short.class, short.class);

        for (Map.Entry<Class<?>, Class<?>> entry : WRAPPER_PRIMITIVE_MAP.entrySet()) {
            PRIMITIVE_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    public static Method[] getMethods(Class<?> beanClass) throws SecurityException {
        assert ObjUtils.isAllNotEmpty(beanClass);
        return METHODS_CACHE.computeIfAbsent(beanClass,
                clazz -> getMethodsDirectly(beanClass, true, true));
    }

    public static Method[] getMethodsDirectly(Class<?> beanClass, boolean withSupers, boolean withMethodFromObject) throws SecurityException {
        assert ObjUtils.isAllNotEmpty(beanClass);
        if (beanClass.isInterface()) {
            // 对于接口，直接调用Class.getMethods方法获取所有方法，因为接口都是public方法
            return withSupers ? beanClass.getMethods() : beanClass.getDeclaredMethods();
        }

        final Set<Method> result = new HashSet<>();
        Class<?> searchType = beanClass;
        while (searchType != null) {
            if (!withMethodFromObject && Object.class == searchType) {
                break;
            }
            result.addAll(Arrays.asList(searchType.getDeclaredMethods()));
            result.addAll(getDefaultMethodsFromInterface(searchType));

            searchType = (withSupers && !searchType.isInterface()) ? searchType.getSuperclass() : null;
        }

        return result.toArray(new Method[0]);
    }

    private static List<Method> getDefaultMethodsFromInterface(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method m : ifc.getMethods()) {
                if (!Modifier.isAbstract(m.getModifiers())) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    /**
     * TODO 考虑父类
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        assert ObjUtils.isAllNotEmpty(obj, fieldName);
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj instanceof Class ? null : obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws SecurityException {
        assert ObjUtils.isAllNotEmpty(clazz, methodName);
        final Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!ObjUtils.isEmpty(method) &&
                    methodName.equals(method.getName()) &&
                    isAllAssignableFrom(method.getParameterTypes(), paramTypes)
                    && !method.isBridge()) {
                return method;
            }
        }
        return null;
    }

    public static boolean isAllAssignableFrom(Class<?>[] types1, Class<?>[] types2) {
        if (ObjUtils.isEmpty(types1) && ObjUtils.isEmpty(types2)) {
            return true;
        }
        if (null == types1 || null == types2) {
            // 任何一个为null不相等（之前已判断两个都为null的情况）
            return false;
        }
        if (types1.length != types2.length) {
            return false;
        }

        Class<?> type1;
        Class<?> type2;
        for (int i = 0; i < types1.length; i++) {
            type1 = types1[i];
            type2 = types2[i];
            if (isBasicType(type1) && isBasicType(type2)) {
                // 原始类型和包装类型存在不一致情况
                if (unWrap(type1) != unWrap(type2)) {
                    return false;
                }
            } else if (!type1.isAssignableFrom(type2)) {
                return false;
            }
        }
        return true;
    }

    public static Class<?> unWrap(Class<?> clazz) {
        if (null == clazz || clazz.isPrimitive()) {
            return clazz;
        }
        Class<?> result = WRAPPER_PRIMITIVE_MAP.get(clazz);
        return (null == result) ? clazz : result;
    }

    /**
     * 是否为基本类型（包括包装类和原始类）
     *
     * @param clazz 类
     * @return 是否为基本类型
     */
    public static boolean isBasicType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * 是否为包装类型
     *
     * @param clazz 类
     * @return 是否为包装类型
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, String methodName, Object... args) {
        assert ObjUtils.isAllNotEmpty(obj, methodName);
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        try {
            Method method = getMethod(clazz, methodName, getClasses(args));
            if (ObjUtils.isEmpty(method)) {
                throw new NoSuchMethodException(methodName);
            }
            return (T) method.invoke(obj instanceof Class ? null : obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?>[] getClasses(Object... objects) {
        Class<?>[] classes = new Class<?>[objects.length];
        Object obj;
        for (int i = 0; i < objects.length; i++) {
            obj = objects[i];
            if (null == obj) {
                classes[i] = Object.class;
            } else {
                classes[i] = obj.getClass();
            }
        }
        return classes;
    }
}
