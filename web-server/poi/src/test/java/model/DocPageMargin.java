package model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/4/11
 */
public enum DocPageMargin {
    narrow(12.7, 12.7, 12.7, 12.7),
    moderate(25.4, 25.4, 19.1, 19.1),
    normal(25.4, 25.4, 31.8, 31.8),
    wide(25.4, 25.4, 50.8, 50.8);

    final double top;
    final double bottom;
    final double left;
    final double right;

    public static final BigDecimal MM_2_TWIP_WEIGHT = BigDecimal.valueOf(56.6929134);

    DocPageMargin(double top, double bottom, double left, double right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public double getTop() {
        return top;
    }

    public double getBottom() {
        return bottom;
    }

    public double getLeft() {
        return left;
    }

    public double getRight() {
        return right;
    }

    public BigInteger getTwipTop() {
        return BigDecimal.valueOf(top).multiply(MM_2_TWIP_WEIGHT).toBigInteger();
    }

    public BigInteger getTwipBottom() {
        return BigDecimal.valueOf(bottom).multiply(MM_2_TWIP_WEIGHT).toBigInteger();
    }

    public BigInteger getTwipLeft() {
        return BigDecimal.valueOf(left).multiply(MM_2_TWIP_WEIGHT).toBigInteger();
    }

    public BigInteger getTwipRight() {
        return BigDecimal.valueOf(right).multiply(MM_2_TWIP_WEIGHT).toBigInteger();
    }
}
