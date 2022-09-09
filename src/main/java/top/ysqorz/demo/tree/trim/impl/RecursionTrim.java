package top.ysqorz.demo.tree.trim.impl;

import top.ysqorz.demo.tree.TreeNode;
import top.ysqorz.demo.tree.trim.TrimTreeAlgorithm;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author passerbyYSQ
 * @create 2022-07-30 14:50
 */
public class RecursionTrim<T> implements TrimTreeAlgorithm<T> {

    @Override
    public void trim(TreeNode<T> root, Predicate<TreeNode<T>> retainPredicate) {
        dfs(root, retainPredicate);
    }

    /**
     * 递归实现树的裁剪
     * @return  是否裁剪
     */
    private boolean dfs(TreeNode<T> root, Predicate<TreeNode<T>> notTrimPredicate) {
        if (root == null) {
            return true; // 裁剪
        }

        List<TreeNode<T>> children = root.getChildren();
        boolean isTrim = true;
        if (children != null) {
            Iterator<TreeNode<T>> iterator = children.iterator();
            while (iterator.hasNext()) {
                TreeNode<T> child = iterator.next();
                if (!dfs(child, notTrimPredicate)) {
                    isTrim = false;
                } else {
                    iterator.remove(); // 使用增强for遍历时不允许修改集合
                }
            }
        }
        System.out.println(root); // 后续遍历
        // 所有孩子都裁剪，且root也需要裁剪，则裁剪
        // 只要有任一个孩子保留，或者root需要保留，则保留
        return isTrim && !notTrimPredicate.test(root);
    }
}
