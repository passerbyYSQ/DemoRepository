package top.ysqorz.demo;

import org.junit.Before;
import org.junit.Test;
import top.ysqorz.demo.trim.impl.NonRecursionTrim;
import top.ysqorz.demo.trim.impl.RecursionTrim;

import java.util.Arrays;

/**
 * @author passerbyYSQ
 * @create 2022-07-30 16:37
 */
public class TestTrimTree {
    private TreeNode<Integer> root1;

    @Test
    public void testRecursionTrim() {
        RecursionTrim<Integer> recursionTrim = new RecursionTrim<>();
        recursionTrim.trim(root1, node -> node.getData().equals(7) || node.getData().equals(3));
        System.out.println("裁剪成功");
    }

    @Test
    public void testNonRecursionTrim() {
        NonRecursionTrim<Integer> nonRecursionTrim = new NonRecursionTrim<>();
        nonRecursionTrim.trim(root1, node -> node.getData().equals(7) || node.getData().equals(3));
        System.out.println("裁剪成功");
    }

    @Before
    public void createTree() {
        root1 = new TreeNode<>(1);
        TreeNode<Integer> root2 = new TreeNode<>(2);
        TreeNode<Integer> root3 = new TreeNode<>(3);
        TreeNode<Integer> root4 = new TreeNode<>(4);
        TreeNode<Integer> root5 = new TreeNode<>(5);
        TreeNode<Integer> root6 = new TreeNode<>(6);
        TreeNode<Integer> root7 = new TreeNode<>(7);
        TreeNode<Integer> root8 = new TreeNode<>(8);

        root1.addChild(Arrays.asList(root2, root3));
        root2.addChild(Arrays.asList(root4, root5, root6));
        root5.addChild(Arrays.asList(root7, root8));
        System.out.println("创建树成功");
    }
}
