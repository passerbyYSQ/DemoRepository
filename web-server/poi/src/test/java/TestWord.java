import org.apache.poi.xwpf.usermodel.*;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/1/30
 */
public class TestWord {

    @Test
    public void testWord() {
        // 创建文档对象
        XWPFDocument document = new XWPFDocument();

        // 创建表格对象
        int numRows = 3;
        int numCols = 4;
        XWPFTable table = document.createTable(numRows, numCols);

        // 设置表格样式
        table.setTableAlignment(TableRowAlign.CENTER);
        table.setWidth("100%");

        // 操作表格内容
        for (int row = 0; row < numRows; row++) {
            XWPFTableRow tableRow = table.getRow(row);
            for (int col = 0; col < numCols; col++) {
                XWPFTableCell cell = tableRow.getCell(col);
                cell.setText("Row " + (row + 1) + ", Col " + (col + 1));
            }
        }

        // 将文档保存到文件
        try (FileOutputStream out = new FileOutputStream("output.docx")) {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
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
    private void mergeCells(XWPFTable table, int startRow, int startCol, int endRow, int endCol) {
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                XWPFTableCell cell = table.getRow(row).getCell(col);
                if (row == startRow && col == startCol) {
                    // 第一个单元格设置合并标志为RESTART
                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
                } else {
                    // 其他单元格设置合并标志为CONTINUE
                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
                }
            }
        }
    }
}
