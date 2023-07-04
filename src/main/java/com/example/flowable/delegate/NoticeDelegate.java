package com.example.flowable.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * @author: dongdahai
 * @date: 2023/7/4
 */
@Component
@Slf4j
public class NoticeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("发送邮件提醒！{}", delegateExecution.getProcessInstanceId());
    }
}
