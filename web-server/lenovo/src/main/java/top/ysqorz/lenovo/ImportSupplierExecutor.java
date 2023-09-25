package top.ysqorz.lenovo;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.*;
import cn.hutool.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/19
 */
public class ImportSupplierExecutor {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtil.getStream("csv/06 供应商清单.csv"), StandardCharsets.UTF_8));
        CsvReader csvReader = CsvUtil.getReader(reader);
        CsvData csvData = csvReader.read();
        for (int i = 1; i < csvData.getRowCount(); i++) {
            CsvRow row = csvData.getRow(i);
            String id = row.get(0);
            String name = row.get(1);
            String desc = row.get(2);
            createSupplier(id, name, desc);
        }
        csvReader.close();
    }

    public static JSONObject createSupplier(String id, String name, String desc) {
        try {
            JSONObject creator = new JSONObject()
                    .set("ID", id)
                    .set("Name", name)
                    .set("Description", desc)
                    .set("OrganizationUnit", "中兴智能汽车有限公司")
                    .set("Country", "中国");
            return ZWTUtils.createObject("Supplier", creator);
        } catch (Exception ex) {
            System.out.printf("创建Supplier失败：%s%n", ex.getMessage());
            return null;
        }
    }
}
