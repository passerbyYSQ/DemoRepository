package top.ysqorz.expression.path;

public interface PathVariable {
    boolean match();

    Object reduce(Object reducer);

    String getSyntax();

    Object getResult(); 
}
