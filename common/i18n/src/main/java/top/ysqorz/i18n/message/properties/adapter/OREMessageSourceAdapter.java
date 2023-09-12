package top.ysqorz.i18n.message.properties.adapter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tech.sucore.config.CusConfigInfo;
import tech.sucore.config.EnvironmentInfo;
import top.ysqorz.i18n.common.CommonUtils;
import top.ysqorz.i18n.message.contol.PropertyBundleControl;
import top.ysqorz.i18n.message.loader.adapter.OREResourceLoader;
import top.ysqorz.i18n.message.properties.ReloadableResourceBundleMessageSource;
import top.ysqorz.i18n.resolver.LocaleContextResolver;
import top.ysqorz.i18n.resolver.adapter.OREConfigLocaleContextResolver;

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

    public static void main(String[] args) throws IOException {
        String installation = CommonUtils.isEmpty(args) ? "default" : args[0];
        OREMessageSourceAdapter oreMessageSourceAdapter = new OREMessageSourceAdapter(installation);
        oreMessageSourceAdapter.generateConstInterfaces();
    }

    public OREMessageSourceAdapter(String installation) throws IOException {
        super(new OREResourceLoader(installation), StandardCharsets.UTF_8, Duration.ofMillis(30).toMillis(), true);
        initCacheMillisFromConfig();
        initBasename();
    }

    public void initCacheMillisFromConfig() {
        CusConfigInfo cusConfig = new CusConfigInfo(getInstallation());
        String cacheMillis = cusConfig.getUserParam("i18n.cache.resource.bundle.millis");
        if (Objects.isNull(cacheMillis)) {
            return;
        }
        this.cacheMillis = Long.parseLong(cacheMillis);
    }

    public void generateConstInterfaces() throws IOException {
        LocaleContextResolver localeContextResolver = new OREConfigLocaleContextResolver(getInstallation());
        Locale[] supportedLocales = localeContextResolver.resolveSupportedLocales().toArray(new Locale[0]);
        generateConstInterfaces("", supportedLocales);
    }

    public void initBasename() throws IOException {
        List<String> modules = new ArrayList<>();
        // 标准模块
        modules.addAll(parseStandardModule());
        // 客户化模块
        modules.addAll(parseCustomModule());
        // 一个模块一个basename
        String[] basenameList = modules.stream().map(module -> module.toUpperCase() + "_messages").toArray(String[]::new);
        addBasename(basenameList);
    }

    public List<String> parseStandardModule() throws IOException {
        String subPath = CommonUtils.joinStr(File.separator, "modules", "meta.build");
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
        OREResourceLoader resourceLoader = (OREResourceLoader) control.getResourceLoader();
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
                if (Objects.isNull(buildIndexAttr) || CommonUtils.isEmpty(moduleName)) {
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
