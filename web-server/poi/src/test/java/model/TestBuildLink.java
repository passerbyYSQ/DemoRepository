package model;

import cn.hutool.core.util.ObjectUtil;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/5/21
 */
public class TestBuildLink {

    @Test
    public void test01() {

        List<Row> rows = Arrays.asList(
                new Row(1, "A", null),
                new Row(1, "C", null),
                new Row(1, "B", "A"),
                new Row(1, "D", null),
                new Row(1, "F", null),
                new Row(1, "H", "E"),
                new Row(1, "I", "E"),
                new Row(1, "E", "D")
        );
        List<Row> res = sortRowsAndBuildLink(rows);
        for (Row re : res) {
            System.out.print(re.getUUID() + " ");
        }
    }

    @Test
    public void test02() {
        List<Row> rows = Arrays.asList(
                new Row(1, "J", null),
                new Row(1, "I", "F"),
                new Row(1, "H", "E"),
                new Row(1, "G", "E"),
                new Row(1, "F", "D"),
                new Row(1, "E", "D"),
                new Row(1, "D", null),
                new Row(1, "C", null),
                new Row(1, "B", "A"),
                new Row(1, "A", null)
        );
        List<Row> res = sortRowsAndBuildLink(rows);
        for (Row re : res) {
            System.out.print(re.getUUID() + " ");
        }
    }

    @Test
    public void test03() {
        List<Row> rows = Arrays.asList(
                new Row(1, "B", "A"),
                new Row(1, "A", null),
                new Row(1, "C", null),
                new Row(1, "D", null),
                new Row(1, "F", "D"),
                new Row(1, "H", "E"),
                new Row(1, "I", "E"),
                new Row(1, "E", "D"),
                new Row(1, "J", "F"),
                new Row(1, "K", "F"),
                new Row(1, "M", "F"),
                new Row(1, "N", "K")
        );
        List<Row> res = sortRowsAndBuildLink(rows);
        for (Row re : res) {
            System.out.print(re.getUUID() + " ");
        }
    }


    public List<Row> sortRowsAndBuildLink(List<Row> rows) {
        // key: UUID, value: Row
        Map<String, Row> rowMap = rows.stream().collect(Collectors.toMap(Row::getUUID, Function.identity()));
        Set<String> visit = new HashSet<>();

        // 构建森林
        for (Row row : rows) {
            Row curr = row;
            while (ObjectUtil.isNotEmpty(curr)) {
                if (visit.contains(curr.getUUID())) {
                    break;
                }
                visit.add(curr.getUUID());

                Row pre = rowMap.get(curr.getPreUUID());
                if (ObjectUtil.isEmpty(pre)) {
                    break;
                }
                curr.setPreRow(pre);
                pre.addNextRows(curr); // 添加的时候就
                curr = pre;
            }

        }

        // 链表头节点排序
        List<Row> headList = rows.stream().filter(row -> ObjectUtil.isEmpty(row.getPreRow()))
                .sorted(Comparator.comparing(Row::getUUID)) // 替换为行号升序
                .collect(Collectors.toList());

        // 重新展开为平级数组
        List<Row> res = new ArrayList<>();

        for (Row head : headList) {
            // 重新展开为平级数组。先序遍历
            preOrderTraversal(head, res);
        }

        return res;
    }

    public void preOrderTraversal(Row head, List<Row> res) {
        if (Objects.isNull(head)) {
            return;
        }
        res.add(head);

        List<Row> nextRows = head.getNextRows();
        if (ObjectUtil.isEmpty(nextRows)) {
            return;
        }
        nextRows.sort(Comparator.comparing(Row::getUUID)); // 替换为创建时间倒序
        for (Row nextRow : nextRows) {
            preOrderTraversal(nextRow, res);
        }
    }

}
