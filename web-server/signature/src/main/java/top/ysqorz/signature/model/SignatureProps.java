package top.ysqorz.signature.model;

import cn.hutool.crypto.asymmetric.SignAlgorithm;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 签名的相关配置
 */
@Data
@ConditionalOnProperty(value = "secure.signature.enable", havingValue = "true")  // 根据条件注入bean
@Component
@ConfigurationProperties("core.secure.signature")
public class SignatureProps {
    private Boolean enable;
    private Map<String, KeyPairProps> keyPair;

    @Data
    public static class KeyPairProps {
        private SignAlgorithm algorithm;
        private String publicKeyPath;
        private String publicKey;
        private String privateKeyPath;
        private String privateKey;
    }
}