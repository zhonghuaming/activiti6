package cn.lucasma.activiti.example;

import com.google.common.collect.Lists;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Lucas Ma
 */
public class MyTaskListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(MyTaskListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        if (StringUtils.equals("create", eventName)) {
            logger.info("config by listener");
            //设置候选人
            delegateTask.addCandidateUsers(Lists.newArrayList("user1", "user2"));
            //设置候选组
            delegateTask.addCandidateGroup("group1");
            //设置参数
            delegateTask.setVariable("key1", "value1");
            //设置过期时间
            delegateTask.setDueDate(DateTime.now().plusDays(3).toDate());
        } else if (StringUtils.equals("complete", eventName)) {
            logger.info("task complete");
        }
    }
}
