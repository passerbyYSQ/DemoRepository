package top.ysqorz.expression;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import top.ysqorz.expression.path.BeanPath;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExpressionApplication.class})
public class ExpressionApplicationTest {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void test() { // equals($s.Projects[0])
        String input = "func(arg1, arg2, arg3)";
        String regex = "^([a-zA-Z_$][a-zA-Z_$\\d]*)\\((\\s*.+\\s*(,\\s*.+\\s*)*)?\\)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String args = matcher.group(2);
            System.out.println("Method name: [" + methodName + "]");
            System.out.println("Args: [" + args + "]");
        }
    }

    @Test
    public void test1() {
        String[] split = "ac, asf, sdg  , qfqwe".split("\\s*,\\s*");
        System.out.println(Arrays.toString(split));
    }

    /**
     *
     */
    @Test
    public void testSplit() {
        String path = "xxx.hello(xxxx, $o.haha(xxx, xxx), xxxx).xxx[0].xxx['123'].get(xxxx)";
        String[] syntaxArray = path.split("\\.(?![^(]*\\))"); // 根据【不在括号的点】分割，负向前瞻断言
        System.out.println(Arrays.toString(syntaxArray));
    }

    @Test
    public void testSplit2() {
        String path = "xxx.hello(xxxx, $o.haha(xxx, $s.user.uid), xxxx).xxx[0].xxx['123'].get(xxxx)";
        // 【不在括号的点】，即最外层的点
        int level = 0; // 括号层数
        StringBuilder sbd = new StringBuilder();
        List<String> syntaxList = new ArrayList<>();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '.' && level == 0) { // 不在括号的点，即最外层的点
                syntaxList.add(sbd.toString());
                sbd = new StringBuilder();
                continue;
            }
            if (c == '(') {
                level++;
            } else if (c == ')') {
                level--;
            }
            sbd.append(c);
        }
        System.out.println(syntaxList);
    }

    @Test
    public void testBeanPath() throws Exception {
        //String path = "$s.get(\"user\").projects[1].member.contains($o.participants.owners[0])";
        // $s.uid.xx.xxx[0].equal($o.Owner)
        String path = "$s.get(\"user\").get(keys.hello.haha)[1].member.contains($o.participants.owners[0])";
        File jsonFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "obj.json");
        Object source = objectMapper.readValue(jsonFile, Object.class);
        BeanPath beanPath = new BeanPath(path, source);
        System.out.println(beanPath.getValue());
    }

    @Test
    public void testProps() {
        Properties props = new Properties();
        List<String> strs = new ArrayList<>();
        strs.add("123");
        props.put("project", strs);
        Object project = props.get("project");
        System.out.println(123);
    }

    @Test
    public void testParseInt() {
        // 1685081918152
        // 1216517788565300
        System.out.println( System.currentTimeMillis());
    }

    @Test
    public void testCMeta() throws Exception {
        System.out.println(executeCmd("cmeta", "-c", "-v"));
        // zwplm01是实例名
//        System.out.println(executeCmd("cmeta", "-i", "default"));
    }

    public int executeCmd(String cmd, String... args) throws Exception {
        String[] completedCmd = new String[args.length + 1];
        completedCmd[0] = cmd;
        System.arraycopy(args, 0, completedCmd, 1, args.length);
        ProcessBuilder processBuilder = new ProcessBuilder(completedCmd);
        Map<String, String> environment = processBuilder.environment();
        // 模拟core_env指令设置进程的环境变量 -1073741819
        String oreRoot = environment.get("ORE_ROOT");
        environment.put("ORE_JAVA_HOME", environment.get("JAVA_HOME"));
        environment.put("ORE_JLIB_PATH", oreRoot + File.separator + "jlib");
        environment.put("ORE_VAR_ROOT", oreRoot + File.separator + "var");
        environment.put("ORE_INSTALL_ROOT", oreRoot + File.separator + "installations");
        Process process = processBuilder.start();
        println(process.getInputStream());
        System.out.println("---------------------------");
        println(process.getErrorStream());
        return process.waitFor(); // 进程正常退出时返回值为0
    }

    public void println(InputStream inputStream) throws IOException {
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufReader.readLine()) != null) {
            System.out.println(line);
        }
    }

    @Test
    public void test113() {
        String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJBY2NvdW50IjoiYWRtaW4iLCJVc2VyTmFtZSI6ImFkbWluIiwiVXNlcklEIjoiYWRtaW4iLCJpc3MiOiJaV1RlYW13b3JrcyIsImV4cCI6MTY4NDQ5ODE3NiwiaWF0IjoxNjg0NDk2Mzc2fQ.F6JOJkpmy4ROUR44lQ9vF-Nf-VSVvcQY-it9vQ5u8pY";
        Map<String, String> userMap = getUserNameMapping(jwt);
        //System.out.println(userMap);
        Set<String> CADModel = getCADCreator("CADModel", jwt);// 40s
        System.out.println("CADModel");
//        Set<String> CADDrafting = getCADCreator("CADDrafting", jwt);
        System.out.println("CADDrafting");
        Set<String> CADDrawing = getCADCreator("CADDrawing", jwt);
        Set<String> total = new HashSet<>();
        total.addAll(CADModel);
//        total.addAll(CADDrafting);
        total.addAll(CADDrawing); // ui
        System.out.println(total.size());
        for (String id : total) {
            String cname = userMap.get(id);
            System.out.println(id + ": " + cname);
        }
    }


    public Set<String> getCADCreator(String clsName, String jwt) {
        String json = HttpUtil.createPost("https://zwteamworks-ct-api.zwsoft.cn/core/message/dispatch")
                .body("{\n" +
                        "  \"message\": \"QueryColumn\",\n" +
                        "  \"data\": \"clsName\",\n" +
                        "  \"params\": {\n" +
                        "    \"clsName\": \"" + clsName + "\",\n" +
                        "    \"filter\": {},\n" +
                        "    \"columns\": [\"Creator\"]\n" +
                        "  }\n" +
                        "}")
                .header("Authorization", jwt)
                .execute().body();
        JSONObject res = JSONUtil.parseObj(json);
        JSONArray table = res.getByPath("data.params.matrix.table", JSONArray.class);
        Set<String> set = new HashSet<>();
        for (int i = 0; i < table.size(); i++) {
            JSONArray row = table.getJSONArray(i);
            set.add(row.getStr(0));
        }
        return set;
    }

    public Map<String, String> getUserNameMapping(String jwt) {
        String json = HttpUtil.createPost("https://zwteamworks-ct-api.zwsoft.cn/core/message/dispatch")
                .body("{\n" +
                        "  \"message\": \"QueryColumn\",\n" +
                        "  \"data\": \"clsName\",\n" +
                        "  \"params\": {\n" +
                        "    \"clsName\": \"CoreUser\",\n" +
                        "    \"filter\": {},\n" +
                        "    \"columns\": [\"Name\", \"CName\"]\n" +
                        "  }\n" +
                        "}")
                .header("Authorization", jwt)
                .execute().body();
        JSONObject res = JSONUtil.parseObj(json);
        JSONArray table = res.getByPath("data.params.matrix.table", JSONArray.class);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < table.size(); i++) {
            JSONArray user = table.getJSONArray(i);
            map.put(user.getStr(0), user.getStr(1));
        }
        return map;
    }

    /**
     * 已登录用户
     */
    @Test
    public void testAccount() {
        String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJBY2NvdW50IjoiYWRtaW4iLCJVc2VyTmFtZSI6ImFkbWluIiwiVXNlcklEIjoiYWRtaW4iLCJpc3MiOiJaV1RlYW13b3JrcyIsImV4cCI6MTY4NDQ5ODE3NiwiaWF0IjoxNjg0NDk2Mzc2fQ.F6JOJkpmy4ROUR44lQ9vF-Nf-VSVvcQY-it9vQ5u8pY";
        Map<String, String> userMap = getUserNameMapping(jwt);
        Map<String, String> accountMap = getLoginAccount(jwt);
        System.out.println(userMap.size());
        System.out.println(accountMap.size()); // 登录过用户
        accountMap.forEach((id, time) -> {
            String cname = userMap.get(id);
            System.out.println(id + ": " + cname + ": " + time);
        });
    }

    /**
     * 未登录用户
     */
    @Test
    public void testAccount1() {
        String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJBY2NvdW50IjoiYWRtaW4iLCJVc2VyTmFtZSI6ImFkbWluIiwiVXNlcklEIjoiYWRtaW4iLCJpc3MiOiJaV1RlYW13b3JrcyIsImV4cCI6MTY4NDQ5ODE3NiwiaWF0IjoxNjg0NDk2Mzc2fQ.F6JOJkpmy4ROUR44lQ9vF-Nf-VSVvcQY-it9vQ5u8pY";
        Map<String, String> userMap = getUserNameMapping(jwt);
        Map<String, String> accountMap = getLoginAccount(jwt);
        AtomicInteger count = new AtomicInteger();
        userMap.forEach((id, cname) -> {
            if (!accountMap.containsKey(id)) {
                System.out.println(id + ": " + cname);
                count.getAndIncrement();
            }
        });
        System.out.println(count.get());
    }

    public Map<String, String> getLoginAccount(String jwt) {
        String json = HttpUtil.createPost("https://zwteamworks-ct-api.zwsoft.cn/core/message/dispatch")
                .body("{\n" +
                        "  \"message\": \"QueryColumn\",\n" +
                        "  \"data\": \"clsName\",\n" +
                        "  \"params\": {\n" +
                        "    \"clsName\": \"LoginAccount\",\n" +
                        "    \"filter\": {\n" +
                        "      \"LastLoginTime\": \"1*\"\n" +
                        "    },\n" +
                        "    \"columns\": [\"UserName\", \"LastLoginTime\"]\n" +
                        "  }\n" +
                        "}")
                .header("Authorization", jwt)
                .execute().body();
        JSONObject res = JSONUtil.parseObj(json);
        JSONArray table = res.getByPath("data.params.matrix.table", JSONArray.class);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < table.size(); i++) {
            JSONArray row = table.getJSONArray(i);
            map.put(row.getStr(0), new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(row.getStr(1)))));
        }
        return map;
    }
}
