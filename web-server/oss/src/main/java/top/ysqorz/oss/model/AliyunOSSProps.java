package top.ysqorz.oss.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@ConfigurationProperties("oss.aliyun")
@Component
@Data
public class AliyunOSSProps {
    /**
     * RAM用户的访问密钥，使用STS时需要确保该用户具有分配角色的权限
     */
    private String accessKeyId;
    private String accessKeySecret;

    private String bucketName;
    private String bucketEndpoint;
    @Resource
    private STSProps stsProps;

    @ConfigurationProperties("oss.aliyun.sts")
    @Component
    @Data
    public static class STSProps {
        /**
         * STS接入地址，例如sts.cn-hangzhou.aliyuncs.com，<a href="https://help.aliyun.com/document_detail/371859.html#reference-sdg-3pv-xdb">...</a>
         */
        private String endpoint;

        /**
         * regionId表示RAM的地域ID。以华东1（杭州）地域为例，regionID填写为cn-hangzhou。也可以保留默认值，默认值为空字符串（""）。
         */
        private String regionId = "";

        /**
         * 临时扮演的角色
         */
        private String roleArn;

        /**
         * 自定义角色会话名称，用来区分不同的令牌
         */
        private String roleSessionName;

        /**
         * 设置临时访问凭证的有效时间，单位为秒
         */
        private Long durationSeconds;

        /**
         * 以下Policy用于限制仅允许使用临时访问凭证向目标存储空间examplebucket上传文件。
         * 临时访问凭证最后获得的权限是步骤4设置的角色权限和该Policy设置权限的交集，即仅允许将文件上传至目标存储空间examplebucket下的exampledir目录。
         * 如果policy为空，则用户将获得该角色下所有权限。
         */
        private String proxyJSON;
    }
}
