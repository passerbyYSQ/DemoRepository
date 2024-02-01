import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/1/29
 */
public class TestPOI {

    /**
     * 3.1 工程概况选用表
     * <p>
     * 设计说明书：ExpandCellChildValue
     * 计算说明书：ExpandCellChildValue
     */
    @Test
    public void test3() throws IOException {
        String json = IoUtil.readUtf8(ResourceUtil.getStream("json/table.json"));
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
        String sheetName = selectionTable.getStr("ObjectDisplayName", "").replaceAll("[\\\\/*?\\[\\]:]", " ");
        Sheet sheet = ObjectUtil.isEmpty(sheetName) ? workbook.createSheet() : workbook.createSheet(sheetName);

        CellStyle headerCellStyle = createHeaderCellStyle(workbook);
        CellStyle bodyCellStyle = createBodyCellStyle(workbook);
        List<CellRangeAddress> mergedRegions = new ArrayList<>();

        Row row = sheet.createRow(0);
        for (int i = 0; i < header.size(); i++) {
            JSONObject cellObj = header.getJSONObject(i);
            Cell cell = row.createCell(i);
            cell.setCellValue(cellObj.getStr("IdmItemText"));
            cell.setCellStyle(headerCellStyle);

            int rowSpan = Convert.toInt(cellObj.getStr("IdmIsRowspan"), 1);
            int colSpan = Convert.toInt(cellObj.getStr("IdmIsColspan"), 1);
            if (rowSpan > 1 || colSpan > 1) {
                mergedRegions.add(new CellRangeAddress( // 暂存合并区域
                        0, rowSpan - 1,
                        i, i + colSpan - 1
                ));
            }
            String colWidth = cellObj.getStr("IdmWidth");
//            if (ObjectUtil.isNotEmpty(colWidth)) {
//                sheet.setColumnWidth(i, Convert.toInt(colWidth) * 256);
//            }
        }

        JSONArray rowObjs = selectionTable.getByPath("osattrs.tableRows", JSONArray.class);
        List<Integer> delRowIndexList = new ArrayList<>();
        int offset = 1;
        for (int i = 0; i < rowObjs.size(); i++) {
            JSONObject rowObj = rowObjs.getJSONObject(i);
            JSONObject rRowValue = rowObj.getByPath("oattrs.rel", JSONObject.class);
            JSONArray rowCellObjs = rowObj.getByPath("osattrs.IdmTableRowObjset", JSONArray.class);

            int currRowIndex = offset + i;
            if (Objects.isNull(rRowValue) || !Objects.equals(rRowValue.getStr("IdmImplementation"), "+")) {
                delRowIndexList.add(currRowIndex);
            }
            row = ObjectUtil.defaultIfNull(sheet.getRow(currRowIndex), sheet.createRow(currRowIndex));
            for (int j = 0; j < rowCellObjs.size(); j++) {
                JSONObject rowCellObj = rowCellObjs.getJSONObject(j);
//                if (j >= header.size()) {
//                    continue; // 超出表头
//                }
//                JSONObject headerCellObj = header.getJSONObject(j);
//                if (!Objects.equals(rowCellObj.getStr("IdmAttribute"), headerCellObj.getStr("Name"))) {
//                    continue;
//                }
                String cellText = getCellFormattedText(rowCellObj);
                Cell cell = row.createCell(j);
                cell.setCellStyle(bodyCellStyle);
//                if (StrUtil.isWrap(cellText, "$")) {
//                    insertLatexFormulaImage(sheet, currRowIndex, j, cellText);
//                } else {
                cell.setCellValue(cellText);
//                }
                int rowSpan = Convert.toInt(rowCellObj.getStr("IdmIsRowspan"), 1);
                int colSpan = Convert.toInt(rowCellObj.getStr("IdmIsColspan"), 1);
                if (rowSpan > 1 || colSpan > 1) {
                    // 暂存合并区域
                    mergedRegions.add(new CellRangeAddress(
                            currRowIndex, currRowIndex + rowSpan - 1,
                            j, j + colSpan - 1
                    ));
                }
            }
        }

        deleteRows(sheet, mergedRegions, delRowIndexList); // 删除一行后，由于上移，注意行号变了！

        // 列宽自适应
        for (int i = 0; i < header.size(); i++) {
            sheet.autoSizeColumn(i, true);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    public CellStyle createBodyCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        // 设置边框样式
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        cellStyle.setWrapText(true); // 设置自动换行
        return cellStyle;
    }

    public CellStyle createHeaderCellStyle(Workbook workbook) {
        // 创建字体对象
        Font font = workbook.createFont();
        font.setBold(true); // 设置粗体
        font.setFontName("黑体"); // 设置字体为黑体

        CellStyle cellStyle = createBodyCellStyle(workbook);
        cellStyle.setAlignment(HorizontalAlignment.CENTER); // 设置水平居中对齐
        cellStyle.setFont(font);
        return cellStyle;
    }

    public void deleteRows(Sheet sheet, List<CellRangeAddress> mergedRegions, List<Integer> delRowIndexList) {
        delRowIndexList.sort(Integer::compare);
        for (int i = delRowIndexList.size() - 1; i >= 0; i--) { // 由于删除行会导致行号迁移，所以升序排序后从后往前删除
            int delRowIndex = delRowIndexList.get(i);
            // 统一纠正合并区域
            fixMergedRegions(sheet, delRowIndex, mergedRegions);
            // 注意删除行，一定要在重新写入合并区域之后
            deleteRowDirectly(sheet, delRowIndex);
        }
        // 最后统一设置纠正后的合并区域
        mergedRegions.forEach(sheet::addMergedRegion);
    }

    public void deleteRowDirectly(Sheet sheet, int delRowIndex) {
        if (Objects.isNull(sheet.getRow(delRowIndex))) {
            return;
        }
        // 删除
        sheet.removeRow(sheet.getRow(delRowIndex));
        // 上移
        if (delRowIndex < sheet.getLastRowNum()) {
            sheet.shiftRows(delRowIndex + 1, sheet.getLastRowNum(), -1);
        }
    }

    public void fixMergedRegions(Sheet sheet, int delRowIndex, List<CellRangeAddress> mergedRegions) {
        List<CellRangeAddress> fixedMergedRegions = new ArrayList<>();
        for (CellRangeAddress cellAddress : mergedRegions) {
            // 合并区域包含了删除行，需要处理该合并区域
            if (delRowIndex >= cellAddress.getFirstRow() && delRowIndex <= cellAddress.getLastRow()) {
                if (delRowIndex == cellAddress.getFirstRow() && delRowIndex + 1 <= sheet.getLastRowNum()) {
                    // 将合并区域左上角的值复制到下一行
                    Cell srcCell = sheet.getRow(delRowIndex).getCell(cellAddress.getFirstColumn());
                    Cell destCell = sheet.getRow(delRowIndex + 1).getCell(cellAddress.getFirstColumn());
//                    CellUtil.copyCell(srcCell, destCell, new CellCopyPolicy(), new CellCopyContext());
                }
                int newLastRow = cellAddress.getLastRow() - 1; // 合并区域底边向上收缩一行
                //sheet.removeMergedRegion(i); // 先移除合并区域。下面的Add导致下标变了
                //if (newLastRow >= cellAddress.getFirstRow() && cellAddress.getLastColumn() >= cellAddress.getFirstColumn()) { // 区域必须大于一个单元格
                // 暂存纠正后的合并区域
                fixedMergedRegions.add(
                        new CellRangeAddress(
                                cellAddress.getFirstRow(), newLastRow,
                                cellAddress.getFirstColumn(), cellAddress.getLastColumn()
                        )
                );
                //}
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
        for (CellRangeAddress mergedRegion : fixedMergedRegions) {
            // 某一方向的坐标非法，跳过
            int difRow = mergedRegion.getLastRow() - mergedRegion.getFirstRow();
            int difCol = mergedRegion.getLastColumn() - mergedRegion.getFirstColumn();
            if (difRow < 0 || difCol < 0) {
                continue;
            }
            // 矩形只有一个单元格
            if (difRow == 0 && difCol == 0) {
                continue;
            }
            mergedRegions.add(mergedRegion);
        }
    }

    public String getCellFormattedText(JSONObject cellObj) {
        JSONArray childCells = cellObj.getByPath("osattrs.cellChildren", JSONArray.class);
        if (ObjectUtil.isEmpty(childCells)) {
            return null;
        }
        StringBuilder sbd = new StringBuilder();
        for (int i = 0; i < childCells.size(); i++) {
            JSONObject childCellObj = childCells.getJSONObject(i);
            String inputValue = ObjectUtil.defaultIfEmpty(childCellObj.getByPath("oattrs.rel.IdmRelInputValue", String.class), "____");
            String textWithPlaceHolder = childCellObj.getStr("IdmItemText", "");
            sbd.append(textWithPlaceHolder.replaceAll("\\$\\{([^{}]+)}", inputValue));
        }
        return sbd.toString();
    }

    private static void insertLatexFormulaImage(Sheet sheet, int row, int col, String latexFormula) {
        // Use your preferred tool to convert LaTeX to an image (e.g., MathType)
        // Replace "path/to/latex-image.png" with the actual path to the LaTeX image
        TeXFormula formula = new TeXFormula(latexFormula);

        // Create a drawing patriarch to hold the image
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        Workbook workbook = sheet.getWorkbook();

        // Add a picture shape
        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
//        anchor.setAnchorType(XSSFClientAnchor.AnchorType.MOVE_AND_RESIZE);

        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 1);
        anchor.setRow2(row + 1);
        // Load the image
        try {
            BufferedImage image = (BufferedImage) formula.createBufferedImage(TeXConstants.STYLE_DISPLAY, 20, null, null);
            int pictureIndex = workbook.addPicture(bufferedImageToBytes(image), Workbook.PICTURE_TYPE_PNG);
            Picture picture = drawing.createPicture(anchor, pictureIndex);
            // 自适应单元格大小
            picture.resize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] bufferedImageToBytes(BufferedImage image) throws IOException {
        // Convert BufferedImage to bytes
        // This is a simplified conversion and may not cover all image types
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
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
