package top.ysqorz.i18n.api.generator;

import top.ysqorz.i18n.api.model.ConstInterfaceMeta;

import java.util.List;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public interface ConstInterfaceGenerator {
    List<ConstInterfaceMeta> loadAllCodes(Locale... supportedLocales);

    void generateConstInterfaces();
}
