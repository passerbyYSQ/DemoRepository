package top.ysqorz.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author passerbyYSQ
 * @create 2022-07-30 14:40
 */
@Getter
@Setter
@NoArgsConstructor
public class TreeNode<T> {
    private T data;
    private int level;
    private List<TreeNode<T>> children = new ArrayList<>();

    public TreeNode(T data) {
        this.data = data;
    }

    public void addChild(List<TreeNode<T>> childrenToAdd) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.addAll(childrenToAdd);
    }

    @Override
    public String toString() {
        return this.data.toString();
    }
}
