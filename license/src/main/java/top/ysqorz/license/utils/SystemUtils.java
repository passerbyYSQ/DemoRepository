package top.ysqorz.license.utils;

import org.junit.Test;
import org.springframework.util.ObjectUtils;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SystemUtils {
    @Test
    public void testMac() {
        // 默认：50EBF6BA52CF
        System.out.println(getLocalMacAddress());
        System.out.println(getLoopbackMacAddress());
//        System.out.println(getMacAddresses());
    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }

    public static boolean isLinux() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("linux");
    }

    public String getMacAddress() {
        String mac = getLocalMacAddress();
        if (ObjectUtils.isEmpty(mac)) {
            mac = getLoopbackMacAddress();
        }
        return mac;
    }

    public static String getLocalMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            return SecureUtils.bytesToHex(network.getHardwareAddress(), false);
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLoopbackMacAddress() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                if (netInterface.isLoopback() ) {
                    byte[] macBytes = netInterface.getHardwareAddress();
                    if (!ObjectUtils.isEmpty(macBytes)) {
                        return SecureUtils.bytesToHex(macBytes, false);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 一台机器可能存在多个网卡, 故返回数组
     * 网络接口地址(NetworkInterface) ----> 接口地址(InterfaceAddress) ----> IP地址(InetAddress)
     * ----> 网络接口地址(NetworkInterface)
     */
    public static List<String> getMacAddresses() {
        List<String> macList = new ArrayList<>();
        try {
            // 获取机器上的所有网络接口, 返回结果至少包含一项(即, loopback本地环回测试)
            // getNetworkInterfaces() + getInetAddresses()可以获取到所有IP地址
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            // 使用标签, 跳出多重循环
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                // 获取对应网络接口的所有接口地址(InterfaceAddress)
                List<InterfaceAddress> interfaceAddrList = netInterface.getInterfaceAddresses();
                for (InterfaceAddress addr : interfaceAddrList) {
                    // InetAddress: Internet Protocol (IP) address: IP地址
                    // 返回网络接口地址对应的IP地址
                    InetAddress ip = addr.getAddress();
                    // 由IP地址获取网络接口(NetworkInterface)
                    // 方便方法搜索到绑定到其的具体IP地址的网络接口(NetworkInterface)
                    NetworkInterface netInterface1 = NetworkInterface.getByInetAddress(ip);
                    // 若为空, 跳过
                    if (netInterface1 == null) {
                        continue;

                    }
                    // 获取以太网等名称, 如：eth0, eth1, wlan1
                    String name = netInterface1.getName();
                    // 获取描述
                    String displayName = netInterface1.getDisplayName();
                    // 当网络接口有权限连接, 并且其具有MAC地址时, 返回二进制MAC硬件地址
                    byte[] mac = netInterface1.getHardwareAddress();
                    // 是否为虚拟网络接口
                    boolean virtual = netInterface1.isVirtual();
                    // 网络接口是否启动
                    boolean up = netInterface1.isUp();
                    if (mac == null) {
                        continue;

                    }
                    StringBuilder sb = new StringBuilder();
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                    macList.add(sb.toString());
                    System.out.println(name + "---" + displayName + "---" + sb + "---isUp" + up + "---isVirtual" + virtual);
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return macList;
    }
}
