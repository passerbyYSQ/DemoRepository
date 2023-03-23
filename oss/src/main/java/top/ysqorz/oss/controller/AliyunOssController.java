package top.ysqorz.oss.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ysqorz.oss.model.STSCredential;
import top.ysqorz.oss.strategy.impl.AliyunOSSStrategy;

import javax.annotation.Resource;

@RestController
@RequestMapping("/oss/aliyun")
public class AliyunOssController {
    @Resource
    private AliyunOSSStrategy aliyunOssStrategy;

    @GetMapping("/sts_token")
    public STSCredential getSTSToken() {
        return aliyunOssStrategy.generateSTSToken();
    }
}
