package top.ysqorz.file.license;

import java.io.File;

public interface TrailLicenseManager {
    File getTrialLicenseFile();

    TrialLicense generateInitialLicense();

    boolean validateExpiration();
}
