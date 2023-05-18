package top.ysqorz.expression.path;

import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 形如：$s.Projects.xxxx.xxxx[0].contains($o.Owner)
 * <p>
 * xxx.hello(xxxx, $o.haha(xxx, xxx), xxxx).xxx[0].xxx['123'].get(xxxx)
 * <p>
 * TODO get("projects")[0]
 * <p>
 * $s.get("user").projects[1].member.contains($o.participants.owners[0])
 * $s.get("user").get($keys.hello.haha)[1].member.contains($o.participants.owners[0])
 */
@Data
public class BeanPath {
    private String path;
    private Object source;
    private List<PathVariable> pathVariableList;

    public BeanPath(String path, Object source) {
        this.path = path;
        this.source = source;
        List<String> syntaxArray = splitElements(path);
        pathVariableList = new ArrayList<>();
        for (String syntax : syntaxArray) {
            ArrayElementPathVariable arrElemPathVar = new ArrayElementPathVariable(syntax, source);
            if (arrElemPathVar.match()) {
                pathVariableList.add(arrElemPathVar);
                continue;
            }

            FuncPathVariable funcPathVar = new FuncPathVariable(syntax, source);
            if (funcPathVar.match()) {
                pathVariableList.add(funcPathVar);
                continue;
            }

            PropsPathVariable propsPathVar = new PropsPathVariable(syntax);
            if (propsPathVar.match()) {
                pathVariableList.add(propsPathVar);
            }
        }
    }

    /**
     * 根据【不在括号的点】，即最外层的点，进行分割
     */
    private List<String> splitElements(String path) {
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
        if (!ObjectUtils.isEmpty(sbd)) {
            syntaxList.add(sbd.toString());
        }
        return syntaxList;
    }

    public Object getValue() {
        Object reducer = source;
        int total = pathVariableList.size();
        for (int i = 0; i < total; i++) {
            PathVariable pathVar = pathVariableList.get(i);
            Object prevReducer = reducer;
            reducer = pathVar.reduce(reducer);
            if (Objects.isNull(reducer) && i < total - 1) { // 最后一个pathVar允许返回null
                throw new NullPointerException(String.format("表达式%s的返回值为null，前一个表达式的返回值为%s", pathVar.getSyntax(), prevReducer));
            }
        }
        return reducer;
    }
}
