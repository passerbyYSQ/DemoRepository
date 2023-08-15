package top.ysqorz.license.utils;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/7
 */
public class TrialLicenseException extends RuntimeException {
    public TrialLicenseException(String message) {
        super(message);
    }

    public TrialLicenseException(Throwable cause) {
        super(cause);
    }
}
