package top.ysqorz.signature.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.ysqorz.signature.model.SignatureProps;

import java.nio.charset.StandardCharsets;

@ConditionalOnBean(SignatureProps.class)
@Component
public class SignatureManager {
    private final SignatureProps signatureProps;

    public SignatureManager(SignatureProps signatureProps) {
        this.signatureProps = signatureProps;
        loadKeyPairByPath();
    }

    /**
     * 验签。验证不通过可能抛出运行时异常CryptoException
     *
     * @param callerID  调用方的唯一标识
     * @param rawData   原数据
     * @param signature 待验证的签名(十六进制字符串)
     * @return 验证是否通过
     */
    public boolean verifySignature(String callerID, String rawData, String signature) {
        Sign sign = getSignByCallerID(callerID);
        if (ObjectUtils.isEmpty(sign)) {
            return false;
        }

        // 使用公钥验签
        return sign.verify(rawData.getBytes(StandardCharsets.UTF_8), HexUtil.decodeHex(signature));
    }

    /**
     * 生成签名
     *
     * @param callerID 调用方的唯一标识
     * @param rawData  原数据
     * @return 签名(十六进制字符串)
     */
    public String sign(String callerID, String rawData) {
        Sign sign = getSignByCallerID(callerID);
        if (ObjectUtils.isEmpty(sign)) {
            return null;
        }
        return sign.signHex(rawData);
    }

    public SignatureProps getSignatureProps() {
        return signatureProps;
    }

    public SignatureProps.KeyPairProps getKeyPairPropsByCallerID(String callerID) {
        return signatureProps.getKeyPair().get(callerID);
    }

    private Sign getSignByCallerID(String callerID) {
        SignatureProps.KeyPairProps keyPairProps = signatureProps.getKeyPair().get(callerID);
        if (ObjectUtils.isEmpty(keyPairProps)) {
            return null; // 无效的、不受信任的调用方
        }
        return SecureUtil.sign(keyPairProps.getAlgorithm(), keyPairProps.getPrivateKey(), keyPairProps.getPublicKey());
    }

    /**
     * 加载非对称密钥对
     */
    private void loadKeyPairByPath() {
        // 支持类路径配置，形如：classpath:secure/public.txt
        // 公钥和私钥都是base64编码后的字符串
        signatureProps.getKeyPair()
                .forEach((key, keyPairProps) -> {
                    // 如果配置了XxxKeyPath，则优先XxxKeyPath
                    keyPairProps.setPublicKey(loadKeyByPath(keyPairProps.getPublicKeyPath()));
                    keyPairProps.setPrivateKey(loadKeyByPath(keyPairProps.getPrivateKeyPath()));
                    if (ObjectUtils.isEmpty(keyPairProps.getPublicKey()) ||
                            ObjectUtils.isEmpty(keyPairProps.getPrivateKey())) {
                        throw new RuntimeException("No public and private key files configured");
                    }
                });
    }

    private String loadKeyByPath(String path) {
        if (ObjectUtils.isEmpty(path)) {
            return null;
        }
        return IoUtil.readUtf8(ResourceUtil.getStream(path));
    }
}