package top.ysqorz.demo.builder;

import top.ysqorz.demo.TreeNode;
import top.ysqorz.demo.trim.impl.NonRecursionTrim;

import java.util.*;
import java.util.function.Predicate;

public class TreeBuilder<T> {
    private TreeNode<T> root;
    private int depth;
    private Predicate<TreeNode<T>> retainFilter; // nullable
    private Comparator<TreeNode<T>> comparator; // nullable
    private DataLoader<T> dataLoader;

    public TreeBuilder(TreeNode<T> root, DataLoader<T> dataLoader) {
        this(root, dataLoader, 0, null, null);
    }

    public TreeBuilder(TreeNode<T> root, DataLoader<T> dataLoader, int depth,
                       Predicate<TreeNode<T>> retainFilter, Comparator<TreeNode<T>> comparator) {
        assert root != null;
        assert dataLoader != null;
        this.root = root;
        this.depth = depth;
        this.retainFilter = retainFilter;
        this.comparator = comparator;
        this.dataLoader = dataLoader;
    }

    public void build() {
        Queue<TreeNode<T>> queue = new ArrayDeque<>();
        queue.offer(root);
        if (depth > 0) {
            // 展开depth层
            for (int i = 0; i < depth && !queue.isEmpty(); i++) {
                int size = queue.size(); // 使用一个变量暂存当前层的节点个数，否则poll之后queue.size()会改变
                for (int j = 0; j < size; j++) {
                    expandChildren(queue);
                }
            }
        } else {
            // 展开整棵树
            while (!queue.isEmpty()) {
                expandChildren(queue);
            }
        }
        // 此时下一层(如果存在的话)的节点已经放入队列，需要清空队列
        queue.clear();
        // 根据筛选条件修剪树
        if (retainFilter != null) {
            NonRecursionTrim<T> nonRecursionTrim = new NonRecursionTrim<>();
            nonRecursionTrim.trim(root, retainFilter);
        }
    }

    private void expandChildren(Queue<TreeNode<T>> queue) {
        TreeNode<T> head = queue.poll();
        if (head == null) {
            return;
        }
        List<TreeNode<T>> children = dataLoader.loadChildren(head);
        if (children == null) {
            return;
        }
        if (comparator != null) {
            children.sort(comparator);
        }
        head.setChildren(children);
        for (TreeNode<T> child : children) {
            child.setLevel(head.getLevel() + 1);
            child.setParentId(head.getId());
            queue.offer(child);
        }
    }

    public interface DataLoader<T> {
        List<TreeNode<T>> loadChildren(TreeNode<T> parent);
    }
}
