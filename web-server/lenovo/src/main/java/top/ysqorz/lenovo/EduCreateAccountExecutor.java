package top.ysqorz.lenovo;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/10/25
 */
public class EduCreateAccountExecutor {

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtil.getStream("csv/EDU_CoreUser.csv"), StandardCharsets.UTF_8));
        CsvReader csvReader = CsvUtil.getReader(reader);
        CsvData csvData = csvReader.read();
        for (int i = 1; i < csvData.getRowCount(); i++) {
            CsvRow row = csvData.getRow(i);
            String userID = row.get(1);
            String userName = row.get(14);
            createAccount(userID, userName);
        }
    }


    public static JSONObject createAccount(String userID, String userName) {
        try {
//            return ZWTUtils.createAccount(userID, userName);
            String salt = RandomUtil.randomString(8); // 随机盐


            JSONObject creator = new JSONObject()
                    .set("Source", "DEFAULT")
                    .set("Account", extractAccount(userID))
                    .set("UserID", userID)
                    .set("UserName", userName)
                    .set("Salt", salt)
                    .set("Cipher", encryptLoginPassword(userID, salt))
                    .set("Status", 0);
            return ZWTUtils.createObject("LoginAccount", creator);
        } catch (Exception ex) {
            System.out.printf("创建账号失败：%s%n", ex.getMessage());
            return null;
        }
    }

    public static String extractAccount(String userID) {
        if (userID.endsWith("PM")) {
            return userID.substring(0, userID.length() - 2);
        } else if (userID.endsWith("CSR")) {
            return userID.substring(0, userID.length() - 3);
        } else {
            return userID;
        }
    }

    public static String md5Hex(String plainText, String salt, int digestCount) {
        return new MD5(salt.getBytes(), digestCount).digestHex(plainText);
    }

    public static String encryptLoginPassword(String plainPwd, String salt) {
        return md5Hex(plainPwd, salt, 32);
    }
}
