package top.ysqorz.lenovo;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IPCRequestDTO {
    private String message;
    private String data;
    private JSONObject params;
}