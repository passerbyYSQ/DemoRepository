package top.ysqorz.demo;

import org.junit.Before;
import org.junit.Test;
import top.ysqorz.demo.builder.TreeBuilder;

import java.util.*;
import java.util.function.Predicate;

public class TestTreeBuilder {
    private Map<TreeNode<Integer>, List<TreeNode<Integer>>> childrenMap;
    private TreeNode<Integer> root1;
    private TreeBuilder.DataLoader<Integer> dataLoader;

    @Test
    public void testBuild() {
        TreeBuilder<Integer> treeBuilder = new TreeBuilder<>(root1, dataLoader);
        treeBuilder.build();
        System.out.println("树成功生成");
    }

    @Test
    public void testTrim() {
        Predicate<TreeNode<Integer>> retainPredicate = node -> node.getData().equals(7) || node.getData().equals(3);
        Comparator<TreeNode<Integer>> comparator = (o1, o2) -> Integer.compare(o2.getData(), o1.getData());
        TreeBuilder<Integer> treeBuilder = new TreeBuilder<>(root1, dataLoader, 0, retainPredicate, comparator);
        treeBuilder.build();
        System.out.println("树裁剪成功");
    }

    @Before
    public void initData() {
        root1 = new TreeNode<>(1);
        TreeNode<Integer> root2 = new TreeNode<>(2);
        TreeNode<Integer> root3 = new TreeNode<>(3);
        TreeNode<Integer> root4 = new TreeNode<>(4);
        TreeNode<Integer> root5 = new TreeNode<>(5);
        TreeNode<Integer> root6 = new TreeNode<>(6);
        TreeNode<Integer> root7 = new TreeNode<>(7);
        TreeNode<Integer> root8 = new TreeNode<>(8);

        childrenMap = new HashMap<>();

        // 不能直接使用 Arrays.asList，因为其返回的List是Arrays.ArrasyList对象，该类没有实现add和remove方法
        // 使用这两个方法时，会抛出 UnsupportedOperationException
        childrenMap.put(root1, new ArrayList<>(Arrays.asList(root2, root3)));
        childrenMap.put(root2, new ArrayList<>(Arrays.asList(root4, root5, root6)));
        childrenMap.put(root5, new ArrayList<>(Arrays.asList(root7, root8)));

        dataLoader = parent -> childrenMap.get(parent);
    }
}
