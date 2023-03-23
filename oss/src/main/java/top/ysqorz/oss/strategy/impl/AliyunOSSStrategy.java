package top.ysqorz.oss.strategy.impl;

import cn.hutool.core.io.IoUtil;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.ysqorz.oss.model.AliyunOSSProps;
import top.ysqorz.oss.model.STSCredential;
import top.ysqorz.oss.strategy.IOSSStrategy;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class AliyunOSSStrategy implements IOSSStrategy {
    @Resource
    private AliyunOSSProps aliyunOSSProps;

    /**
     * <a href="https://help.aliyun.com/document_detail/100624.html">...</a>
     * <a href="https://help.aliyun.com/document_detail/31927.html?spm=5176.8466032.bucket.dpractice-file-sign.20081450YrXG1F">...</a>
     */
    @Override
    public STSCredential generateSTSToken() {
        DefaultAcsClient client = null;
        try {
            // regionId表示RAM的地域ID。以华东1（杭州）地域为例，regionID填写为cn-hangzhou。也可以保留默认值，默认值为空字符串（""）。
            String regionId = "";
            DefaultProfile.addEndpoint(regionId, "Sts", aliyunOSSProps.getStsProps().getEndpoint()); // STS接入地址
            IClientProfile profile = DefaultProfile.getProfile(regionId, aliyunOSSProps.getAccessKeyId(), aliyunOSSProps.getAccessKeySecret()); // RAM用户密钥
            client = new DefaultAcsClient(profile);
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(aliyunOSSProps.getStsProps().getRoleArn());
            request.setRoleSessionName(aliyunOSSProps.getStsProps().getRoleSessionName());
            //request.setPolicy(policy);
            request.setDurationSeconds(aliyunOSSProps.getStsProps().getDurationSeconds());
            AssumeRoleResponse response = client.getAcsResponse(request);
            STSCredential stsCredential = new STSCredential();
            BeanUtils.copyProperties(response.getCredentials(), stsCredential);
            return stsCredential;
        } catch (ClientException ex) {
            throw new RuntimeException(ex); // 弱化为运行时异常
        } finally {
            if (Objects.nonNull(client)) {
                client.shutdown();
            }
        }
    }

    public <T> T executeByAccessKey(ClientExecutor<T> executor) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder()
                    .build(aliyunOSSProps.getBucketEndpoint(), aliyunOSSProps.getAccessKeyId(), aliyunOSSProps.getAccessKeySecret());
            return executor.execute(ossClient);
        } finally {
            if (Objects.nonNull(ossClient)) {
                ossClient.shutdown();
            }
        }
    }

    public <T> T executeBySTSToken(STSCredential stsCredential, ClientExecutor<T> executor) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder()
                    .build(aliyunOSSProps.getBucketEndpoint(), stsCredential.getAccessKeyId(),
                            stsCredential.getAccessKeySecret(), stsCredential.getSecurityToken());
            return executor.execute(ossClient);
        } finally {
            if (Objects.nonNull(ossClient)) {
                ossClient.shutdown();
            }
        }
    }

    public interface ClientExecutor<T> {
        T execute(OSS ossClient);
    }

    public ClientExecutor<PutObjectResult> buildUploadExecutor(String path, InputStream inputStream) {
        return ossClient -> {
            PutObjectRequest putObjectRequest = new PutObjectRequest(aliyunOSSProps.getBucketName(), path, inputStream);
            // ObjectMetadata metadata = new ObjectMetadata();
            // 上传文件时设置存储类型。
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // 上传文件时设置读写权限ACL。
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);
            // 上传文件。
            return ossClient.putObject(putObjectRequest);
        };
    }

    public ClientExecutor<ObjectMetadata> buildDownloadExecutor(String path, OutputStream outputStream) {
        return ossClient -> {
            //GetObjectRequest getObjectRequest = new GetObjectRequest(aliyunOSSProps.getBucketName(), path);
            //ossClient.getObject(getObjectRequest, destFile);
            OSSObject ossObject = ossClient.getObject(aliyunOSSProps.getBucketName(), path);
            IoUtil.copy(ossObject.getObjectContent(), outputStream);
            IoUtil.close(ossObject);
            return ossObject.getObjectMetadata();
        };
    }

    /**
     * 客户端：<a href="https://help.aliyun.com/document_detail/31925.html">...</a>
     */
    @Override
    public PutObjectResult simpleUploadFileBySTSToken(String path, InputStream inputStream) {
        return executeBySTSToken(generateSTSToken(), buildUploadExecutor(path, inputStream));
    }

    /**
     * 注意STS需要有对应的下载权限
     */
    @Override
    public ObjectMetadata simpleDownloadFileBySTSToken(String path, OutputStream outputStream) {
        return executeBySTSToken(generateSTSToken(), buildDownloadExecutor(path, outputStream));
    }

    @Override
    public PutObjectResult simpleUploadFileByAccessKey(String path, InputStream inputStream) {
        return executeByAccessKey(buildUploadExecutor(path, inputStream));
    }

    @Override
    public ObjectMetadata simpleDownloadFileByAccessKey(String path, OutputStream outputStream) {
        return executeByAccessKey(buildDownloadExecutor(path, outputStream));
    }

    /**
     * <a href="https://help.aliyun.com/document_detail/31862.html">...</a>
     */
    @Override
    public void moveFileByAccessKey(String srcPath, String destPath, Boolean isDeleteSrcFile) {
        executeByAccessKey((ClientExecutor<Void>) ossClient -> {
            ossClient.copyObject(aliyunOSSProps.getBucketName(), srcPath, aliyunOSSProps.getBucketName(), destPath);
            if (isDeleteSrcFile) {
                ossClient.deleteObject(aliyunOSSProps.getBucketName(), srcPath);
            }
            return null;
        });
    }

    @Override
    public void moveFileByAccessKey(String srcPath, String destPath) {
        moveFileByAccessKey(srcPath, destPath, Boolean.TRUE); // 默认移动之后删除源文件
    }

    /**
     * <a href="https://help.aliyun.com/document_detail/31862.html">...</a>
     */
    @Override
    public void deleteFileByAccessKey(String path) {
        executeByAccessKey(ossClient -> ossClient.deleteObject(aliyunOSSProps.getBucketName(), path));
    }

    /**
     * 生成分享链接，用于前端预览，例如图片预览
     */
    @Override
    public String generateSharedURL(String path, Duration duration) {
        return executeByAccessKey(ossClient -> {
            // 设置请求头。
            Map<String, String> headers = new HashMap<>();
            // 指定Object的存储类型。
            //headers.put(OSSHeaders.STORAGE_CLASS, StorageClass.Standard.toString());
            // 指定ContentType。
            //headers.put(OSSHeaders.CONTENT_TYPE, "text/txt");

            // 设置用户自定义元信息。
            Map<String, String> userMetadata = new HashMap<>();
            //userMetadata.put("key1","value1");
            //userMetadata.put("key2","value2");

            // 设置查询参数。
            // Map<String, String> queryParam = new HashMap<String, String>();
            // 指定IP地址或者IP地址段。
            // queryParam.put("x-oss-ac-source-ip","192.0.2.0");
            // 指定子网掩码中1的个数。
            // queryParam.put("x-oss-ac-subnet-mask","32");
            // 指定VPC ID。
            // queryParam.put("x-oss-ac-vpc-id","vpc-12345678");
            // 指定是否允许转发请求。
            // queryParam.put("x-oss-ac-forward-allow","true");

            // 指定生成的签名URL过期时间，单位为毫秒。
            Date expiration = new Date(new Date().getTime() + duration.toMillis());

            // 生成签名URL。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(aliyunOSSProps.getBucketName(), path, HttpMethod.GET);
            // 设置过期时间。
            request.setExpiration(expiration);
            // 将请求头加入到request中。
            request.setHeaders(headers);
            // 添加用户自定义元信息。
            request.setUserMetadata(userMetadata);
            //request.setQueryParameter(queryParam);
            // 设置单链接限速，单位为bit，例如限速100 KB/s。
            // request.setTrafficLimit(100 * 1024 * 8);

            // 通过HTTP GET请求生成签名URL。
            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        });
    }
}
