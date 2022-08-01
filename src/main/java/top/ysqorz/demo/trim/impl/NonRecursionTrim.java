package top.ysqorz.demo.trim.impl;

import top.ysqorz.demo.TreeNode;
import top.ysqorz.demo.trim.TrimTreeAlgorithm;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author passerbyYSQ
 * @create 2022-07-30 15:25
 */
public class NonRecursionTrim<T> implements TrimTreeAlgorithm<T> {
    @Override
    public void trim(TreeNode<T> root, Predicate<TreeNode<T>> retainPredicate) {
        Deque<TreeNode<T>> nodeStack = new ArrayDeque<>();
        Deque<Integer> childIdxStack = new ArrayDeque<>();
        // 注意以地址作为key。如果重写了equals，由于中途child的移除，使用equals会导致判断不是原来的自己
        Map<TreeNode<T>, TreeNode<T>> parentMap = new HashMap<>(); // 维护额外的父子关系
        Map<TreeNode<T>, Boolean> trimFlagMap = new HashMap<>(); // 裁剪标志，各个节点的裁剪标志默认为true

        TreeNode<T> curr = root;
        while (curr != null || !nodeStack.isEmpty()) {
            // 纵向入栈
            while (curr != null) {
                nodeStack.push(curr);
                List<TreeNode<T>> children = curr.getChildren();
                TreeNode<T> parent = curr;
                curr = null;
                if (children != null && !children.isEmpty()) {
                    curr = children.get(0);
                    parentMap.put(curr, parent);
                    childIdxStack.push(0);
                }
            }
            // 出栈
            curr = nodeStack.pop();

            // 寻找下一个入栈的子节点
            List<TreeNode<T>> children = curr.getChildren();
            TreeNode<T> nextChild = null;
            if (children != null && !children.isEmpty()) {
                // 弹出上一次入栈的子节点索引，+1即为下一个入栈的子节点
                int nextChildIdx = childIdxStack.pop() + 1;
                if (nextChildIdx < children.size()) {
                    nextChild = children.get(nextChildIdx);
                    parentMap.put(nextChild, curr);
                    childIdxStack.push(nextChildIdx);
                }
            }

            if (nextChild == null) {
                System.out.println(curr);
                TreeNode<T> parent = parentMap.get(curr);
                if (parent != null) {
                    boolean isTrim = trimFlagMap.getOrDefault(curr, true) && !retainPredicate.test(curr);
                    // 父节点裁剪标志的中间结果暂存
                    trimFlagMap.put(parent, isTrim && trimFlagMap.getOrDefault(parent, true));
                    if (isTrim) {
                        children = parent.getChildren();
                        children.remove(curr);// children肯定不为null，因为至少有curr
                        if (!children.isEmpty()) {
                            // 由于移除了一个child，所以下一个入栈的child的索引也会-1
                            childIdxStack.push(childIdxStack.pop() - 1);
                        }
                    }
                }
            } else {
                nodeStack.push(curr); // 将父节点重新入栈
            }
            curr = nextChild;
        }

        trimFlagMap.clear();
        parentMap.clear();
    }
}
