package top.ysqorz.i18n.message.properties.adapter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tech.sucore.config.CusConfigInfo;
import tech.sucore.config.EnvironmentInfo;
import top.ysqorz.i18n.common.I18nUtils;
import top.ysqorz.i18n.common.constant.I18nConstant;
import top.ysqorz.i18n.message.contol.PropertyBundleControl;
import top.ysqorz.i18n.message.loader.adapter.OREI18nResourceLoader;
import top.ysqorz.i18n.message.properties.ReloadableResourceBundleMessageSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * ZWT的消息源适配器，针对ZWT固定参数
 *
 * @author yaoshiquan
 * @date 2023/9/12
 */
public class OREMessageSourceAdapter extends ReloadableResourceBundleMessageSource {

    // -i default  -p tech.sucore.common.constant  -d D:\temp  -lang zh-CN,en-US
    public static void main(String[] args) throws Exception {
        I18nUtils.generateConstInterfacesByCMD(args1 -> {
            String installation = I18nUtils.getCmdArgs("-i", "default", args1);
            return new OREMessageSourceAdapter(installation);
        }, args);
    }

    private Locale defaultLocale = Locale.getDefault();

    public OREMessageSourceAdapter() throws IOException {
        this("default");
    }

    public OREMessageSourceAdapter(String installation) throws IOException {
        super(new OREI18nResourceLoader(installation), StandardCharsets.UTF_8, Duration.ofMinutes(30).toMillis(), true);
        initCacheMillisFromConfig();
        initBasename();
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public String getMessage(String code, String... args) {
        return getMessage(code, getDefaultLocale(), args);
    }

    public String getMessage(String code, String defaultMessage, String... args) {
        return getMessage(code, defaultMessage, getDefaultLocale(), args);
    }

    public void initCacheMillisFromConfig() {
        CusConfigInfo cusConfig = new CusConfigInfo(getInstallation(), true);
        String cacheMillis = cusConfig.getUserParam(I18nConstant.PROPS_RESOURCE_BUNDLE_CACHE_MILLIS);
        if (Objects.nonNull(cacheMillis)) {
            this.cacheMillis = Long.parseLong(cacheMillis);
        }
        String langTag = cusConfig.getUserParam(I18nConstant.PROP_LOCALE_CONTEXT);
        if (Objects.nonNull(langTag)) {
            this.defaultLocale = Locale.forLanguageTag(langTag);
        }
    }

    public void initBasename() throws IOException {
        List<String> modules = new ArrayList<>();
        // 标准模块
        modules.addAll(parseStandardModule());
        // 客户化模块
        modules.addAll(parseCustomModule());
        Collections.reverse(modules);
        // 一个模块一个basename
        String[] basenameList = modules.stream().map(module -> module.toUpperCase() + "_messages").toArray(String[]::new);
        addBasename(basenameList);
    }

    public List<String> parseStandardModule() throws IOException {
        String subPath = I18nUtils.joinStr(File.separator, "modules", "meta.build");
        File metaBuildFile = new File(EnvironmentInfo.getStandardMetaPath(), subPath);
        return parseMetaBuildFile(metaBuildFile);
    }

    public List<String> parseCustomModule() throws IOException {
        String instMetaDir = EnvironmentInfo.getInstallationMetaPath(getInstallation());
        File metaBuildFile = new File(instMetaDir, "meta.build");
        return parseMetaBuildFile(metaBuildFile);
    }

    public String getInstallation() {
        PropertyBundleControl control = (PropertyBundleControl) this.control;
        OREI18nResourceLoader resourceLoader = (OREI18nResourceLoader) control.getResourceLoader();
        return resourceLoader.getInstallation();
    }

    public List<String> parseMetaBuildFile(File metaBuildFile) throws IOException {
        Map<Integer, String> moduleMap = new TreeMap<>(Integer::compare);
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(metaBuildFile);
            NodeList moduleNodes = document.getElementsByTagName("module");
            for (int i = 0; i < moduleNodes.getLength(); i++) {
                Node moduleNode = moduleNodes.item(i);
                Node buildIndexAttr = moduleNode.getAttributes().getNamedItem("build_index");
                String moduleName = moduleNode.getTextContent().trim();
                if (Objects.isNull(buildIndexAttr) || I18nUtils.isEmpty(moduleName)) {
                    continue;
                }
                Integer buildIndex = Integer.valueOf(buildIndexAttr.getNodeValue().trim());
                moduleMap.put(buildIndex, moduleName);
            }
            return new ArrayList<>(moduleMap.values());
        } catch (Exception ex) {
            throw new IOException("meta.build file parsed failed: " + metaBuildFile.getAbsolutePath(), ex);
        }
    }
}
