package top.ysqorz.TransmittableThreadLocal.demo.tree;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 不要重写equals()和hashCode()
 *
 * @author passerbyYSQ
 * @create 2022-07-30 14:40
 */
@Getter
@Setter
@NoArgsConstructor
public class TreeNode<T> {
    private String id;
    private T data;
    private int level;
    private String parentId;
    private List<TreeNode<T>> children;

    public TreeNode(String id, T data) {
        this.id = id;
        this.data = data;
    }

    public TreeNode(T data) {
        this(UUID.randomUUID().toString(), data);
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
