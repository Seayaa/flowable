package com.example.flowable.controller;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dongdahai
 * @date: 2023/7/3
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;

    private String staffId = "00001";
    private String managerId = "10002";

    /**
     * 流程跟踪图片查看
     *
     * @param res
     * @param processId
     * @return void
     * @throws
     */
    @GetMapping("/pic")
    public void showPic(HttpServletResponse res, String processId) throws Exception{
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (pi == null) {
            return;
        }
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(processId)
                .list();
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, "Simsun", "Simsun", "Simsun", engconf.getClassLoader(), 1.0, false);

        BufferedImage image = ImageIO.read(in);
        int desireWidth = 2000;
        int desireHeight = 2000;
        int width = image.getWidth();
        int height = image.getHeight();
        int offsetX = (desireWidth - width) / 2;
        int offsetY = (desireHeight - height) / 2;
        BufferedImage centeredImage =
                new BufferedImage(desireWidth, desireHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = centeredImage.createGraphics();
        g2d.drawImage(image, offsetX, offsetY, null);
        g2d.dispose();

        OutputStream out = null;
        try {
            out = res.getOutputStream();
            ImageIO.write(centeredImage, "png", out);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    /**
     * 开启请假流程
     *
     * @param staffId
     * @return java.lang.String
     * @throws
     */
    @GetMapping("/startLeaveProcess")
    public String startLeaveProcess(@RequestParam("staffId") String staffId) {
        Map<String, Object> map = new HashMap<>();
        map.put("staffId", staffId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("askForLeave", map);
        StringBuilder sb = new StringBuilder();
        sb.append("创建请假流程 processId: " + processInstance.getId());
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskCreateTime().desc().list();
        for (Task task : tasks) {
            sb.append("任务taskId: " + task.getId());
        }
        return sb.toString();
    }

    /**
     * 提交审批
     *
     * @param staffId
     * @return java.lang.String
     * @throws
     */
    @GetMapping("/submitAsk")
    public String submitAsk(@RequestParam("staffId") String staffId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskCreateTime().desc().list();
        for (Task task : list) {
            log.info("任务ID ：{}； 任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
            Map<String, Object> map = new HashMap<>();
            map.put("managerId", managerId);
            taskService.complete(task.getId(), map);
        }
        return "提交成功";
    }

    /**
     * 审批通过
     *
     * @param staffId
     * @return java.lang.String
     * @throws
     */
    @GetMapping("/approve")
    public String approve(@RequestParam("staffId") String staffId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskCreateTime().desc().list();
        for (Task task : list) {
            log.info("经理：{} 审批：{} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("passed", true);
            taskService.complete(task.getId(), map);
        }
        return "审批通过";
    }

    /**
     * 审批不通过
     *
     * @param staffId
     * @return java.lang.String
     * @throws
     */
    @GetMapping("/reject")
    public String reject(@RequestParam("staffId") String staffId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskCreateTime().desc().list();
        for (Task task : list) {
            log.info("经理：{} 审批：{} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("passed", false);
            taskService.complete(task.getId(), map);
        }
        return "审批不通过";
    }
}
