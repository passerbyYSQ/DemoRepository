package top.ysqorz.file.license;

public interface LicenseCallback {
    /**
     * 授权过期的回调，必须由调用方实现
     * @param license 将授权文本的信息给调用方，以便调用方可以根据信息实现更精确的控制，比如：只停止某些模块
     */
    void licenseExpired(TrialLicense license);
}
