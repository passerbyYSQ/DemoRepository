package top.ysqorz;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import org.junit.Test;
import top.ysqorz.util.CommonUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void test() {
        JSONObject userCreator = new JSONObject()
                .set("Tel", "18575784346");

        JSONObject body = new JSONObject()
                .set("installation", "default")
                .set("account", "zhangsan666")
                .set("userName", "zhangsan666")
                .set("source", "DEFAULT")
                .set("userCreator", userCreator);

        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put("callerID", new String[]{"edu-cloud"});
        String rawData = CommonUtils.extractRequestParams(body.toString(), paramMap, null);
        Sign sign = SecureUtil.sign(SignAlgorithm.valueOf("SHA256withRSA"),
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ54qrIuOgxBPXAYSRjECw9GZeBOiYm07d1LJHEDcx6BqAW6rMRyxMkHaiWsptielJRJUw92vbGNdym/4kxvwIZl34KsCp4TaQ0md9srnpiJ/aVC019g8wej8jC7Uq6kSJoGsNm8A+Cr75l3oVrSL6k/QgOBvz9n6vkn+NiC5wxZAgMBAAECgYBKI8AQboof06HvwJzqJkXaVqAxCe1U7L3gK1iYOvnvys8WJxjPK7afKoXhrF2+uKpgmdFvSalD2SBn+urfweS6/qPFeNVxn5mp/I1iC0V9z29vzVmEMz8iVLqmiHi6aPMGujRuRq87ok6sgZzKz2M4Bv0pDyDNhjV60w/omlUhNQJBAN6YxYUNxjeBZEOLGiA90xJPLxsYXEFmA4EM7dAmYQmpb1MhizArhzD+q4DKd6Kpd9ty7rtCHvgaLxctMv0YiHsCQQC2QHeyezUDNhsA+Jrutcz3Qf4vVYmomlqHQ3LNHxqRJmESEKJwlIZjEVXC3LHwhyhyKxL4L3SXCFQ8Gxw9h0g7AkEAjnaIM9fU/ME7Ql9MoUXwSM+U/mpg4RpBM97qdUWa5Wwja7kUNAgnxhu39/2dA+YiZ7jvQbnXyEI50UOEVqfU8QJAHbSgTe0bFOzqtdvPlPz47YxKG/j7OQn/m3B488oQEHyK8eDYOTVcOiWHLv292xCMnR88Nku12zA2Wp2omrbEJQJBAL74xpq8LUbLJO9KRKXU37RD48aRm1R+wfStvvn8AIxvZJTwpuxlRuggfWPvsxckWS0qRrUIdUC054n4dNjfDBs=",
                "");
        // 签名
        String signature = sign.signHex(rawData);

        HttpResponse response = HttpUtil.createPost("http://192.168.9.162:8040/open-api/v1/account/create" + "?callerID=edu-cloud")
                .body(body.toString())
                .header("X-Request-Signature", signature)
                .execute();
        JSONObject res = new JSONObject(response.body());
        System.out.println(res.toString());
    }

}
