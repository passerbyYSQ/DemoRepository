package top.ysqorz.i18n;

import org.junit.Assert;
import org.junit.Test;
import top.ysqorz.i18n.api.common.CommonUtils;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class CommonUtilsTest {
    @Test
    public void testConvert() {
        Assert.assertEquals("MY_ROOT_CONFIG", CommonUtils.camelToScreamingSnake("MyRootConfig"));
        Assert.assertEquals("MY_ORE_ROOT_CONFIG", CommonUtils.camelToScreamingSnake("MyORERootConfig"));
    }
}
