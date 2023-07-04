package top.ysqorz.oss.model;

import lombok.Data;

@Data
public class STSCredential {
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String expiration;
}