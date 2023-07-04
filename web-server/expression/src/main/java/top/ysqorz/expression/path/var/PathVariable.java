package top.ysqorz.expression.path.var;

public interface PathVariable {
    boolean match();

    Object reduce(Object reducer);

    String getSyntax();

    Object getResult(); 
}
