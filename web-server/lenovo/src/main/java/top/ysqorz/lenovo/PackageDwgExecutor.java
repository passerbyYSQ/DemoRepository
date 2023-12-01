package top.ysqorz.lenovo;

import cn.hutool.core.compress.ZipWriter;
import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/11/24
 */
public class PackageDwgExecutor {

    public static void main(String[] args) throws IOException {
        packDwg2Zip(60001, 10000);
    }

    public static void packDwg2Zip(int start, int count) throws IOException {
        int end = start + count;
//        File inFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "dwg/历史图纸.dwg");
        File outDir = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "dwg");
        File outFile = new File(outDir, String.format("历史图纸DWG - [%s, %s].zip", start, end - 1)); // 闭区间
        OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outFile.toPath()));
        ZipWriter zipWriter = new ZipWriter(outputStream, StandardCharsets.UTF_8);
        for (int i = start; i < end; i++) {
            InputStream inputStream = new BufferedInputStream(ResourceUtil.getStream("dwg/历史图纸.dwg"));
            // 由于每次add之后都关闭了输入流，因此每次都要重新打开输入流
            zipWriter.add("历史图纸-" + i + ".dwg", inputStream);
            inputStream.close();
        }
        zipWriter.close();
        outputStream.close();
    }
}
