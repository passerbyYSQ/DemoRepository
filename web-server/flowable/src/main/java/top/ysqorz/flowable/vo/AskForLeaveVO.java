package top.ysqorz.flowable.vo;

import lombok.Data;

@Data
public class AskForLeaveVO {
    private String name;
    private Integer days;
    private String reason;
}