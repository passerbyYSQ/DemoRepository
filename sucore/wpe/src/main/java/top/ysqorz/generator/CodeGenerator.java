package top.ysqorz.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.util.ResourceUtils;
import top.ysqorz.model.SucoreClassDataModel;

import java.beans.Introspector;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class CodeGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        File classPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        config.setDirectoryForTemplateLoading(new File(classPath, "template"));
        Template javaBeanTemplate = config.getTemplate("JavaBean.ftl");
//        SucoreClassDataModel dataModel = new SucoreClassDataModel()
//                .setClassName("User")
//                .setClassComment("用户实体类")
//                .setPackageName("top.ysqorz.generator.po")
//                .addAttr(String.class, "userName", "用户名")
//                .addAttr(Long.class, "userID", "用户标识")
//                .addAttr(Integer.class, "age", "年龄");
        File srcPath = FileUtil.getParent(classPath, 2);
        List<SucoreClassDataModel> modeList = loadDataModel();

        List<String> subPathList = new ArrayList<>();
        Collections.addAll(subPathList, "src", "main", "java");
        Collections.addAll(subPathList, CodeGenerator.class.getPackage().getName().split("\\."));
        for (SucoreClassDataModel dataModel : modeList) {
            List<String> tempSubPathList = new ArrayList<>(subPathList);
            tempSubPathList.add(dataModel.getModule());
            File outputDir = new File(srcPath, String.join(File.separator, tempSubPathList));
            if (!outputDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outputDir.mkdirs();
            }
            OutputStream outputStream = Files.newOutputStream(new File(outputDir, dataModel.getClassName() + ".java").toPath());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            javaBeanTemplate.process(dataModel, writer);
            writer.close();
            outputStream.close();
        }
    }

    public static List<SucoreClassDataModel> loadDataModel() throws FileNotFoundException {
        JSONObject tree = loadClassTree();
        Queue<JSONObject> queue = new LinkedList<>();
        queue.offer(tree);
        SucoreClassDataModel parent = null;
        List<SucoreClassDataModel> modelList = new ArrayList<>();
        while (!queue.isEmpty()) {
            JSONObject current = queue.poll();
            SucoreClassDataModel dataModel = transformDataModel(current, parent);
            modelList.add(dataModel);
            parent = dataModel;

            List<JSONObject> children = current.getBeanList("children", JSONObject.class);
            if (ObjectUtil.isNotEmpty(children)) {
                for (JSONObject child : children) {
                    queue.offer(child);
                }
            }
        }
        return modelList;
    }

    public static SucoreClassDataModel transformDataModel(JSONObject current, SucoreClassDataModel parent) {
        String module = current.getStr("module");
        String packageName = CodeGenerator.class.getPackage().getName() + "." + module;
        SucoreClassDataModel dataModel = new SucoreClassDataModel()
                .setModule(module)
                .setPackageName(packageName)
                .setClassName(current.getStr("className"))
                .setClassComment(current.getStr("classDisplayLabel"))
                .setIsAbstract(current.getBool("isAbstract"))
                .setParent(parent);
        List<JSONObject> normalAttaches = current.getBeanList("normalAttaches", JSONObject.class);
        if (ObjectUtil.isNotEmpty(normalAttaches)) {
            List<SucoreClassDataModel.Attribute> attrList = normalAttaches.stream()
                    .map(attr -> {
                        String attrName = attr.getStr("attrName");
                        if ("Class".equalsIgnoreCase(attrName)) {
                            attrName = "Clazz";
                        }
                        // https://zhuanlan.zhihu.com/p/383518075
                        attrName = Introspector.decapitalize(StrUtil.toCamelCase(attrName));
                        return new SucoreClassDataModel.Attribute(attrName, attr.getStr("displayName"));
                    })
                    .collect(Collectors.toList());
            dataModel.setAttrs(attrList);
        }
        return dataModel;
    }

    public static JSONObject loadClassTree() throws FileNotFoundException {
        //File classPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        String path = String.join(File.separator, ResourceUtils.CLASSPATH_URL_PREFIX, "json", "sucore_class.json");
        String json = FileReader.create(ResourceUtils.getFile(path)).readString();
        return JSONUtil.parseObj(json).getJSONObject("data");
    }
}
