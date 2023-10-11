package top.ysqorz.wpe.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/10/11
 */
@Data
@Accessors(chain = true)
public class CsvRowVOMeta {
    private String packageName;
    private String className;
    private String classComment;
    private List<AttributeMeta> attrs;
}
