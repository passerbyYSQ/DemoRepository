package top.ysqorz.i18n.message;

import java.io.IOException;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public interface ConstInterfaceGenerator {
    void generateConstInterfaces(String subPackagePath, Locale... supportedLocales) throws IOException;
}
