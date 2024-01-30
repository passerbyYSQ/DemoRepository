import org.junit.Test;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;

/**
 * ...
 * https://www.latexlive.com/
 *
 * @author yaoshiquan
 * @date 2024/1/30
 */
public class TestJLaTeXMath {

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
