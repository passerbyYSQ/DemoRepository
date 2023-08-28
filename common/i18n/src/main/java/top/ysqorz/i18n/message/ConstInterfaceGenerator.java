package top.ysqorz.i18n.message;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public interface ConstInterfaceGenerator {
    void generateConstInterfaces(File destDir, Locale... supportedLocales) throws IOException;
}
