package top.ysqorz.wpe.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.util.ResourceUtils;
import top.ysqorz.wpe.model.SucoreClassDataModel;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * sucore的持久化类代码生成器
 * TODO 抽象类不需要加上@TableName注解
 */
public class CodeGenerator {
    private static final boolean withExtend = true;

    public static void main(String[] args) throws IOException, TemplateException {
        File classPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        config.setDirectoryForTemplateLoading(new File(classPath, "template"));
        Template javaBeanTemplate = config.getTemplate("sucore_class.ftl");
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
        List<SucoreClassDataModel> modelList = new ArrayList<>();
        while (!queue.isEmpty()) {
            JSONObject current = queue.poll();
//            if (current.getBool("isDynamic", Boolean.FALSE)) { // 过滤动态类
//                continue;
//            }
            SucoreClassDataModel dataModel = transformDataModel(current);
            modelList.add(dataModel);

            List<JSONObject> children = current.getBeanList("children", JSONObject.class);
            if (ObjectUtil.isNotEmpty(children)) {
                for (JSONObject child : children) {
                    if (withExtend) {
                        child.set("ParentDataModel", dataModel);
                    } else {
                        List<JSONObject> fullAttaches = current.getBeanList("normalAttaches", JSONObject.class); // 新的List引用
                        if (fullAttaches == null) {
                            fullAttaches = new ArrayList<>();
                        }
                        List<JSONObject> normalAttaches = child.getBeanList("normalAttaches", JSONObject.class);
                        if (normalAttaches == null) {
                            normalAttaches = new ArrayList<>();
                        }
                        fullAttaches.addAll(normalAttaches);
                        child.set("normalAttaches", fullAttaches);
                    }
                    queue.offer(child);
                }
            }
        }
        return modelList;
    }

    public static SucoreClassDataModel transformDataModel(JSONObject current) {
        String module = current.getStr("module");
        String packageName = CodeGenerator.class.getPackage().getName() + "." + module;
        SucoreClassDataModel dataModel = new SucoreClassDataModel()
                .setModule(module)
                .setPackageName(packageName)
                .setClassName(current.getStr("className"))
                .setClassComment(current.getStr("classDisplayLabel"))
                .setIsAbstract(current.getBool("isAbstract", Boolean.FALSE))
                .setIsDynamic(current.getBool("isDynamic", Boolean.FALSE))
                .setParent(current.get("ParentDataModel", SucoreClassDataModel.class));
        List<JSONObject> normalAttaches = current.getBeanList("normalAttaches", JSONObject.class);
        List<JSONObject> constants = current.getBeanList("constants", JSONObject.class);
        if (ObjectUtil.isNotEmpty(normalAttaches)) {
            List<SucoreClassDataModel.Attribute> attrList = normalAttaches.stream()
//                    .filter(attr -> {
//                        return "PERSISTENT".equalsIgnoreCase(attr.getStr("storageType")); // 过滤动态属性，只保留持久属性
//                    })
                    .map(attr -> {
                        StringBuilder comment = new StringBuilder(attr.getStr("displayName"));
                        String attrName = attr.getStr("attrName");
                        // 补充关系左右侧UUID的注释信息
                        if (attr.getBool("isRelation", Boolean.TRUE) && ObjectUtil.isNotEmpty(constants)) {
                            String constValue = null;
                            if ("UUID_L".equals(attrName)) {
                                constValue = getConstantValue(constants, "ClassLeft");
                            } else if ("UUID_R".equals(attrName)) {
                                constValue = getConstantValue(constants, "ClassRight");
                            }
                            if (ObjectUtil.isNotEmpty(constValue)) {
                                comment.append("，").append(constValue).append("对象的UUID");
                            }
                        }
                        return new SucoreClassDataModel.Attribute(attrName, comment.toString());
                    })
                    .collect(Collectors.toList());
            dataModel.setAttrs(attrList);
        }
        return dataModel;
    }

    public static String getConstantValue(List<JSONObject> constants, String name) {
        if (ObjectUtil.isEmpty(constants)) {
            return null;
        }
        return constants.stream().filter(object -> object.getStr("constName").equals(name))
                .map(object -> object.getStr("constValue"))
                .findFirst().orElse(null);
    }

    public static JSONObject loadClassTree() throws FileNotFoundException {
        //File classPath = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
        String path = String.join(File.separator, ResourceUtils.CLASSPATH_URL_PREFIX, "json", "sucore_class.json");
        String json = FileUtil.readUtf8String(ResourceUtils.getFile(path));
        return JSONUtil.parseObj(json);
    }
}
