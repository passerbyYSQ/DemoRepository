import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Test;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMath;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.io.FileOutputStream;

/**
 * ...
 * https://www.latexlive.com/
 *
 * @author yaoshiquan
 * @date 2024/1/30
 */
public class TestJLaTeXMath {


    @Test
    public void testLMath() {
        try {
            // 创建文档
            XWPFDocument document = new XWPFDocument();

            String xml = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mfrac><mrow><mn>1</mn></mrow><mrow><mn>2</mn></mrow></mfrac></math>";
            // 创建段落
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(xml);
            // 插入 OMML 公式
            CTOMath ctOMath = CTOMath.Factory.parse(xml);

            // 获取段落的 CTP (Common Text Paragraph) 对象
//            CTP ctp = paragraph.getCTP();
//            // 将 CTP 对象的 OMathArray 属性设置为包含 CTOMath 对象的数组
//            ctp.addNewOMath().setOMathArray(new CTOMath[] {ctOMath});
            // 保存文档
            FileOutputStream out = new FileOutputStream("example.docx");
            document.write(out);
            out.close();

            System.out.println("公式插入成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String latexExpression = "{\\mathrm{V}_{\\mathbf{q}2}=\\frac{\\alpha_{\\mathbf{a}}\\cdot q_{b}}{4n_{q}}}";

        // Parse LaTeX expression
        TeXFormula formula = new TeXFormula(latexExpression);
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);

        // Create a JLabel to display the icon
        JLabel label = new JLabel();
        label.setIcon(icon);

        // Display the label
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);
    }

    @Test
    public void test() {

    }
}
