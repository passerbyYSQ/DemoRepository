package top.ysqorz.license.utils;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    @Test
    public void testMillis() {
        System.out.println(System.currentTimeMillis());
    }

    public static String format(Long millis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(millis));
    }
}
