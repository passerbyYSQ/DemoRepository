package top.ysqorz.license.core;

import java.io.File;

public interface TrailLicenseManager {
    File getTrialLicenseFile();

    boolean validateExpiration();
}
