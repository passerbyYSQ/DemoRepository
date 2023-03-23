package top.ysqorz.TransmittableThreadLocal.demo.expression;

import org.junit.Test;
import top.ysqorz.TransmittableThreadLocal.demo.expression.operand.NumericValue;
import top.ysqorz.TransmittableThreadLocal.demo.expression.operand.Operand;
import top.ysqorz.TransmittableThreadLocal.demo.expression.operand.StringValue;
import top.ysqorz.TransmittableThreadLocal.demo.expression.operand.Value;
import top.ysqorz.TransmittableThreadLocal.demo.expression.operator.*;
import top.ysqorz.demo.expression.operator.*;
import top.ysqorz.r2dbc.TransmittableThreadLocal.demo.expression.operator.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ExpressionParser {
    private static final String OPERATOR_CHARSET = "+-*/%><=!&|";

    @Test
    public void test() {
        List<ExpressionEntity> entityList = parse("1 + 2 * ( -3 + 4 )");
        Value result = calculateELOV(entityList);
        System.out.println(result.getBigDecimal());
    }

    public List<ExpressionEntity> parse(String expr) {
        List<ExpressionEntity> entities = new ArrayList<>();
        int len = expr.length();
        int leftBracketCount = 0; // 左侧括号数量
        int curIdx = 0;
        while (curIdx < len) {
            switch (expr.charAt(curIdx)) {
                case ' ': { // 空格忽略
                    curIdx++;
                    break;
                }
                case '"': { // 字符串常量限定符，双引号里面为字符串常量
                    curIdx++; // 跳过左边的"
                    // 找右边的"，提取字符串常量。注意排除转义的情况 \"
                    int p = curIdx;
                    while (p < len) {
                        if (expr.charAt(p) == '"' && expr.charAt(p - 1) != '\\') {
                            break;
                        }
                        p++;
                    }
                    if (p >= len) { // 找到表达式末尾也没有找到右侧的限定符
                        throw new ExpressionException("缺少右侧的字符串限定符：" + expr.substring(curIdx - 1, p));
                    }
                    entities.add(new StringValue(expr.substring(curIdx, p)));
                    curIdx = p + 1;
                    break;
                }
                case '+': {
                    curIdx++;
                    entities.add(new PLUS());
                    break;
                }
                case '-': {
                    curIdx++;
                    boolean needInsertZero = false;
                    if (entities.isEmpty()) { // 表达式一开头就是负数的情况  -2+3 => 0-2+3
                        needInsertZero = true;
                    } else {
                        ExpressionEntity prevEntity = entities.get(entities.size() - 1);
                        if (prevEntity instanceof Bracket && ((Bracket) prevEntity).isLeftBracket()) { // 2+(-3) => 2+(0-3)
                            needInsertZero = true;
                        }
                    }
                    if (needInsertZero) {
                        entities.add(new NumericValue("0"));
                    }
                    entities.add(new MINUS());
                    break;
                }
                case '*': {
                    curIdx++;
                    entities.add(new MULTIPLY());
                    break;
                }
                // TODO 待支持其他算术运算符和逻辑运算符
                case '(': {
                    curIdx++;
                    leftBracketCount++;
                    entities.add(Bracket.createLeftBracket());
                    break;
                }
                case ')': {
                    if (leftBracketCount <= 0) {
                        throw new ExpressionException("无效的右侧括号，缺少与之匹配的左侧括号：" + expr.substring(0, curIdx));
                    }
                    curIdx++;
                    leftBracketCount--;
                    entities.add(Bracket.createRightBracket());
                    break;
                }
                default: {
                    // TODO 暂时当值处理，后续考虑支持变量引用、表达式引用
                    if (Character.isDigit(expr.charAt(curIdx))) { // 数字开头，当作数值处理。
                        int p = curIdx;
                        int digitPointCount = 0;
                        while (p < len) {
                            char c = expr.charAt(p);
                            if (!Character.isDigit(c) && c != '.') {
                                break;
                            }
                            p++;
                            if (c == '.') {
                                digitPointCount++;
                            }
                        }
                        if (digitPointCount > 1) { // 存在多个小数点，非法数值
                            throw new ExpressionException("非法的数值：" + expr.substring(curIdx, p + 1));
                        }
                        entities.add(new NumericValue(expr.substring(curIdx, p)));
                        curIdx = p;
                    } else {
                        throw new ExpressionException("非法的操作数，暂不支持解析：" + expr.substring(0, curIdx + 1));
                    }
                }
            }
        }
        // 检查表达式是否合法
        checkExpressionEntities(entities);
        return entities;
    }

    protected void checkExpressionEntities(List<ExpressionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            throw new ExpressionException("表达式元素序列为空");
        }
        boolean hasNull = false;
        StringBuilder sbd = new StringBuilder();
        int total = entities.size();
        for (int i = 0; i < total; i++) {
            ExpressionEntity entity = entities.get(i);
            if (entity == null) {
                hasNull = true;
                sbd.append("表达式序列第").append(i).append("个序列为null\n");
            }
        }
        if (hasNull) {
            throw new ExpressionException(sbd.toString());
        }

        checkExpressionFirstEntity(entities.get(0), total);
        checkExpressionLastEntity(entities.get(total - 1));
        checkExpressionBrackets(entities);
        if (total > 1) {
            for (int i = 1; i < total; i++) {
                checkExpressionAdjacentEntity(entities.get(i - 1), entities.get(i));
            }
        }
    }

    protected void checkExpressionBrackets(List<ExpressionEntity> entities) {
        int bracketCount = 0;
        for (ExpressionEntity entity : entities) {
            if (entity instanceof Bracket) {
                Bracket bracket = (Bracket) entity;
                if (bracket.isLeftBracket()) {
                    bracketCount++;
                } else {
                    if (bracketCount <= 0) {
                        throw new ExpressionException("左右括号不成对匹配");
                    }
                    bracketCount--;
                }
            }
        }
    }

    protected void checkExpressionAdjacentEntity(ExpressionEntity prev, ExpressionEntity follow) {
        if (prev instanceof Operator) {
            if (follow instanceof Operator) { // TODO 支持逻辑表达式的时候，此处需要考虑非的情形
                throw new ExpressionException("运算符后不能跟运算符");
            }
            if (follow instanceof Bracket && ((Bracket) follow).isRightBracket()) {
                throw new ExpressionException("运算符后不能跟右括号");
            }
        }
        if (prev instanceof Operand) {
            if (follow instanceof Operand) {
                throw new ExpressionException("操作数后不能跟操作数");
            }
            if (follow instanceof Bracket && ((Bracket) follow).isLeftBracket()) {
                throw new ExpressionException("操作数后不能跟左括号");
            }
            // TODO 支持逻辑表达式的时候，此处需要考虑非的情形
        }
        if (prev instanceof Bracket) {
            Bracket bracket = (Bracket) prev;
            if (bracket.isLeftBracket()) {
                // TODO 非

                if (follow instanceof Bracket && ((Bracket) follow).isRightBracket()) {
                    throw new ExpressionException("左括号后不能直接跟右括号");
                }
            } else {
                if (follow instanceof Operand) {
                    throw new ExpressionException("右括号不能直接跟操作数");
                }
                // TODO 非
                if (follow instanceof Bracket && ((Bracket) follow).isLeftBracket()) {
                    throw new ExpressionException("右括号后不能直接跟左括号");
                }
            }
        }
    }

    protected void checkExpressionFirstEntity(ExpressionEntity entity, int total) {
        if (total == 1) {
            if (entity instanceof Operator) {
                throw new ExpressionException("只有一个元素时，不能是运算符");
            }
            if (entity instanceof Bracket) {
                throw new ExpressionException("只有一个元素时，不能是括号");
            }
        } else {
            if (entity instanceof Operator) {
                if (!(entity instanceof MINUS)) { // TODO 支持逻辑表达式的时候，此处需要考虑非的情形
                    throw new ExpressionException("第一元素为运算符时，只能是减号");
                }
            }
            if (entity instanceof Bracket) {
                if (!((Bracket) entity).isLeftBracket()) {
                    throw new ExpressionException("第一个元素为括号时，只能是左括号");
                }
            }
        }
    }

    protected void checkExpressionLastEntity(ExpressionEntity entity) {
        if (entity instanceof Operator) {
            throw new ExpressionException("最后一个元素不能是运算符");
        }
        if (entity instanceof Bracket) {
            if (((Bracket) entity).isLeftBracket()) {
                throw new ExpressionException("最后一个元素是括号时，不能是左括号");
            }
        }
    }

    public static boolean isOperatorChar(char c) {
        return OPERATOR_CHARSET.indexOf(c) >= 0;
    }

    public Value calculateELOV(List<ExpressionEntity> entities) {
        Deque<CalcStep> steps = new ArrayDeque<>();
        steps.push(buildNextCalcStep(CalcStep.ROOT, entities));
        while (!steps.isEmpty()) {
            CalcStep curStep = steps.peek();
            if (curStep.isUnary()) {
                throw new ExpressionException("暂不支持单目运算符"); // TODO
            } else {
                boolean isLeftResolved = curStep.checkSourceResolved(CalcStep.LEFT);
                boolean isRightResolved = curStep.checkSourceResolved(CalcStep.RIGHT);
                // 左侧还没解算且可以解算
                if (!isLeftResolved && curStep.checkSourceResolvable(CalcStep.LEFT)) {
                    curStep.resolveSource(CalcStep.LEFT);
                    isLeftResolved = true;
                }
                // 右侧还没解算且可以解算
                if (!isRightResolved && curStep.checkSourceResolvable(CalcStep.RIGHT)) {
                    curStep.resolveSource(CalcStep.RIGHT);
                    isRightResolved = true;
                }
                if (isLeftResolved && isRightResolved) { // 分解的计算任务回溯
                    steps.pop(); // 当前计算任务已经解算完成，弹出
                    curStep.doCalculate();
                    Value curValue = curStep.getResultValue();
                    if (steps.isEmpty()) { // 无剩余计算任务
                        if (curStep.getLastStepType() != CalcStep.ROOT) {
                            throw new ExpressionException("表达式解算异常");
                        }
                        return curValue; // 解算完成，返回最终结果
                    } else { // 还存在上级任务，那么当前计算任务的结果给上级计算任务使用
                        steps.peek().setSourceResolveValue(curStep.getLastStepType(), curValue);
                    }

                } else {
                    // 分解计算任务入栈
                    // 1.左侧未解算；2.右侧未解算；3.左右侧都没有解算(解算顺序先左再右)
                    if (!isLeftResolved) {
                        List<ExpressionEntity> leftSource = curStep.getSourceByType(CalcStep.LEFT);
                        CalcStep nextStep = buildNextCalcStep(CalcStep.LEFT, leftSource);
                        steps.push(nextStep); // 当前计算任务分解出来的子任务，随后入站
                    }
                    if (!isRightResolved) {
                        List<ExpressionEntity> rightSource = curStep.getSourceByType(CalcStep.RIGHT);
                        CalcStep nextStep = buildNextCalcStep(CalcStep.RIGHT, rightSource);
                        steps.push(nextStep);
                    }
                }
            }
        }
        throw new ExpressionException("解算异常");
    }

    protected CalcStep buildNextCalcStep(int lastStepType, List<ExpressionEntity> source) {
        // 脱去最外侧的括号，形如(( ... ))，因为括号的优先级最高
        List<ExpressionEntity> tmpSource = trimBracket(source);
        if (tmpSource.size() == 1) { // 只有一个操作数
            if (source.get(0) instanceof Operand) {
                return new CalcStep(lastStepType, null, tmpSource);
            } else {
                throw new ExpressionException("表达式序列异常");
            }
        }
        int bracketCount = 0;
        boolean isInBracket = false;
        int minPriority = Integer.MAX_VALUE;
        int operatorIdx = -1;
        // 对于相同优先级的，取最右侧那个运算符
        for (int i = tmpSource.size() - 1; i >= 0; i--) {
            ExpressionEntity entity = tmpSource.get(i);
            if (entity instanceof Bracket) {
                Bracket bracket = (Bracket) entity;
                if (bracket.isRightBracket()) {
                    bracketCount++;
                    if (!isInBracket) {
                        isInBracket = true;
                    }
                } else {
                    bracketCount--;
                    if (bracketCount == 0) {
                        isInBracket = false;
                    }
                }
            } else { // 非括号
                if (isInBracket || !(entity instanceof Operator)) {
                    continue; // 由于括号优先级最高，所以括号内的跳过
                }
                Operator operator = (Operator) entity;
                int curPriority = operator.getPriority().getPriority();
                if (curPriority < minPriority) { // 不能取等号
                    minPriority = curPriority;
                    operatorIdx = i;
                }
            }
        }
        if (operatorIdx < 0) {
            throw new ExpressionException("找不到优先级最低的运算符");
        }
        // TODO 待处理单目运算符
        Operator operator = (Operator) tmpSource.get(operatorIdx);
        List<ExpressionEntity> leftSource = getLeftEntities(tmpSource, operatorIdx);
        List<ExpressionEntity> rightSource = getRightEntities(tmpSource, operatorIdx);
        return new CalcStep(lastStepType, operator, leftSource, rightSource);
    }

    private List<ExpressionEntity> getLeftEntities(List<ExpressionEntity> source, int idx) {
        if (idx == 0) {
            return null;
        }
        List<ExpressionEntity> result = new ArrayList<>();
        for (int i = 0; i < idx; i++) {
            result.add(source.get(i));
        }
        return result;
    }

    private static List<ExpressionEntity> getRightEntities(List<ExpressionEntity> source, int idx) {
        int size = source.size();
        if (idx == size - 1) {
            return null;
        }
        List<ExpressionEntity> result = new ArrayList<>();
        for (int i = idx + 1; i < size; i++) {
            result.add(source.get(i));
        }
        return result;
    }

    protected static List<ExpressionEntity> trimBracket(List<ExpressionEntity> source) {
        int prev = 0, tail = source.size();
        while (prev < tail) {
            ExpressionEntity prevEntity = source.get(prev);
            ExpressionEntity tailEntity = source.get(tail - 1);
            if (prevEntity instanceof Bracket && ((Bracket) prevEntity).isLeftBracket() &&
                    tailEntity instanceof Bracket && ((Bracket) tailEntity).isRightBracket()) {
                prev++;
                tail--;
            } else {
                break;
            }
        }
        if (prev >= tail) {
            throw new ExpressionException("括号中无操作数");
        }
        return source.subList(prev, tail);
    }

    private static class CalcStep {
        public static final int ROOT = -1; // 不存在上级计算任务
        public static final int LEFT = 0; // 当前计算任务来自于上级计算任务的左侧
        public static final int RIGHT = 1; // 当前计算任务来自于上级计算任务的右侧
        private Operator operator; // 当前计算步骤的运算符
        private boolean isUnary; // 是否单目运算符
        private int lastStepType; // 上一级计算计算任务源，指明当前任务是上一级任务的左侧还是右侧操作数
        private List<ExpressionEntity> leftSource, rightSource; // 左右未解算的序列，解算之后需要清空序列
        private Value leftValue, rightValue; // 左右序列解算后的值

        private Value result;

        public CalcStep(int lastStepType, Operator operator, List<ExpressionEntity> sources) {
            assert sources != null;
            this.lastStepType = lastStepType;
            this.operator = operator;
            this.isUnary = true;
            this.leftSource = sources;
        }

        // 双目运算符
        public CalcStep(int lastStepType, Operator operator, List<ExpressionEntity> leftSource, List<ExpressionEntity> rightSource) {
            assert operator != null;
            assert leftSource != null;
            assert rightSource != null;
            this.lastStepType = lastStepType;
            this.operator = operator;
            this.isUnary = false;
            this.leftSource = leftSource;
            this.rightSource = rightSource;
        }

        public boolean isUnary() {
            return isUnary;
        }

        public boolean checkSourceResolved() {
            return checkSourceResolved(LEFT) && checkSourceResolved(RIGHT);
        }

        public boolean checkSourceResolved(int sourceType) {
            switch (sourceType) {
                case LEFT:
                    return leftValue != null;
                case RIGHT:
                    return isUnary() || rightValue != null; // 单目运算符，rightValue没有意义，直接返回true表示已解算
                default:
                    throw new ExpressionException("非法的计算任务标志");
            }
        }

        public boolean checkSourceResolvable(int sourceType) {
            return checkSourceResolvable(getSourceByType(sourceType));
        }

        public boolean checkSourceResolvable(List<ExpressionEntity> source) {
            // TODO 待处理单目运算符
            List<ExpressionEntity> tmpSource = trimBracket(source);
            return tmpSource.size() == 1 && tmpSource.get(0) instanceof Operand;
        }

        // 只有一个操作数的时候才能解算
        public void resolveSource(int sourceType) {
            if (isUnary()) {
                throw new ExpressionException("暂不支持处理单目运算符");
            }
            List<ExpressionEntity> source = getSourceByType(sourceType);
            if (!checkSourceResolvable(source)) {
                throw new ExpressionException(sourceType + "尚未解算");
            }
            ExpressionEntity entity = source.get(0);
            if (entity instanceof Value) {
                setSourceResolveValue(sourceType, (Value) entity);
            } else {
                throw new ExpressionException("解算异常");
            }
        }

        public List<ExpressionEntity> getSourceByType(int sourceType) {
            switch (sourceType) {
                case LEFT:
                    return leftSource;
                case RIGHT:
                    return rightSource;
                default:
                    throw new ExpressionException("非法的计算任务标志");
            }
        }

        public void doCalculate() {
            if (!checkSourceResolved()) {
                throw new ExpressionException("操作数序列尚未解算，不能执行该运算符");
            }
            if (isUnary()) {
                if (operator != null) {
                    // TODO
                    throw new ExpressionException("暂不支持单目运算符");
                } else { // 只有一个操作数
                    result = leftValue;
                }
            } else {
                // TODO 暂时先处理算术运算符
                if (operator instanceof ArithmeticOperator) {
                    ArithmeticOperator arithmeticOpr = (ArithmeticOperator) operator;
                    if (leftValue instanceof NumericValue && rightValue instanceof NumericValue) {
                        NumericValue leftNum = (NumericValue) leftValue;
                        NumericValue rightNum = (NumericValue) rightValue;
                        result = arithmeticOpr.evaluate(leftNum, rightNum);
                        // 解算结果后，清除缓存的序列，help gc
                        leftSource.clear();
                        rightSource.clear();
                    } else {
                        throw new ExpressionException("表达式序列异常");
                    }
                }
            }
        }

        public void setSourceResolveValue(int sourceType, Value value) {
            assert value != null;
            switch (sourceType) {
                case LEFT:
                    leftValue = value;
                    break;
                case RIGHT:
                    rightValue = value;
                    break;
                default:
                    throw new ExpressionException("非法的计算任务标志");
            }
        }

        public Value getResultValue() {
            return result;
        }

        public int getLastStepType() {
            return lastStepType;
        }
    }
}
