package top.ysqorz.file.license;

/**
 * 试用授权文本解析器，将来扩展Json，XML，或者Properties或者其他的数据格式
 *
 * 现在使用JSON
 */
public interface TrialLicenseTranslator {
    /**
     * @param plainText 解密之后的文本内容
     */
    TrialLicense translate(String plainText);

    String translate(TrialLicense trialLicense);
}
