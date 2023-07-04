package top.ysqorz.oss.strategy;

import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import top.ysqorz.oss.model.STSCredential;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;

public interface IOSSStrategy {
    /**
     * 生成临时令牌给前端用于向oss上传文件
     *
     */
    STSCredential generateSTSToken();

    /**
     * 使用STSToken，简单的上传文件
     * TODO 转换结果
     */
    PutObjectResult simpleUploadFileBySTSToken(String path, InputStream inputStream);

    /**
     * TODO 转换结果
     */
    ObjectMetadata simpleDownloadFileBySTSToken(String path, OutputStream outputStream);

    PutObjectResult simpleUploadFileByAccessKey(String path, InputStream inputStream);

    ObjectMetadata simpleDownloadFileByAccessKey(String path, OutputStream outputStream);


    void deleteFileByAccessKey(String path);

    void moveFileByAccessKey(String srcPath, String destPath, Boolean isDeleteSrcFile);

    /**
     * 也可以用于重命名
     */
    void moveFileByAccessKey(String srcPath, String destPath);

    String generateSharedURL(String path, Duration duration);
}
