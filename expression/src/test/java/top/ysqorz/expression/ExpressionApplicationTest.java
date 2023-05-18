package top.ysqorz.expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import top.ysqorz.expression.path.BeanPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExpressionApplication.class})
public class ExpressionApplicationTest {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void test() { // equals($s.Projects[0])
        String input = "func(arg1, arg2, arg3)";
        String regex = "^([a-zA-Z_$][a-zA-Z_$\\d]*)\\((\\s*.+\\s*(,\\s*.+\\s*)*)?\\)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String args = matcher.group(2);
            System.out.println("Method name: [" + methodName + "]");
            System.out.println("Args: [" + args + "]");
        }
    }

    @Test
    public void test1() {
        String[] split = "ac, asf, sdg  , qfqwe".split("\\s*,\\s*");
        System.out.println(Arrays.toString(split));
    }

    /**
     *
     */
    @Test
    public void testSplit() {
        String path = "xxx.hello(xxxx, $o.haha(xxx, xxx), xxxx).xxx[0].xxx['123'].get(xxxx)";
        String[] syntaxArray = path.split("\\.(?![^(]*\\))"); // 根据【不在括号的点】分割，负向前瞻断言
        System.out.println(Arrays.toString(syntaxArray));
    }

    @Test
    public void testSplit2() {
        String path = "xxx.hello(xxxx, $o.haha(xxx, $s.user.uid), xxxx).xxx[0].xxx['123'].get(xxxx)";
        // 【不在括号的点】，即最外层的点
        int level = 0; // 括号层数
        StringBuilder sbd = new StringBuilder();
        List<String> syntaxList = new ArrayList<>();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '.' && level == 0) { // 不在括号的点，即最外层的点
                syntaxList.add(sbd.toString());
                sbd = new StringBuilder();
                continue;
            }
            if (c == '(') {
                level++;
            } else if (c == ')') {
                level--;
            }
            sbd.append(c);
        }
        System.out.println(syntaxList);
    }

    @Test
    public void testBeanPath() throws Exception {
        //String path = "$s.get(\"user\").projects[1].member.contains($o.participants.owners[0])";
        // $s.uid.xx.xxx[0].equal($o.Owner)
        String path = "$s.get(\"user\").get(keys.hello.haha)[1].member.contains($o.participants.owners[0])";
        File jsonFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "obj.json");
        Object source = objectMapper.readValue(jsonFile, Object.class);
        BeanPath beanPath = new BeanPath(path, source);
        System.out.println(beanPath.getValue());
    }
}
