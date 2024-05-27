package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/5/21
 */
@Data
public class Row {
    private int level;
    private String UUID;
    private String preUUID;

    // 用于构建双向链表
    private Row preRow;
    private List<Row> nextRows = new ArrayList<>();

    public Row(int level, String UUID, String preUUID) {
        this.level = level;
        this.UUID = UUID;
        this.preUUID = preUUID;
    }

    public void addNextRows(Row row) {
        nextRows.add(row);
    }

    @Override
    public String toString() {
        return "Row{" +
                "level=" + level +
                ", UUID='" + UUID + '\'' +
                ", preUUID='" + preUUID + '\'' +
                '}';
    }
}
