package top.ysqorz.license.core.translator.impl;

import cn.hutool.json.JSONUtil;
import top.ysqorz.license.core.model.TrialLicense;
import top.ysqorz.license.core.translator.TrialLicenseTranslator;

/**
 * 此处试用hutool的json实现，迁移至sucore需要改用gson实现
 */
public class JSONLicenseTranslator implements TrialLicenseTranslator {
    @Override
    public TrialLicense translate(String plainText) {
        return JSONUtil.toBean(plainText, TrialLicense.class);
    }

    @Override
    public String translate(TrialLicense trialLicense) {
        return JSONUtil.toJsonStr(trialLicense);
    }
}
