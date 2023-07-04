package com.example.flowable.controller;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dongdahai
 * @date: 2023/7/4
 */
@Slf4j
public class TestControllerTest {

    @Autowired
    private TaskService taskService;
    private String staffId = "00001";
    private String jingliTask = "10001";


    @Test
    public void submitAsk() {
        List<Task> list = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskCreateTime().desc().list();
        for (Task task : list) {
            log.info("任务ID ：{}； 任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
            Map<String, Object> map = new HashMap<>();
            map.put("jingliTask", jingliTask);
            taskService.complete(task.getId(), map);
        }
    }

    @Test
    public void approve() {
        List<Task> list =taskService.createTaskQuery().taskAssignee(staffId).orderByTaskCreateTime().desc().list();
        for (Task task : list) {
            log.info("经理：{} 审批：{} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            taskService.complete(task.getId(), map);
        }
    }
}
