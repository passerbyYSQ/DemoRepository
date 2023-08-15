package top.ysqorz.license.api;

import java.io.File;

public interface TrailLicenseManager {
    File getTrialLicenseFile();

    void startDaemon(TrialLicenseCallback callback);

    boolean validateExpiration();
}
