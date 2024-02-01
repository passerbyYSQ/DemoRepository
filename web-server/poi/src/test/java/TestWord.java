import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.junit.Test;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMath;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMathPara;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/1/30
 */
public class TestWord {
    static File stylesheet = new File("E:\\Project\\IdeaProjects\\DemoRepository\\web-server\\poi\\src\\test\\resources\\json\\MML2OMML.XSL");
    static TransformerFactory tFactory = TransformerFactory.newInstance();
    static StreamSource stylesource = new StreamSource(stylesheet);

    static CTOMath getOMML(String mathML) throws Exception {
        Transformer transformer = tFactory.newTransformer(stylesource);

        StringReader stringreader = new StringReader(mathML);
        StreamSource source = new StreamSource(stringreader);

        StringWriter stringwriter = new StringWriter();
        StreamResult result = new StreamResult(stringwriter);
        transformer.transform(source, result);

        String ooML = stringwriter.toString();
        stringwriter.close();

        CTOMathPara ctOMathPara = CTOMathPara.Factory.parse(ooML);
        CTOMath ctOMath = ctOMathPara.getOMathArray(0);

        //for making this to work with Office 2007 Word also, special font settings are necessary
        XmlCursor xmlcursor = ctOMath.newCursor();
        while (xmlcursor.hasNextToken()) {
            XmlCursor.TokenType tokentype = xmlcursor.toNextToken();
            if (tokentype.isStart()) {
                if (xmlcursor.getObject() instanceof CTR) {
                    CTR cTR = (CTR) xmlcursor.getObject();
                    cTR.addNewRPr2().addNewRFonts().setAscii("Cambria Math");
//                    cTR.getRPr2().getRFontsArray(0).setHAnsi("Cambria Math"); // since apache poi 5.0.0
                }
            }
        }

        return ctOMath;
    }


    public static String convertToMathML(String latex) throws IOException {
        SnuggleEngine engine = new uk.ac.ed.ph.snuggletex.SnuggleEngine();
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new uk.ac.ed.ph.snuggletex.SnuggleInput(latex);
        session.parseInput(input);
        return session.buildXMLString();

    }


    @Test
    public void finalTest() throws Exception {
        String json = IoUtil.readUtf8(ResourceUtil.getStream("json/4.1.7 气压水罐容积计算表.json"));
        JSONObject selectionTable = new JSONObject(json);
        OutputStream outputStream = Files.newOutputStream(new File("E:\\Project\\IdeaProjects\\DemoRepository\\web-server\\poi\\src\\test\\resources\\json", "testWord.docx").toPath());
        writeSelectionTableMatrix(selectionTable, outputStream);
    }

    public void writeSelectionTableMatrix(JSONObject selectionTable, OutputStream outputStream) throws Exception {
        // 创建一个新的Word文档
        XWPFDocument document = new XWPFDocument();
//        XWPFParagraph paragraph = document.createParagraph();
//        XWPFRun run = paragraph.createRun();
//        run.setText(selectionTable.getStr("ObjectDisplayName"));
//        run.setBold(true);
//        run.setFontSize(10);
//        run.setFontFamily("黑体");


        JSONArray header = selectionTable.getByPath("osattrs.tableHeader.osattrs.IdmTableHeaderObjset[0]", JSONArray.class);
        JSONArray rowObjs = selectionTable.getByPath("osattrs.tableRows", JSONArray.class);

        int numRows = rowObjs.size() + 1;
        int numCols = header.size();
        XWPFTable table = document.createTable(numRows, numCols);
        table.setWidth("100%");


        List<CellRangeAddress> mergedRegions = new ArrayList<>();

        // 表头
        int currRowIndex = 0;
        for (int i = 0; i < numCols; i++) {
            JSONObject cellObj = header.getJSONObject(i);
            XWPFTableCell cell = table.getRow(currRowIndex).getCell(i);
//            cell.setWidthType(TableWidthType.PCT); // 百分比
//            cell.setWidth("1%"); // TODO 列宽
//            cell.setWidthType(TableWidthType.NIL);

//            cell.setText(cellObj.getStr("IdmItemText"));
            XWPFParagraph paragraph = cell.getParagraphs().get(cell.getParagraphs().size() - 1);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();
            run.setTextPosition(1);
            run.setText(cellObj.getStr("IdmItemText"));

            // 暂存合并区域
            int rowSpan = Convert.toInt(cellObj.getStr("IdmIsRowspan"), 1);
            int colSpan = Convert.toInt(cellObj.getStr("IdmIsColspan"), 1);
            if (rowSpan > 1 || colSpan > 1) {
                mergedRegions.add(new CellRangeAddress( // 暂存合并区域
                        0, rowSpan - 1,
                        i, i + colSpan - 1
                ));
            }
        }
        List<Integer> delRowIndexList = new ArrayList<>();
        currRowIndex++;
        for (int i = 0; i < rowObjs.size(); i++, currRowIndex++) {
            JSONObject rowObj = rowObjs.getJSONObject(i);
            JSONObject rRowValue = rowObj.getByPath("oattrs.rel", JSONObject.class);
            JSONArray rowCellObjs = rowObj.getByPath("osattrs.IdmTableRowObjset", JSONArray.class);

            // 记录删除的行
            if (Objects.isNull(rRowValue) || !Objects.equals(rRowValue.getStr("IdmImplementation"), "+")) {
                delRowIndexList.add(currRowIndex);
            }

            for (int j = 0; j < rowCellObjs.size(); j++) {
                JSONObject rowCellObj = rowCellObjs.getJSONObject(j);
                XWPFTableCell cell = table.getRow(currRowIndex).getCell(j);
                XWPFParagraph par = getCellFormattedText(cell, rowCellObj);

//                cell.setWidthType(TableWidthType.PCT); // 百分比
//                cell.setWidth("1%"); // TODO 列宽
//                cell.setWidthType(TableWidthType.a);
//                cell.setText(cellText);

                // 暂存合并区域
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


        deleteRows(table, mergedRegions, delRowIndexList); // 删除一行后，由于上移，注意行号变了！

        // 不要自适应，否则合并效果有部分失效
//        for (int i = 0; i < table.getNumberOfRows(); i++) {
//            List<XWPFTableCell> cells = table.getRow(i).getTableCells();
//            for (XWPFTableCell cell : cells) {
//                cell.setWidthType(TableWidthType.AUTO);
//            }
//        }

        // 创建段落

        // 插入数学公式
//       String latex = "$x=\\frac{-b\\pm\\sqrt{b^2-4ac}}{2a}$";
//       String mathML = convertToMathML(latex);
//        CTOMath ctoMath = getOMML(mathML);
//        // 创建段落
//        XWPFParagraph paragraph = table.getRow(1).getCell(1).addParagraph();
//        XWPFRun run = paragraph.createRun();
//        run.setText("The Quadratic Formula: ");
//        // 将 MathML 插入段落
//        paragraph.getCTP().setOMathArray(new CTOMath[] { ctoMath }); // 不要addNewOMath
//        XWPFRun inlineRun = paragraph.createRun();
//        inlineRun.setText("结束啦");

        setTableFontSize(table, 8);

        // 保存Word文档
        document.write(outputStream);
        document.close();

        System.out.println("表格插入完成。");
    }

    public void setTableFontSize(XWPFTable table, int fontSize) {
        // POI4不生效
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
//                    CTP ctp = paragraph.getCTP();
//                    CTPPr ppr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();
//                    CTParaRPr paraRpr = ppr.isSetRPr() ? ppr.getRPr() : ppr.addNewRPr();
//                    CTHpsMeasure fontSize1 = paraRpr.isSetSz() ? paraRpr.getSz() : paraRpr.addNewSz();
//                    fontSize1.setVal(BigInteger.valueOf(fontSize));

                    for (XWPFRun run : paragraph.getRuns()) {
                        run.setFontSize(fontSize);
                    }
                }
            }
        }
    }

    public void deleteRows(XWPFTable table, List<CellRangeAddress> mergedRegions, List<Integer> delRowIndexList) {
        delRowIndexList.sort(Integer::compare);
        for (int i = delRowIndexList.size() - 1; i >= 0; i--) { // 由于删除行会导致行号迁移，所以升序排序后从后往前删除
            int delRowIndex = delRowIndexList.get(i);
            // 统一纠正合并区域
            fixMergedRegions(table, delRowIndex, mergedRegions);
            table.removeRow(delRowIndex);
        }
        // 最后统一设置纠正后的合并区域
        for (CellRangeAddress mergedRegion : mergedRegions) {
            mergeCellsInRectangle(table, mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(),
                    mergedRegion.getLastRow(), mergedRegion.getLastColumn());
            System.out.printf("mergeCellsInRectangle(table, %d, %d, %d, %d);%n", mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(),
                    mergedRegion.getLastRow(), mergedRegion.getLastColumn());
        }

//        for (CellRangeAddress mergedRegion : mergedRegions) {
//            mergeCells(table, mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(),
//                    mergedRegion.getLastRow(), mergedRegion.getLastColumn());
//            System.out.printf("mergeCells(table, %d, %d, %d, %d);%n", mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(),
//                    mergedRegion.getLastRow(), mergedRegion.getLastColumn());
//        }

//        mergeCellsInRectangle(table, 0, 1, 0, 3);
//        mergeCellsInRectangle(table, 1, 1, 1, 2);
//        mergeCellsInRectangle(table, 2, 1, 2, 2);
//        mergeCellsInRectangle(table, 3, 0, 3, 1);
//        mergeCellsInRectangle(table, 3, 2, 3, 3);
//        mergeCellsInRectangle(table, 4, 0, 4, 1);
//        mergeCellsInRectangle(table, 4, 2, 4, 3);
//        mergeCellsInRectangle(table, 5, 0, 5, 1);
//        mergeCellsInRectangle(table, 5, 2, 5, 3);
//        mergeCellsInRectangle(table, 6, 0, 6, 1);
//        mergeCellsInRectangle(table, 6, 2, 6, 3);
//        mergeCellsInRectangle(table, 7, 0, 7, 1);
//        mergeCellsInRectangle(table, 7, 2, 7, 3);
//        mergeCellsInRectangle(table, 8, 0, 8, 1);
//        mergeCellsInRectangle(table, 8, 2, 8, 3);
//        mergeCellsInRectangle(table, 9, 0, 9, 1);
//        mergeCellsInRectangle(table, 9, 2, 9, 3);
//        mergeCellsInRectangle(table, 10, 0, 10, 1);
//        mergeCellsInRectangle(table, 10, 2, 10, 3);

//        table.getRow(10).getCell(2).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
//        table.getRow(10).getCell(3).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
//
//        table.getRow(10).getCell(0).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
//        table.getRow(10).getCell(1).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);


//        mergeCellsHorizontal(table, 10, 0, 1);

    }

    /**
     * @Description: 跨列合并
     * table要合并单元格的表格
     * row要合并哪一行的单元格
     * fromCell开始合并的单元格
     * toCell合并到哪一个单元格
     */
    public void mergeCellsHorizontal(XWPFTable table, int row, int fromCol, int toCol) {
//        for (int cellIndex = fromCol; cellIndex <= toCol; cellIndex++) {
//            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
//            if (cellIndex == fromCol) {
//                // The first merged cell is set with RESTART merge value
//                // 第一个合并单元格设置为RESTART合并值
//                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
//            } else {
//                // Cells which join (merge) the first one, are set with CONTINUE
//                // 使用CONTINUE设置连接（合并）第一个单元格的单元格
//                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
//            }
//        }

//        for (int i = toCol; i > fromCol; i--) {
//            table.getRow(row).removeCell(i);
//        }
        XWPFTableCell cell = table.getRow(row).getCell(fromCol);
        BigInteger span = Convert.toBigInteger(toCol - fromCol + 1);
        if (cell.getCTTc().getTcPr().getGridSpan() == null) cell.getCTTc().getTcPr().addNewGridSpan();
        cell.getCTTc().getTcPr().getGridSpan().setVal(span);
        System.out.println("Span=" + (toCol - fromCol + 1));
    }

    /**
     * @Description: 跨行合并
     * table要合并单元格的表格
     * col要合并哪一列的单元格
     * fromRow从哪一行开始合并单元格
     * toRow合并到哪一个行
     */
    public void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
//        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
//            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
//            if (rowIndex == fromRow) {
//                // 第一个合并单元格设置为RESTART合并值
//                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
//            } else {
//                // 使用CONTINUE设置连接（合并）第一个单元格的单元格
//                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
//            }
//        }
    }

    public void fixMergedRegions(XWPFTable table, int delRowIndex, List<CellRangeAddress> mergedRegions) {
        List<CellRangeAddress> fixedMergedRegions = new ArrayList<>();
        for (CellRangeAddress cellAddress : mergedRegions) {
            // 合并区域包含了删除行，需要处理该合并区域
            if (delRowIndex >= cellAddress.getFirstRow() && delRowIndex <= cellAddress.getLastRow()) {
                if (delRowIndex == cellAddress.getFirstRow() && delRowIndex + 1 < table.getNumberOfRows()) {
                    // 将合并区域左上角的值复制到下一行
                    XWPFTableCell srcCell = table.getRow(delRowIndex).getCell(cellAddress.getFirstColumn());
                    XWPFTableCell destCell = table.getRow(delRowIndex + 1).getCell(cellAddress.getFirstColumn());
                    //destCell.getCTTc().setPArray(srcCell.getCTTc().getPArray());
                    destCell.setText(srcCell.getText());
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

    public XWPFParagraph getCellFormattedText(XWPFTableCell cell, JSONObject cellObj) throws Exception {
        JSONArray childCells = cellObj.getByPath("osattrs.cellChildren", JSONArray.class);
        if (ObjectUtil.isEmpty(childCells)) {
            return null;
        }

        XWPFParagraph paragraph = cell.getParagraphs().get(cell.getParagraphs().size() - 1);
        for (int i = 0; i < childCells.size(); i++) {
            JSONObject childCellObj = childCells.getJSONObject(i);
            String text = childCellObj.getStr("IdmItemText", "");

            if (StrUtil.isWrap(text, "$")) {
                String mathML = convertToMathML(text);
                CTOMath ctoMath = getOMML(mathML);
                // 创建段落
                XWPFRun run = paragraph.createRun();
                run.setText("");
                // 将 MathML 插入段落
                paragraph.getCTP().setOMathArray(new CTOMath[]{ctoMath}); // 不要addNewOMath
            } else if (StrUtil.isWrap(text, "${", "}")) {
                String inputValue = ObjectUtil.defaultIfEmpty(childCellObj.getByPath("oattrs.rel.IdmRelInputValue", String.class), "____");
                XWPFRun run = paragraph.createRun();
                run.setText(text.replaceAll("\\$\\{([^{}]+)}", inputValue));
            } else {
                XWPFRun run = paragraph.createRun();
                run.setText(text);
            }
        }
        return paragraph;
    }

    @Test
    public void testWord() {
        try (OutputStream out = new FileOutputStream("example.docx")) {
            // 创建一个新的Word文档
            XWPFDocument document = new XWPFDocument();

            int numRows = 2;
            int numCols = 2;
            XWPFTable table = document.createTable(numRows, numCols); // 创建一个1行3列的表格

            // 设置表格宽度为页面宽度
            table.setWidth("100%");
            String[] per = {"1%", "1%", "1%", "1%"};

            // 在表格中添加内容
            // 操作表格内容
            for (int row = 0; row < numRows; row++) {
                XWPFTableRow tableRow = table.getRow(row);
                for (int col = 0; col < numCols; col++) {
                    XWPFTableCell cell = tableRow.getCell(col);
//                    CTTblWidth cellWidth = cell.getCTTc().addNewTcPr().addNewTcW();
//                    cellWidth.setType(STTblWidth.DXA);
//                    cell.setWidth(String.valueOf(9072)); // 设置列宽为1英寸（9072单位）
                    cell.setWidthType(TableWidthType.PCT); // 百分比
                    cell.setWidth(per[col]);

//                    cell.setWidthType(TableWidthType.AUTO);
                    cell.setText("Row " + row + ", Col " + col);
                }
            }

            mergeCellsHorizontal(table, 0, 0, 1);
            mergeCellsHorizontal(table, 1, 0, 1);
            mergeCellsVertically(table, 0, 0, 1);
//            mergeCellsInRectangle(table, 2, 0, 2, 1);
//            mergeCellsInRectangle(table, 3, 0, 3, 1);
//            mergeCellsInRectangle(table, 0, 1, 0, 3);
//            mergeCellsHorizontal(table, 1, 0, 2);
//            mergeCellsVertically(table, 0, 1, 2);
//            mergeCellsVertically(table, 1, 1, 2);

//            table.removeRow(2);

            // 保存Word文档
            document.write(out);
            document.close();

            System.out.println("表格插入完成。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeCells(XWPFTable table, int startRow, int startCol, int endRow, int endCol) {
        for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
            mergeCellsVertically(table, colIndex, startRow, endRow);
        }
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            mergeCellsHorizontal(table, rowIndex, startCol, endCol);
        }
    }

    public void mergeCellsInRectangle(XWPFTable table, int startRow, int startCol, int endRow, int endCol) {
        for (int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
            for (int colIndex = startCol; colIndex <= endCol; colIndex++) {
                XWPFTableCell cell = table.getRow(rowIndex).getCell(colIndex);

//                cell.getCTTc().getTcPr().addNewGridSpan().setVal();

//                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
//                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
                STMerge.Enum hm = STMerge.CONTINUE; // 水平
                STMerge.Enum vm = STMerge.CONTINUE; // 竖直
                if (rowIndex == startRow) {
                    vm = STMerge.RESTART;
                }
                if (colIndex == startCol) {
                    hm = STMerge.RESTART;
                }
                if (Objects.isNull(cell.getCTTc().getTcPr())) {
                    cell.getCTTc().addNewTcPr();
                }
                if (Objects.isNull(cell.getCTTc().getTcPr().getHMerge())) {
                    cell.getCTTc().getTcPr().addNewHMerge();
                }
                cell.getCTTc().getTcPr().getHMerge().setVal(hm);
                if (Objects.isNull(cell.getCTTc().getTcPr().getVMerge())) {
                    cell.getCTTc().getTcPr().addNewVMerge();
                }
                cell.getCTTc().getTcPr().getVMerge().setVal(vm);
            }
        }
    }

    @Test
    public void test1() {
        try {
            XWPFDocument document = new XWPFDocument();
            XWPFTable table = document.createTable(5, 5); // 创建一个5x5的表格

            // 合并矩形区域
            mergeCells(table, 1, 1, 3, 3);

            // 填充表格内容
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    XWPFTableCell cell = table.getRow(row).getCell(col);
                    cell.setText("Cell " + (row + 1) + "-" + (col + 1));
                }
            }

            // 保存文档
            try (FileOutputStream out = new FileOutputStream("合并单元格的文档.docx")) {
                document.write(out);
                System.out.println("文档已生成！");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 合并矩形区域的单元格
//    private void mergeCells(XWPFTable table, int startRow, int startCol, int endRow, int endCol) {
//        for (int row = startRow; row <= endRow; row++) {
//            for (int col = startCol; col <= endCol; col++) {
//                XWPFTableCell cell = table.getRow(row).getCell(col);
//                if (row == startRow && col == startCol) {
//                    // 第一个单元格设置合并标志为RESTART
//                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
//                } else {
//                    // 其他单元格设置合并标志为CONTINUE
//                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
//                }
//            }
//        }
//    }
}
