import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/1/29
 */
public class TestPOI_Refactor {

    /**
     * 3.1 工程概况选用表
     */
    @Test
    public void test3() throws IOException {
        String json = IoUtil.readUtf8(ResourceUtil.getStream("json/3.1 工程概况选用表.json"));
        JSONObject selectionTable = new JSONObject(json);
        OutputStream outputStream = Files.newOutputStream(new File("E:\\Project\\IdeaProjects\\DemoRepository\\web-server\\poi\\src\\test\\resources\\json", "result.xlsx").toPath());
        writeSelectionTableMatrix(selectionTable, outputStream);
        System.out.println(123);
    }

    // 写入数据矩阵，收集合并信息
    public void writeSelectionTableMatrix(JSONObject selectionTable, OutputStream outputStream) throws IOException {
        // cell list
        JSONArray header = selectionTable.getByPath("osattrs.tableHeader.osattrs.IdmTableHeaderObjset[0]", JSONArray.class);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        // 创建单元格样式并设置自动换行
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        // 设置水平居中对齐
//        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 设置垂直居中对齐
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Row row = sheet.createRow(0);
        for (int i = 0; i < header.size(); i++) {
            JSONObject cellObj = header.getJSONObject(i);
            Cell cell = row.createCell(i);
            cell.setCellValue(cellObj.getStr("IdmItemText"));

            // 每一列设置最大字符数
            int width = 32; // 列宽，单位是字符数
            sheet.setColumnWidth(i, width * 256); // 乘以 256 是因为单位是 1/256 字符宽度
//            sheet.autoSizeColumn(i, true);
        }

        JSONArray rowObjs = selectionTable.getByPath("osattrs.tableRows", JSONArray.class);
        List<CellRangeAddress> mergedRegions = new ArrayList<>();
        int offset = 1;
        for (int i = 0; i < rowObjs.size(); i++) {
            JSONObject rowObj = rowObjs.getJSONObject(i);
            JSONObject rRowValue = rowObj.getByPath("oattrs.rel", JSONObject.class);
            JSONArray rowCellObjs = rowObj.getByPath("osattrs.IdmTableRowObjset", JSONArray.class);

            int currRowIndex = offset + i;
            row = ObjectUtil.defaultIfNull(sheet.getRow(currRowIndex), sheet.createRow(currRowIndex));
            for (int j = 0; j < rowCellObjs.size(); j++) {
                JSONObject rowCellObj = rowCellObjs.getJSONObject(j);
                if (j >= header.size()) {
                    continue; // 超出表头
                }
                JSONObject headerCellObj = header.getJSONObject(j);
                if (!Objects.equals(rowCellObj.getStr("IdmAttribute"), headerCellObj.getStr("Name"))) {
                    continue;
                }
                String cellText = getCellFormattedText(rowCellObj);
                Cell cell = row.createCell(j);
                cell.setCellValue(cellText);
                cell.setCellStyle(cellStyle);
                int rowSpan = Integer.parseInt(rowCellObj.getStr("IdmIsRowspan"));
                int colSpan = Integer.parseInt(rowCellObj.getStr("IdmIsColspan"));
                if (rowSpan > 1 || colSpan > 1) {
                    // 暂存合并区域
                    mergedRegions.add(new CellRangeAddress(
                            currRowIndex, currRowIndex + rowSpan - 1,
                            j, j + colSpan - 1
                    ));
                }
            }
        }

        deleteRows(sheet, mergedRegions, 4, 5, 6); // 删除一行后，由于上移，注意行号变了！

        workbook.write(outputStream);
        workbook.close();
    }

    public void deleteRows(Sheet sheet, List<CellRangeAddress> mergedRegions, int... delRowIndexList) {
        Arrays.sort(delRowIndexList);
        for (int i = delRowIndexList.length - 1; i >= 0; i--) { // 由于删除行会导致行号迁移，所以升序排序后从后往前删除
            int delRowIndex = delRowIndexList[i];
            // 统一纠正合并区域
            fixMergedRegions(sheet, delRowIndex, mergedRegions);
            // 注意删除行，一定要在重新写入合并区域之后
            deleteRowDirectly(sheet, delRowIndex);
        }
        // 最后统一设置纠正后的合并区域
        mergedRegions.forEach(sheet::addMergedRegion);
    }

    public void deleteRowDirectly(Sheet sheet, int delRowIndex) {
        // 删除
        sheet.removeRow(sheet.getRow(delRowIndex));
        // 上移
        sheet.shiftRows(delRowIndex + 1, sheet.getLastRowNum(), -1);
    }

    public void fixMergedRegions(Sheet sheet, int delRowIndex, List<CellRangeAddress> mergedRegions) {
        List<CellRangeAddress> fixedMergedRegions = new ArrayList<>();
        for (CellRangeAddress cellAddress : mergedRegions) {
            // 合并区域包含了删除行，需要处理该合并区域
            if (delRowIndex >= cellAddress.getFirstRow() && delRowIndex <= cellAddress.getLastRow()) {
                if (delRowIndex == cellAddress.getFirstRow() && delRowIndex + 1 <= sheet.getLastRowNum()) {
                    // 将合并区域左上角的值复制到下一行 TODO 不同类型单元格的值复制
                    String tempValue = sheet.getRow(delRowIndex).getCell(cellAddress.getFirstColumn()).getStringCellValue();
                    sheet.getRow(delRowIndex + 1).getCell(cellAddress.getFirstColumn()).setCellValue(tempValue);
                }
                int newLastRow = cellAddress.getLastRow() - 1; // 合并区域底边向上收缩一行
                //sheet.removeMergedRegion(i); // 先移除合并区域。下面的Add导致下标变了
                if (newLastRow > cellAddress.getFirstRow() || cellAddress.getLastColumn() > cellAddress.getFirstColumn()) { // 区域必须大于一个单元格
                    // 暂存纠正后的合并区域
                    fixedMergedRegions.add(
                            new CellRangeAddress(
                                    cellAddress.getFirstRow(), newLastRow,
                                    cellAddress.getFirstColumn(), cellAddress.getLastColumn()
                            )
                    );
                }
            } else {
                // 不需要处理
                if (cellAddress.getFirstRow() > delRowIndex) {
                    // 向上平移一个单位
                    fixedMergedRegions.add(
                            new CellRangeAddress(
                                    cellAddress.getFirstRow() - 1, cellAddress.getLastRow() - 1,
                                    cellAddress.getFirstColumn(), cellAddress.getLastColumn()
                            )
                    );
                } else {
                    fixedMergedRegions.add(cellAddress);
                }
            }
        }

        mergedRegions.clear();
        mergedRegions.addAll(fixedMergedRegions);
    }

    public String getCellFormattedText(JSONObject cellObj) {
        JSONArray childCells = cellObj.getByPath("osattrs.cellChildren", JSONArray.class);
        if (ObjectUtil.isEmpty(childCells)) {
            return null;
        }
        StringBuilder sbd = new StringBuilder();
        for (int i = 0; i < childCells.size(); i++) {
            JSONObject childCellObj = childCells.getJSONObject(i);
            sbd.append(childCellObj.getStr("IdmItemText")); // TODO 待处理占位符。处理不同类型的单元格的值
        }
        return sbd.toString();
    }


    /**
     * 合并区域显示的是最左上角的单元格的值
     */
    @Test
    public void test2() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // 设置合并单元格的值
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellValue("Merged Cell");

        // 创建合并单元格
        CellRangeAddress mergedRegion = new CellRangeAddress(0, 2, 0, 2);
        sheet.addMergedRegion(mergedRegion);

        // 保存工作簿
        workbook.write(Files.newOutputStream(Paths.get("output.xlsx")));
        workbook.close();
    }

}
