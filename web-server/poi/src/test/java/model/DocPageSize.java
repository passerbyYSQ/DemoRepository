package model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * ISO 216标准是一种国际标准，定义了一系列标准纸张尺寸，其中最常见的是A系列。这些尺寸按照特定的比例设计，
 * 使得相邻尺寸之间的长宽比始终保持为1(√2。单位均为毫米
 *
 * @author yaoshiquan
 * @date 2024/4/11
 */
public enum DocPageSize {
    A0(841, 1189),
    A1(594, 841),
    A2(420, 594),
    A3(297, 420),
    A4(210, 297),
    A5(148, 210),
    A6(105, 148),
    B5(176, 250),
    Letter(216, 279),
    Legal(216, 356);

    final int width;
    final int height;

    public static final BigDecimal MM_2_TWIP_WEIGHT = BigDecimal.valueOf(56.6929134);

    DocPageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BigInteger getTwipWidth() {
        return BigDecimal.valueOf(width).multiply(MM_2_TWIP_WEIGHT).toBigInteger();
    }

    public BigInteger getTwipHeight() {
        return BigDecimal.valueOf(height).multiply(MM_2_TWIP_WEIGHT).toBigInteger();
    }
}
