package top.ysqorz.lenovo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ASM {
    public static void main(String[] args) {
        try {
            // 读取字节码文件
            // 指定要读取的类的字节码文件的绝对路径
            List<String> filePaths = scanClassFiles(new File("E:\\Project\\ZW\\sucore\\core\\server\\core_objserver"));
            HashMap<String, List<String>> map = new HashMap<>();
            for (String filePath : filePaths) {
                ArrayList<String> list = new ArrayList<>();


                // 创建FileInputStream来读取字节码文件
                FileInputStream fileInputStream = new FileInputStream(filePath);

                // 创建ClassReader并读取类
                ClassReader classReader = new ClassReader(fileInputStream);

                // 创建自定义的ClassVisitor
                ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7) {

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                     String signature, String[] exceptions) {
                        // 创建自定义的MethodVisitor
                        MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM7) {
                            @Override
                            public void visitLdcInsn(Object cst) {
                                if (cst instanceof String) {
                                    String stringValue = (String) cst;
                                    // 判断字符串是否包含中文字符
                                    if (containsChineseCharacter(stringValue)) {
                                        System.out.println("找到中文字符串：" + stringValue);
                                        list.add(stringValue);
                                    }
                                }
                                super.visitLdcInsn(cst);
                            }
                        };
                        return methodVisitor;
                    }
                };

                // 通过ClassVisitor解析字节码文件
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
                if (list.isEmpty()) {
                    continue;
                }
                map.put(filePath, list);
            }



            String jsonStr = JSONUtil.toJsonStr(map);

            String fileName = "map_data.json"; // 文件名
            FileUtil.writeBytes(jsonStr.getBytes(Charset.defaultCharset()), fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean containsChineseCharacter(String text) {
        // 判断字符串是否包含中文字符
        return text.matches(".*[\\u4E00-\\u9FA5]+.*");
    }

    public static List<String> scanClassFiles(File folder) {
        List<String> classFiles = new ArrayList<>();

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    classFiles.addAll(scanClassFiles(file)); // 递归调用并将结果添加到列表中
                }
            }
        } else {
            String fileName = folder.getName();
            if (fileName.endsWith(".class")) {
                classFiles.add(folder.getAbsolutePath()); // 将找到的文件路径添加到列表中
            }
        }

        return classFiles;
    }
}
