package cn.lucasma.activiti.bpmn20;

import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * userTask候选人候选组测试
 */
public class UserTaskTest {


    private static final Logger logger = LoggerFactory.getLogger(UserTaskTest.class);


    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    /**
     * 候选人候选组测试
     */
    @Test
    @Deployment(resources = {"my-process-usertask.bpmn20.xml"})
    public void testUserTask() {

        ProcessInstance processInstance = activitiRule.getRuntimeService()
                .startProcessInstanceByKey("my-process");
        TaskService taskService = activitiRule.getTaskService();
        //根据候选人查询
        Task task = taskService.createTaskQuery().taskCandidateUser("user1").singleResult();
        logger.info("find by user1 task = {}", task);

        task = taskService.createTaskQuery().taskCandidateUser("user2").singleResult();
        logger.info("find by user2 task = {}", task);
        //根据候选组
        task = taskService.createTaskQuery().taskCandidateGroup("group1").singleResult();
        logger.info("find by group1 task = {}", task);

        //设置代理人  方式一推荐
        taskService.claim(task.getId(), "user2");
        //设置代理人  方式二不推荐
//        taskService.setAssignee(task.getId(), "user2");
        logger.info("claim task.id = {} by user2", task.getId());

       //当task指定代理人后task候选人会被清空 taskCandidateOrAssigned根据候选人或者代理人
        task = taskService.createTaskQuery().taskCandidateOrAssigned("user1").singleResult();
        logger.info("find by user1 task ={}", task);
        task = taskService.createTaskQuery().taskCandidateOrAssigned("user2").singleResult();
        logger.info("find by user2 task ={}", task);
    }

    /**
     * 通过TaskListener设置候选人候选组
     */
    @Test
    @Deployment(resources = {"my-process-usertask2.bpmn20.xml"})
    public void testUserTask2() {
        ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");

        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().taskCandidateUser("user1").singleResult();
        logger.info("find by user1 task ={}", task);
        taskService.complete(task.getId());
    }
}
