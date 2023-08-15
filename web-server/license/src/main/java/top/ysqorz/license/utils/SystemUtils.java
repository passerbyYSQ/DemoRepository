package top.ysqorz.license.utils;

import org.junit.Test;

import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Predicate;

public class SystemUtils {

    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }

    public static boolean isLinux() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("linux");
    }

    /**
     * 获取MAC地址，支持多网卡情况
     */
    public static String getMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(getLocalhost());
            return SecureUtils.bytes2Hex(network.getHardwareAddress());
        } catch (SocketException e) {
            throw new TrialLicenseException(e); // TODO 断网下无法获取网卡地址
        }
    }

    /**
     * 获取本机网卡IP地址
     */
    public static InetAddress getLocalhost() {
        final LinkedHashSet<InetAddress> localAddressList = getLocalAddressList(address -> {
            // 非loopback地址，指127.*.*.*的地址
            return !address.isLoopbackAddress()
                    // 需为IPV4地址
                    && address instanceof Inet4Address;
        });

        InetAddress address2 = null;
        for (InetAddress inetAddress : localAddressList) {
            if (!inetAddress.isSiteLocalAddress()) {
                // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                return inetAddress;
            } else if (null == address2) {
                // 取第一个匹配的地址
                address2 = inetAddress;
            }
        }

        if (null != address2) {
            return address2;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new TrialLicenseException(e);
        }
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param addressFilter 过滤器，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> getLocalAddressList(Predicate<InetAddress> addressFilter) {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new TrialLicenseException(e);
        }

        if (networkInterfaces == null) {
            throw new TrialLicenseException("Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress != null && (null == addressFilter || addressFilter.test(inetAddress))) {
                    ipSet.add(inetAddress);
                }
            }
        }

        return ipSet;
    }
}
