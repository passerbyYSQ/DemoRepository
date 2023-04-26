package top.ysqorz.tree.trim;

import top.ysqorz.tree.TreeNode;

import java.util.function.Predicate;

/**
 * 修剪树的算法
 *
 * @author passerbyYSQ
 * @create 2022-07-30 14:44
 */
public interface TrimTreeAlgorithm<T> {

    /**
     * 修剪算法的实现声明
     */
    void trim(TreeNode<T> root, Predicate<TreeNode<T>> retainPredicate);
}
