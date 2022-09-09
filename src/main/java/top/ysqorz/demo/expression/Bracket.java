package top.ysqorz.demo.expression;

public class Bracket implements ExpressionEntity {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private int type;

    private Bracket(int type) {
        this.type = type;
    }

    public static Bracket createLeftBracket() {
        return new Bracket(LEFT);
    }

    public static Bracket createRightBracket() {
        return new Bracket(RIGHT);
    }

    public boolean isLeftBracket() {
        return type == LEFT;
    }

    public boolean isRightBracket() {
        return type == RIGHT;
    }

    public int getBracketType() {
        return type;
    }

    @Override
    public int getEntityType() {
        return BRACKET;
    }
}
