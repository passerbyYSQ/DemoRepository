package top.ysqorz.flowable.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.ysqorz.flowable.service.AskForLeaveService;
import top.ysqorz.flowable.vo.AskForLeaveVO;

@RestController
public class AskForLeaveController {
    @Autowired
    private AskForLeaveService askForLeaveService;

    @PostMapping("/ask_for_leave")
    public String askForLeave(@RequestBody AskForLeaveVO askForLeaveVO) {
        return askForLeaveService.askForLeave(askForLeaveVO);
    }

}